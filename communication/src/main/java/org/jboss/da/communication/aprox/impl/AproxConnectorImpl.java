package org.jboss.da.communication.aprox.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

@ApplicationScoped
public class AproxConnectorImpl implements AproxConnector {

    private Configuration config = new Configuration();

    @Override
    public GAVDependencyTree getDependencyTreeOfRevision(String scmUrl, String revision,
            String version) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GAVDependencyTree getDependencyTreeOfGAV(GAV gav) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getVersionsOfGA(GA ga) {
        StringBuilder query = new StringBuilder();
        File tempFile = null;
        try {
            query.append(config.getConfig().getAproxServer());
            query.append(ga.getGroupId().replace(".", "/"));
            query.append("/" + ga.getArtifactId() + "/maven-metadata.xml");

            URLConnection connection = new URL(query.toString()).openConnection();
            tempFile = File.createTempFile("metadata", "xml");

            tempFile = readStream(tempFile, connection);

            return parseMetadataFile(tempFile).getVersioning().getVersions().getVersion();
        } catch (IOException | ConfigurationParseException e) {
            e.printStackTrace();
        } finally {
            tempFile.delete();
        }
        return null;
    }

    private File readStream(File f, URLConnection connection) {
        try (InputStream in = connection.getInputStream();
                OutputStream out = new FileOutputStream(f)) {

            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = in.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return f;
    }

    private VersionResponse parseMetadataFile(File metadata) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(VersionResponse.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (VersionResponse) jaxbUnmarshaller
                    .unmarshal(metadata);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return null;
    }
}
