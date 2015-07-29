package org.jboss.da.communication.aprox.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.jboss.da.common.util.Configuration;
import org.jboss.da.common.util.ConfigurationParseException;
import org.jboss.da.communication.aprox.api.AproxConnector;
import org.jboss.da.communication.aprox.model.GA;
import org.jboss.da.communication.aprox.model.GAV;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;
import org.jboss.da.communication.aprox.model.VersionResponse;

import javax.enterprise.context.ApplicationScoped;
import org.jboss.da.communication.CommunicationException;

@ApplicationScoped
public class AproxConnectorImpl implements AproxConnector {

    private Configuration config = new Configuration();

    @Override
    public GAVDependencyTree getDependencyTreeOfRevision(String scmUrl, String revision,
            String pomPath) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GAVDependencyTree getDependencyTreeOfGAV(GAV gav) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getVersionsOfGA(GA ga) throws CommunicationException {
        StringBuilder query = new StringBuilder();
        try {
            query.append(config.getConfig().getAproxServer());
            query.append("group/");
            query.append(config.getConfig().getAproxGroup()).append('/');
            query.append(ga.getGroupId().replace(".", "/")).append("/");
            query.append(ga.getArtifactId()).append('/');
            query.append("maven-metadata.xml");

            URLConnection connection = new URL(query.toString()).openConnection();

            return parseMetadataFile(connection).getVersioning().getVersions().getVersion();
        } catch (IOException | ConfigurationParseException | CommunicationException e) {
            throw new CommunicationException("Failed to obtain versions for " + ga.toString(), e);
        }
    }

    private VersionResponse parseMetadataFile(URLConnection connection) throws IOException,
            CommunicationException {
        try (InputStream in = connection.getInputStream()) {
            JAXBContext jaxbContext = JAXBContext.newInstance(VersionResponse.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (VersionResponse) jaxbUnmarshaller.unmarshal(in);
        } catch (JAXBException e) {
            throw new CommunicationException("Failed to parse metadataFile", e);
        }
    }
}
