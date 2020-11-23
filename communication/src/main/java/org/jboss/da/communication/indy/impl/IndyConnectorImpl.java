package org.jboss.da.communication.indy.impl;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.common.json.DAConfig;
import org.jboss.da.common.json.GlobalConfig;
import org.jboss.da.common.util.Configuration;
import org.jboss.da.common.util.ConfigurationParseException;
import org.jboss.da.communication.indy.api.IndyConnector;
import org.jboss.da.communication.indy.model.VersionResponse;
import org.jboss.da.communication.pom.api.PomAnalyzer;
import org.jboss.da.communication.pom.model.MavenProject;
import org.jboss.da.communication.repository.api.RepositoryException;
import org.jboss.da.model.rest.GA;
import org.jboss.da.model.rest.GAV;
import org.jboss.pnc.pncmetrics.MetricsConfiguration;
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
import org.jboss.da.common.logging.MDCUtils;
import org.jboss.da.common.util.UserLog;

@ApplicationScoped
public class IndyConnectorImpl implements IndyConnector {

    private static final String METRICS_KEY = "da.client.indy.timer";

    @Inject
    private Logger log;

    @Inject
    @UserLog
    private Logger userLog;

    private DAConfig config;

    private GlobalConfig globalConfig;

    @Inject
    private PomAnalyzer pomAnalyzer;

    @Inject
    private MetadataFileParser parser;

    @Inject
    private MetricsConfiguration metricsConfiguration;

    @Inject
    public IndyConnectorImpl(Configuration configuration) {
        try {
            config = configuration.getConfig();
            globalConfig = configuration.getGlobalConfig();
        } catch (ConfigurationParseException ex) {
            throw new IllegalStateException("Configuration failure, can't parse default repository group", ex);
        }
    }

    @Override
    public List<String> getVersionsOfGA(GA ga) throws RepositoryException {
        return this.getVersionsOfGA(ga, config.getIndyGroup());
    }

    @Override
    public List<String> getVersionsOfGA(GA ga, String repository) throws RepositoryException {
        String query = repositoryLink(
                "maven",
                repository,
                ga.getGroupId().replace(".", "/") + "/" + ga.getArtifactId());

        MetricRegistry registry = metricsConfiguration.getMetricRegistry();
        Timer.Context context = null;

        if (registry != null) {
            Timer timer = registry.timer(METRICS_KEY);
            context = timer.time();
        }

        try {
            userLog.info("Retrieving versions for maven artifacts " + ga + " from " + query);
            HttpURLConnection connection = getResponse(query);

            final List<String> versions = parseMetadataFile(connection).getVersioning().getVersions().getVersion();
            log.debug(
                    "Maven metadata for {} found. Response: {}. Versions: {}",
                    ga,
                    connection.getResponseCode(),
                    versions);
            return versions;
        } catch (FileNotFoundException ex) {
            log.debug("Maven metadata for {} not found. Assuming empty version list.", ga);
            return Collections.emptyList();
        } catch (IOException | CommunicationException e) {
            throw new RepositoryException(
                    "Failed to obtain versions for " + ga + " from repository on url " + query,
                    e);
        } finally {
            if (context != null) {
                context.stop();
            }
        }
    }

    @Override
    public List<String> getVersionsOfNpm(String packageName) throws RepositoryException {
        return this.getVersionsOfNpm(packageName, config.getIndyGroup());
    }

    @Override
    public List<String> getVersionsOfNpm(String packageName, String repository) throws RepositoryException {
        String query = repositoryLink("npm", repository, packageName);
        try {
            userLog.info("Retrieving versions for npm artifacts " + packageName + " from " + query);
            log.info("Retrieving npm metadata for " + packageName + " from " + query);
            HttpURLConnection connection = getResponse(query);

            final Set<String> versions = parser.parseNpmMetadata(connection).getVersions().keySet();
            log.debug(
                    "Npm metadata for {} found. Response: {}. Versions: {}",
                    packageName,
                    connection.getResponseCode(),
                    versions);
            return new ArrayList<>(versions);
        } catch (FileNotFoundException ex) {
            log.debug("Npm metadata for {} not found. Assuming empty version list.", packageName);
            return Collections.emptyList();
        } catch (IOException e) {
            throw new RepositoryException(
                    "Failed to obtain versions for " + packageName + " from repository on url " + query,
                    e);
        }
    }

