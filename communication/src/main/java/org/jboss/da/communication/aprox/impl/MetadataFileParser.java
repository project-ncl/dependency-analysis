package org.jboss.da.communication.aprox.impl;

import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.aprox.model.VersionResponse;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class MetadataFileParser {

    public static VersionResponse parseMetadataFile(InputStream in) throws IOException,
            CommunicationException, JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(VersionResponse.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return (VersionResponse) jaxbUnmarshaller.unmarshal(in);
    }

}
