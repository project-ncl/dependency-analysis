package org.jboss.da.communication.rt;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.xml.bind.JAXBException;

import org.jboss.da.common.CommunicationException;
import org.jboss.da.common.config.Configuration;
import org.jboss.da.common.logging.UserLog;
import org.jboss.da.communication.pom.model.MavenProject;
import org.jboss.da.communication.repository.MetadataFileParser;
import org.jboss.da.communication.repository.api.RepositoryConnector;
import org.jboss.da.communication.repository.api.RepositoryException;
import org.jboss.da.model.rest.GA;
import org.jboss.da.model.rest.GAV;
import org.jboss.pnc.api.enums.RepositoryType;
import org.jboss.pnc.common.log.MDCUtils;
import org.slf4j.Logger;


@ApplicationScoped
public class ArtifactoryConnector implements RepositoryConnector {

    private final Logger log;

    private final Logger userLog;

    private final Configuration.Artifactory rtConfiguration;

    private final MetadataFileParser parser;

    private final HttpClient httpClient;

    public ArtifactoryConnector(
            Logger log,
            @UserLog Logger userLog,
            Configuration configuration,
            MetadataFileParser parser) {
        this.log = log;
        this.userLog = userLog;
        this.rtConfiguration = configuration.artifactory();
        this.parser = parser;
        this.httpClient = HttpClient.newBuilder().connectTimeout(configuration.artifactory().requestTimeout()).build();
    }

    @Override
    public List<String> getVersionsOfGA(GA ga) throws RepositoryException {
        String query = repositoryPath(
                RepositoryType.MAVEN,
                ga.getGroupId().replace(".", "/") + "/" + ga.getArtifactId());
        try {
            userLog.info("Retrieving versions for maven artifacts {} from {}", ga, query);

            HttpResponse<InputStream> connection = makeRequest(query);
            if (connection == null) {
                return Collections.emptyList();
            }
            List<String> versions;
            try (InputStream in = connection.body()) {
                versions = parser.parseMavenMetadata(in).getVersioning().getVersions().getVersion();
            } catch (JAXBException e) {
                throw new RepositoryException(
                        "Failed to parse metadata file. RT status code: " + connection.statusCode(),
                        e);
            }

            log.debug(
                    "Maven metadata for {} found. Response: {}. Versions: {}",
                    ga,
                    connection.statusCode(),
                    versions);

            return versions;
        } catch (FileNotFoundException ex) {
            log.debug("Maven metadata for {} not found. Assuming empty version list.", ga);
            return Collections.emptyList();
        } catch (IOException | CommunicationException e) {
            log.debug("Failed to obtain versions for {} from repository on url {}", ga, query, e);
            throw new RepositoryException(
                    "Failed to obtain versions for " + ga + " from repository on url " + query,
                    e);
        }
    }

    @Override
    public List<String> getVersionsOfNpm(String packageName) throws RepositoryException {
        String query = repositoryPath(RepositoryType.NPM, packageName);
        try {
            userLog.info("Retrieving versions for npm artifacts {} from {}", packageName, query);
            log.info("Retrieving npm metadata for {} from {}", packageName, query);

            HttpResponse<InputStream> connection = makeRequest(query);
            if (connection == null) {
                return Collections.emptyList();
            }

            Set<String> versions;
            try (InputStream in = connection.body()) {
                versions = parser.parseNpmMetadata(in).getVersions().keySet();
            }

            log.debug(
                    "Npm metadata for {} found. Response: {}. Versions: {}",
                    packageName,
                    connection.statusCode(),
                    versions);

            return new ArrayList<>(versions);
        } catch (FileNotFoundException ex) {
            log.debug("Npm metadata for {} not found. Assuming empty version list.", packageName);
            return Collections.emptyList();
        } catch (IOException e) {
            log.debug("Failed to obtain versions for {} from repository on url {}", packageName, query, e);
            throw new RepositoryException(
                    "Failed to obtain versions for " + packageName + " from repository on url " + query,
                    e);
        }
    }

    @Override
    @Deprecated
    public Optional<MavenProject> getPom(GAV gav) throws RepositoryException {
        throw new UnsupportedOperationException("Unused.");
    }

    @Override
    @Deprecated
    public Optional<InputStream> getPomStream(GAV gav) throws RepositoryException {
        throw new UnsupportedOperationException("Unused.");
    }

    @Override
    @Deprecated
    public boolean doesGAVExistInPublicRepo(GAV gav) throws RepositoryException {
        throw new UnsupportedOperationException("Unused.");
    }

    private String repositoryPath(RepositoryType type, String path) {
        StringBuilder query = new StringBuilder();
        query.append("/artifactory/");
        query.append(rtConfiguration.groups().get(type)).append('/');
        query.append(path).append('/');
        query.append(switch (type) {
            case MAVEN -> "maven-metadata.xml";
            case NPM -> "package.json";
            case COCOA_POD, GENERIC_PROXY, DISTRIBUTION_ARCHIVE ->
                throw new UnsupportedOperationException("Not supported.");
        });
        return query.toString();
    }

    private HttpResponse<InputStream> makeRequest(String path) throws IOException {
        try {
            int retry = 0;
            HttpResponse<InputStream> response;
            do {
                retry++;
                HttpRequest.Builder builder = HttpRequest.newBuilder()
                        .GET()
                        .uri(rtConfiguration.url().get().resolve(path))
                        .timeout(rtConfiguration.requestTimeout())
                        .header("Authorization", "Bearer " + rtConfiguration.accessToken().get());

                MDCUtils.getHeadersFromMDC().forEach(builder::setHeader);

                response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofInputStream());
            } while ((response.statusCode() == 504 || response.statusCode() == 500)
                    && retry <= rtConfiguration.requestRetries());

            return response;
        } catch (InterruptedException e) {
            // DA got interrupted while the request was processing
            return null;
        }
    }
}
