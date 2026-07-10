package org.jboss.da.communication.repository;

import java.io.IOException;
import java.io.InputStream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import org.jboss.da.communication.repository.model.VersionResponse;
import org.jboss.da.communication.repository.model.npm.NpmMetadata;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@ApplicationScoped
public class MetadataFileParser {

    private static final JAXBContext JAXB_CONTEXT;

    static {
        try {
            JAXB_CONTEXT = JAXBContext.newInstance(VersionResponse.class);
        } catch (JAXBException e) {
            throw new ExceptionInInitializerError("Failed to initialize JAXBContext: " + e.getMessage());
        }
    }

    @Inject
    ObjectMapper om;

    public VersionResponse parseMavenMetadata(InputStream in) throws JAXBException {
        Unmarshaller jaxbUnmarshaller = JAXB_CONTEXT.createUnmarshaller();
        return (VersionResponse) jaxbUnmarshaller.unmarshal(in);
    }

    public NpmMetadata parseNpmMetadata(InputStream in) throws IOException {
        return om.readValue(in, NpmMetadata.class);
    }
}
