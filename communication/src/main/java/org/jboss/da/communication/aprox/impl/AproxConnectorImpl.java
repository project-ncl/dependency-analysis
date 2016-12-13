package org.jboss.da.communication.aprox.impl;

import org.jboss.da.common.CommunicationException;
import org.jboss.da.common.json.DAConfig;
import org.jboss.da.common.util.Configuration;
import org.jboss.da.common.util.ConfigurationParseException;
import org.jboss.da.communication.aprox.api.AproxConnector;
import org.jboss.da.communication.aprox.model.VersionResponse;
import org.jboss.da.communication.pom.api.PomAnalyzer;
import org.jboss.da.communication.pom.model.MavenProject;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AproxConnectorImpl implements AproxConnector {

    @Inject
    private Logger log;

    @Inject
    private Configuration config;

    @Inject
    private PomAnalyzer pomAnalyzer;

    @Override
    public List<String> getVersionsOfGA(GA ga) throws CommunicationException {
        StringBuilder query = new StringBuilder();
        try {
            DAConfig config = this.config.getConfig();
            query.append(config.getAproxServer());
            query.append("/api/group/");
            query.append(config.getAproxGroup()).append('/');
            query.append(ga.getGroupId().replace(".", "/")).append("/");
            query.append(ga.getArtifactId()).append('/');
            query.append("maven-metadata.xml");

            log.info("Retrieving metadata for " + ga + " from " + query.toString());
            HttpURLConnection connection = (HttpURLConnection) new URL(query.toString())
                    .openConnection();

            int retry = 0;
            while (connection.getResponseCode() == 504 && retry < 5) {
                connection = (HttpURLConnection) new URL(query.toString()).openConnection();
                retry++;
            }

            return parseMetadataFile(connection).getVersioning().getVersions().getVersion();
        } catch (FileNotFoundException ex) {
            return Collections.emptyList();
        } catch (IOException | ConfigurationParseException | CommunicationException e) {
            throw new CommunicationException("Failed to obtain versions for " + ga.toString()
                    + " from approx server with url " + query.toString(), e);
        }
    }

    @Override
    public Optional<MavenProject> getPom(GAV gav) throws CommunicationException {
        Optional<InputStream> is = getPomStream(gav);
        if (is.isPresent()) {
            return pomAnalyzer.readPom(getPomStream(gav).get());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<InputStream> getPomStream(GAV gav) throws CommunicationException {
        StringBuilder query = new StringBuilder();
        try {
            DAConfig cfg = this.config.getConfig();
            query.append(cfg.getAproxServer());
            query.append("/api/group/");
            query.append(cfg.getAproxGroupPublic()).append('/');
            query.append(gav.getGroupId().replace(".", "/")).append("/");
            query.append(gav.getArtifactId()).append('/');
            query.append(gav.getVersion()).append('/');
            query.append(gav.getArtifactId()).append('-').append(gav.getVersion()).append(".pom");

            URLConnection connection = new URL(query.toString()).openConnection();
            return Optional.of(connection.getInputStream());
        } catch (FileNotFoundException ex) {
            return Optional.empty();
        } catch (IOException | ConfigurationParseException e) {
            throw new CommunicationException("Failed to obtain pom for " + gav.toString()
                    + " from approx server with url " + query.toString(), e);
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
    public boolean doesGAVExistInPublicRepo(GAV gav) throws CommunicationException {
        StringBuilder query = new StringBuilder();

        try {
            DAConfig config = this.config.getConfig();
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

        } catch (IOException | ConfigurationParseException e) {
            throw new CommunicationException("Failed to establish a connection with Aprox: "
                    + query.toString(), e);
        }
    }

    private VersionResponse parseMetadataFile(URLConnection connection) throws IOException,
            CommunicationException {
        try (InputStream in = connection.getInputStream()) {
            return MetadataFileParser.parseMetadataFile(in);
        } catch (JAXBException e) {
            throw new CommunicationException("Failed to parse metadataFile", e);
        }
    }
}
