package org.jboss.da.communication.pom;

import org.jboss.da.communication.pom.model.MavenProject;
import org.slf4j.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Optional;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import org.jboss.da.communication.pom.impl.NamespaceFilter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderFactory;

@ApplicationScoped
public class PomReader {

    @Inject
    private Logger log;

    public Optional<MavenProject> analyze(File pomFile) {
        try {
            Source source = filterNamespace(new InputSource(new FileInputStream(pomFile)));
            return Optional.of(unmarshal(source));
        } catch (JAXBException | SAXException | FileNotFoundException e) {
            log.error("Exception parsing the pom.xml: " + pomFile, e);
            return Optional.empty();
        }
    }

    public Optional<MavenProject> analyze(InputStream pom) {
        try {
            Source source = filterNamespace(new InputSource(pom));
            return Optional.of(unmarshal(source));
        } catch (JAXBException | SAXException e) {
            log.error("Exception parsing the pom.xml from stream.", e);
            return Optional.empty();
        }
    }

    private Source filterNamespace(InputSource is) throws SAXException {
        NamespaceFilter nf = new NamespaceFilter(MavenProject.NAMESPACE);
        nf.setParent(XMLReaderFactory.createXMLReader());
        return new SAXSource(nf, is);
    }

    private MavenProject unmarshal(Source source) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(MavenProject.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (MavenProject) unmarshaller.unmarshal(source);
    }
}