    private String repositoryLink(String type, String repository, String path) {
        StringBuilder query = new StringBuilder();
        query.append(globalConfig.getIndyUrl());
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
        MDCUtils.headersFromContext().forEach(connection::addRequestProperty);
        connection.setConnectTimeout(config.getIndyRequestTimeout());
        connection.setReadTimeout(config.getIndyRequestTimeout());
        int retry = 0;
        while ((connection.getResponseCode() == 504 || connection.getResponseCode() == 500) && retry < 2) {

            userLog.warn("Connection to: {} failed with status: {}. retrying...", query, connection.getResponseCode());
            log.warn("Connection to: {} failed with status: {}. retrying...", query, connection.getResponseCode());

            retry++;

            try {
                // Wait before retrying using Exponential back-off: 200ms, 400ms
                Thread.sleep((long) Math.pow(2, retry) * 100L);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }

            connection = (HttpURLConnection) new URL(query).openConnection();
            MDCUtils.headersFromContext().forEach(connection::addRequestProperty);
            connection.setConnectTimeout(config.getIndyRequestTimeout());
            connection.setReadTimeout(config.getIndyRequestTimeout());
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
            query.append(globalConfig.getIndyUrl());
            query.append("/api/content/maven/group/");
            query.append(config.getIndyGroupPublic()).append('/');
            query.append(gav.getGroupId().replace(".", "/")).append("/");
            query.append(gav.getArtifactId()).append('/');
            query.append(gav.getVersion()).append('/');
            query.append(gav.getArtifactId()).append('-').append(gav.getVersion()).append(".pom");

            URLConnection connection = new URL(query.toString()).openConnection();
            MDCUtils.headersFromContext().forEach(connection::addRequestProperty);
            return Optional.of(connection.getInputStream());
        } catch (FileNotFoundException ex) {
            return Optional.empty();
        } catch (IOException e) {
            throw new RepositoryException("Failed to obtain pom for " + gav + " from repository on url " + query, e);
        }
    }

    @Override
    /**
     * Implementation note: dcheung tried to initially use HttpURLConnection and send a 'HEAD' request to the resource.
     * Even though that worked, for some reason this completely makes Arquillian fail to deploy the testsuite. For that
     * reason, dcheung switched to using a simple URL object instead with the try-catch logic.
     *
     * No dcheung doesn't usually talks about himself in the third person..
     */
    public boolean doesGAVExistInPublicRepo(GAV gav) throws RepositoryException {
        StringBuilder query = new StringBuilder();

        try {
            query.append(globalConfig.getIndyUrl());
            query.append("/api/content/maven/group/");
            query.append(config.getIndyGroupPublic()).append('/');
            query.append(gav.getGroupId().replace(".", "/")).append("/");
            query.append(gav.getArtifactId()).append('/');
            query.append(gav.getVersion()).append('/');
            query.append(gav.getArtifactId()).append("-").append(gav.getVersion()).append(".pom");

            URLConnection connection = new URL(query.toString()).openConnection();
            MDCUtils.headersFromContext().forEach(connection::addRequestProperty);
            try {
                connection.getInputStream().close();
                // if we've reached here, then it means the pom exists
                return true;
            } catch (FileNotFoundException e) {
                // if we've reached here, the resource is not available
                return false;
            }
        } catch (IOException e) {
            throw new RepositoryException(
                    "Failed to check existence of pom for " + gav + " in repository on url " + query,
                    e);
        }
    }

    private VersionResponse parseMetadataFile(URLConnection connection) throws IOException, CommunicationException {
        try (InputStream in = connection.getInputStream()) {
            return MetadataFileParser.parseMavenMetadata(in);
        } catch (JAXBException e) {
            throw new RepositoryException("Failed to parse metadata file", e);
        }
    }
}
