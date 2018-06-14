package org.jboss.da.communication.aprox.impl;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.common.json.DAConfig;
import org.jboss.da.common.util.Configuration;
import org.jboss.da.common.util.ConfigurationParseException;
import org.jboss.da.communication.aprox.api.AproxConnector;
import org.jboss.da.communication.aprox.model.VersionResponse;
import org.jboss.da.communication.pom.api.PomAnalyzer;
import org.jboss.da.communication.pom.model.MavenProject;
import org.jboss.da.communication.repository.api.RepositoryException;
import org.jboss.da.metrics.MetricsConfiguration;
import org.jboss.da.model.rest.GA;
import org.jboss.da.model.rest.GAV;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class AproxConnectorImpl implements AproxConnector {

    private static final String METRICS_KEY = "da.client.indy.timer";

    @Inject
    private Logger log;

    private DAConfig config;

    @Inject
    private PomAnalyzer pomAnalyzer;

    private MetadataFileParser parser = new MetadataFileParser();

    @Inject
    private MetricsConfiguration metricsConfiguration;

    @Inject
    public AproxConnectorImpl(Configuration configuration) {
        try {
            this.config = configuration.getConfig();
        } catch (ConfigurationParseException ex) {
            throw new IllegalStateException(
                    "Configuration failure, can't parse default repository group", ex);
        }
    }

    @Override
    public List<String> getVersionsOfGA(GA ga) throws RepositoryException {
        return this.getVersionsOfGA(ga, config.getAproxGroup());
    }

    @Override
    public List<String> getVersionsOfGA(GA ga, String repository) throws RepositoryException {
        String query = repositoryLink("maven", repository, ga.getGroupId().replace(".", "/") + "/"
                + ga.getArtifactId());

        MetricRegistry registry = metricsConfiguration.getMetricRegistry();
        Timer.Context context = null;

        if (registry != null) {
            Timer timer = registry.timer(METRICS_KEY);
            context = timer.time();
        }

        try {
            log.info("Retrieving maven metadata for " + ga + " from " + query);
            HttpURLConnection connection = getResponse(query);

            final List<String> versions = parseMetadataFile(connection).getVersioning()
                    .getVersions().getVersion();
            log.debug("Maven metadata for {} found. Response: {}. Versions: {}", ga,
                    connection.getResponseCode(), versions);
            return versions;
        } catch (FileNotFoundException ex) {
            log.debug("Maven metadata for {} not found. Assuming empty version list.", ga);
            return Collections.emptyList();
        } catch (IOException | CommunicationException e) {
            throw new RepositoryException("Failed to obtain versions for " + ga
                    + " from repository on url " + query, e);
        } finally {
            if (context != null) {
                context.stop();
            }
        }
    }

    @Override
    public List<String> getVersionsOfNpm(String packageName) throws RepositoryException {
        return this.getVersionsOfNpm(packageName, config.getAproxGroup());
    }

    @Override
    public List<String> getVersionsOfNpm(String packageName, String repository)
            throws RepositoryException {
        String query = repositoryLink("npm", repository, packageName);
        try {
            log.info("Retrieving npm metadata for " + packageName + " from " + query);
            HttpURLConnection connection = getResponse(query);

            final Set<String> versions = parser.parseNpmMetadata(connection).getVersions().keySet();
            log.debug("Npm metadata for {} found. Response: {}. Versions: {}", packageName,
                    connection.getResponseCode(), versions);
            return new ArrayList(versions);
        } catch (FileNotFoundException ex) {
            log.debug("Npm metadata for {} not found. Assuming empty version list.", packageName);
            return Collections.emptyList();
        } catch (IOException | CommunicationException e) {
            throw new RepositoryException("Failed to obtain versions for " + packageName
                    + " from repository on url " + query, e);
        }
    }

    private String repositoryLink(String type, String repository, String path) {
        StringBuilder query = new StringBuilder();
        query.append(config.getAproxServer());
        query.append("/api/content/");
        query.append(type);
        query.append("/group/");
        query.append(repository).append('/');
        query.append(path).append('/');
        switch (type) {
            case "maven": {
                query.append("maven-metadata.xml");
                break;
            }
            case "npm": {
                query.append("package.json");
                break;
            }
        }

        return query.toString();
    }

    private HttpURLConnection getResponse(String query) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(query).openConnection();
        connection.setConnectTimeout(config.getAproxRequestTimeout());
        connection.setReadTimeout(config.getAproxRequestTimeout());
        int retry = 0;
        while ((connection.getResponseCode() == 504 || connection.getResponseCode() == 500)
                && retry < 2) {

            log.warn("Connection to: {} failed with status: {}. retrying...", query,
                    connection.getResponseCode());

            retry++;

            try {
                // Wait before retrying using Exponential back-off: 200ms, 400ms
                Thread.sleep((long) Math.pow(2, retry) * 100L);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }

            connection = (HttpURLConnection) new URL(query).openConnection();
            connection.setConnectTimeout(config.getAproxRequestTimeout());
            connection.setReadTimeout(config.getAproxRequestTimeout());
        }
        return connection;
    }

    @Override
    public Optional<MavenProject> getPom(GAV gav) throws RepositoryException {
        return getPomStream(gav).flatMap(pomAnalyzer::readPom);
    }

    @Override
    public Optional<InputStream> getPomStream(GAV gav) throws RepositoryException {
        StringBuilder query = new StringBuilder();
        try {
            query.append(config.getAproxServer());
            query.append("/api/group/");
            query.append(config.getAproxGroupPublic()).append('/');
            query.append(gav.getGroupId().replace(".", "/")).append("/");
            query.append(gav.getArtifactId()).append('/');
            query.append(gav.getVersion()).append('/');
            query.append(gav.getArtifactId()).append('-').append(gav.getVersion()).append(".pom");

            URLConnection connection = new URL(query.toString()).openConnection();
            return Optional.of(connection.getInputStream());
        } catch (FileNotFoundException ex) {
            return Optional.empty();
        } catch (IOException e) {
            throw new RepositoryException("Failed to obtain pom for " + gav
                    + " from repository on url " + query, e);
        }
    }

    @Override
    /**
     * Implementation note: dcheung tried to initially use HttpURLConnection
     * and send a 'HEAD' request to the resource. Even though that worked,
     * for some reason this completely makes Arquillian fail to deploy the testsuite.
     * For that reason, dcheung switched to using a simple URL object instead with the
     * try-catch logic.
     *
     * No dcheung doesn't usually talks about himself in the third person..
     */
    public boolean doesGAVExistInPublicRepo(GAV gav) throws RepositoryException {
        StringBuilder query = new StringBuilder();

        try {
            query.append(config.getAproxServer());
            query.append("/api/group/");
            query.append(config.getAproxGroupPublic()).append('/');
            query.append(gav.getGroupId().replace(".", "/")).append("/");
            query.append(gav.getArtifactId()).append('/');
            query.append(gav.getVersion()).append('/');
            query.append(gav.getArtifactId()).append("-").append(gav.getVersion()).append(".pom");

            URLConnection connection = new URL(query.toString()).openConnection();
            try {
                connection.getInputStream().close();
                // if we've reached here, then it means the pom exists
                return true;
            } catch (FileNotFoundException e) {
                // if we've reached here, the resource is not available
                return false;
            }
        } catch (IOException e) {
            throw new RepositoryException("Failed to check existence of pom for " + gav
                    + " in repository on url " + query, e);
        }
    }

    private VersionResponse parseMetadataFile(URLConnection connection) throws IOException,
            CommunicationException {
        try (InputStream in = connection.getInputStream()) {
            return MetadataFileParser.parseMavenMetadata(in);
        } catch (JAXBException e) {
            throw new RepositoryException("Failed to parse metadata file", e);
        }
    }
}
