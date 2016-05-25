package org.jboss.da.reports.impl;

import org.jboss.da.common.CommunicationException;
import org.jboss.da.common.version.VersionParser;
import org.jboss.da.communication.aprox.impl.MetadataFileParser;
import org.jboss.da.model.rest.GAV;
import org.jboss.da.reports.backend.impl.VersionFinderImpl;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class VersionFinderTest {

    @Test
    public void bestMatchVersionForTest() throws JAXBException, IOException, CommunicationException {
        //given
        VersionParser versionParser = new VersionParser();
        VersionFinderImpl versionFinder = new VersionFinderImpl(
                LoggerFactory.getLogger(VersionParser.class),
                null,
                versionParser
            );
        GAV gav = new GAV("org.jboss.modules", "jboss-modules", "1.4.1.Final");
        InputStream in = getResourceSteam("maven-metadata.xml");

        //when
        List<String> availableVersions = MetadataFileParser.parseMetadataFile(in)
                .getVersioning().getVersions().getVersion();

        Optional<String> bestMatchVersion = versionFinder.getBestMatchVersionFor(gav, availableVersions);

        //expect
        Assert.assertTrue("Missing version.", availableVersions.contains("1.4.1.Final-redhat-1"));
        Assert.assertTrue("Missing version.", availableVersions.contains("1.4.1.Final-redhat-2"));
        Assert.assertTrue("Missing version.", availableVersions.contains("1.5.1.Final-redhat-4"));

        Assert.assertNotNull(bestMatchVersion.get());
        Assert.assertEquals("", "1.4.1.Final-redhat-2", bestMatchVersion.get());
    }

    private InputStream getResourceSteam(String file) {
        return getClass().getClassLoader().getResourceAsStream(file);
    }
}
