package org.jboss.da.communication.pom;

import org.jboss.da.communication.pom.model.MavenProject;
import org.slf4j.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.InputStream;
import java.util.Optional;

@ApplicationScoped
public class PomReader {

    @Inject
    private Logger log;

    public Optional<MavenProject> analyze(File pomFile) {
        try {
            return Optional.of((MavenProject) getUnmarshaller().unmarshal(pomFile));
        } catch (JAXBException e) {
            log.error("Exception parsing the pom.xml: " + pomFile, e);
            return Optional.empty();
        }
    }

    public Optional<MavenProject> analyze(InputStream pom) {
        try {
            return Optional.of((MavenProject) getUnmarshaller().unmarshal(pom));
        } catch (JAXBException e) {
            log.error("Exception parsing the pom.xml from stram.", e);
            return Optional.empty();
        }
    }

    private Unmarshaller getUnmarshaller() throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(MavenProject.class);
        return jaxbContext.createUnmarshaller();
    }
}
