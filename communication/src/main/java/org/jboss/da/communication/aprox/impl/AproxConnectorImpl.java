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
import org.jboss.da.model.rest.GA;
import org.jboss.da.model.rest.GAV;
import org.jboss.pnc.pncmetrics.MetricsConfiguration;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.jboss.da.common.logging.MDCUtils;
import org.jboss.da.common.util.UserLog;

@ApplicationScoped
public class AproxConnectorImpl implements AproxConnector {

    private static final String METRICS_KEY = "da.client.indy.timer";

    @Inject
    private Logger log;

    @Inject
    @UserLog
    private Logger userLog;

    private DAConfig config;

    @Inject
    private PomAnalyzer pomAnalyzer;

    @Inject
    private MetadataFileParser parser;

    @Inject
    private MetricsConfiguration metricsConfiguration;

    private HttpClient client;

    private HttpRequest.Builder requestBuilder;

    @Inject
    public AproxConnectorImpl(Configuration configuration) {
        try {
            this.config = configuration.getConfig();
            client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(config.getAproxRequestTimeout())).build();
            requestBuilder = HttpRequest.newBuilder().timeout(
                    Duration.ofMillis(config.getAproxRequestTimeout()));
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
            userLog.info("Retrieving versions for maven artifacts " + ga + " from " + query);
            HttpResponse<InputStream> connection = getResponse(query);
            if (connection.statusCode() == 404) {
                log.debug("Maven metadata for {} not found. Assuming empty version list.", ga);
                return Collections.emptyList();
            }

            final List<String> versions = parseMetadataFile(connection.body()).getVersioning()
                    .getVersions().getVersion();
            log.debug("Maven metadata for {} found. Response: {}. Versions: {}", ga,
                    connection.statusCode(), versions);
            return versions;
        } catch (IOException | CommunicationException | InterruptedException e) {
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
            userLog.info("Retrieving versions for npm artifacts " + packageName + " from " + query);
            log.info("Retrieving npm metadata for " + packageName + " from " + query);
            HttpResponse<InputStream> connection = getResponse(query);
            if (connection.statusCode() == 404) {
                log.debug("Npm metadata for {} not found. Assuming empty version list.",
                        packageName);
                return Collections.emptyList();
            }

            final Set<String> versions = parser.parseNpmMetadata(connection.body()).getVersions()
                    .keySet();
            log.debug("Npm metadata for {} found. Response: {}. Versions: {}", packageName,
                    connection.statusCode(), versions);
            return new ArrayList(versions);
        } catch (IOException | InterruptedException e) {
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

    private java.net.http.HttpResponse<InputStream> getResponse(String query)
            throws java.io.IOException, java.lang.InterruptedException {
        return getResponse(query, BodyHandlers.ofInputStream());
    }

    private <T> HttpResponse<T> getResponse(String query, BodyHandler<T> bodyHandler)
            throws IOException, InterruptedException {
        HttpRequest.Builder requestBuild = requestBuilder.copy().uri(URI.create(query));
        MDCUtils.headersFromContext().forEach(requestBuild::header);
        HttpRequest request = requestBuild.build();
        HttpResponse<T> response = client.send(request, bodyHandler);
        int retry = 0;
        while ((response.statusCode() == 504 || response.statusCode() == 500) && retry < 2) {
            userLog.warn("Connection to: {} failed with status: {}. retrying...", query,
                    response.statusCode());
            retry++;
            try {
                // Wait before retrying using Exponential back-off: 200ms, 400ms
                Thread.sleep((long) Math.pow(2, retry) * 100L);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
            response = client.send(request, bodyHandler);
        }
        return response;
    }

    @Override
    public Optional<MavenProject> getPom(GAV gav) throws RepositoryException {
        return getPomStream(gav).flatMap(pomAnalyzer::readPom);
    }

    @Override
    public Optional<InputStream> getPomStream(GAV gav) throws RepositoryException {
        StringBuilder query = new StringBuilder();
        query.append(config.getAproxServer());
        query.append("/api/group/");
        query.append(config.getAproxGroupPublic()).append('/');
        query.append(gav.getGroupId().replace(".", "/")).append("/");
        query.append(gav.getArtifactId()).append('/');
        query.append(gav.getVersion()).append('/');
        query.append(gav.getArtifactId()).append('-').append(gav.getVersion()).append(".pom");
        try {
            HttpResponse<InputStream> response = getResponse(query.toString());
            if (response.statusCode() == 404) {
                return Optional.empty();
            }
            return Optional.of(response.body());
        } catch (IOException | InterruptedException e) {
            throw new RepositoryException("Failed to obtain pom for " + gav
                    + " from repository on url " + query, e);
        }
    }

    @Override
    /**
     * Implementation note: dcheung tried to initially use HttpURLConnection and send a 'HEAD'
     * request to the resource. Even though that worked, for some reason this completely makes
     * Arquillian fail to deploy the testsuite. For that reason, dcheung switched to using a simple
     * URL object instead with the try-catch logic.
     *
     * No dcheung doesn't usually talks about himself in the third person..
     */
    public boolean doesGAVExistInPublicRepo(GAV gav) throws RepositoryException {
        StringBuilder query = new StringBuilder();
        query.append(config.getAproxServer());
        query.append("/api/group/");
        query.append(config.getAproxGroupPublic()).append('/');
        query.append(gav.getGroupId().replace(".", "/")).append("/");
        query.append(gav.getArtifactId()).append('/');
        query.append(gav.getVersion()).append('/');
        query.append(gav.getArtifactId()).append("-").append(gav.getVersion()).append(".pom");

        try {
            HttpResponse<Void> response = getResponse(query.toString(), BodyHandlers.discarding());
            return response.statusCode() < 300;
        } catch (IOException | InterruptedException e) {
            throw new RepositoryException("Failed to check existence of pom for " + gav
                    + " in repository on url " + query, e);
        }
    }

    private VersionResponse parseMetadataFile(InputStream in) throws IOException,
            CommunicationException {
        try {
            return MetadataFileParser.parseMavenMetadata(in);
        } catch (JAXBException e) {
            throw new RepositoryException("Failed to parse metadata file", e);
        } finally {
            in.close();
        }
    }
}
