package org.jboss.da.communication.aprox.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import javax.ejb.Stateless;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.jboss.da.communication.aprox.api.AproxConnector;
import org.jboss.da.communication.aprox.model.GA;
import org.jboss.da.communication.aprox.model.GAV;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;
import org.jboss.da.communication.aprox.model.VersionResponse;

@Stateless
public class AproxConnectorImpl implements AproxConnector {

    public GAVDependencyTree getDependencyTreeOfRevision(String scmUrl, String revision,
            String version) {
        // TODO Auto-generated method stub
        return null;
    }

    public GAVDependencyTree getDependencyTreeOfGAV(GAV gav) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<String> getVersionsOfGA(GA ga) {
        StringBuilder query = new StringBuilder("http://10.3.8.115/api/group/public/"); // update with appropriate aprox group
        query.append(ga.getGroupId().replace(".", "/")); // append groupId
        query.append("/" + ga.getArtifactId() + "/maven-metadata.xml");
        File tempFile = null;
        try {
            URLConnection connection = new URL(query.toString()).openConnection();
            tempFile = File.createTempFile("metadata", "xml");

            tempFile = readStream(tempFile, connection);

            JAXBContext jaxbContext = JAXBContext.newInstance(VersionResponse.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            VersionResponse versionResponse = (VersionResponse) jaxbUnmarshaller
                    .unmarshal(tempFile);
            return versionResponse.getVersioning().getVersions().getVersion();
        } catch (IOException | JAXBException e) {
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
}
