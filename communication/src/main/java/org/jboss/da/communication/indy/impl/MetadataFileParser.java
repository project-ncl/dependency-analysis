package org.jboss.da.communication.indy.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import org.jboss.da.communication.indy.model.VersionResponse;
import org.jboss.da.communication.indy.model.npm.NpmMetadata;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@ApplicationScoped
public class MetadataFileParser {

    @Inject
    ObjectMapper om;

    public static VersionResponse parseMavenMetadata(InputStream in) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(VersionResponse.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return (VersionResponse) jaxbUnmarshaller.unmarshal(in);
    }

    public NpmMetadata parseNpmMetadata(URLConnection connection) throws IOException {
        try (InputStream in = connection.getInputStream()) {
            return om.readValue(in, NpmMetadata.class);
        }
    }
}
