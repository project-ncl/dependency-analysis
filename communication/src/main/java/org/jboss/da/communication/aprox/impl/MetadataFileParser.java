package org.jboss.da.communication.aprox.impl;

import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.aprox.model.VersionResponse;
import org.jboss.da.communication.aprox.model.npm.NpmMetadata;
import org.jboss.da.communication.repository.api.RepositoryException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class MetadataFileParser {

    private ObjectMapper om = new ObjectMapper();

    public static VersionResponse parseMavenMetadata(InputStream in) throws IOException,
            CommunicationException, JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(VersionResponse.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return (VersionResponse) jaxbUnmarshaller.unmarshal(in);
    }

    public NpmMetadata parseNpmMetadata(URLConnection connection) throws CommunicationException {
        try (InputStream in = connection.getInputStream()) {
            return om.readValue(in, NpmMetadata.class);
        } catch (IOException e) {
            throw new RepositoryException("Failed to parse metadata file", e);
        }
    }
}
