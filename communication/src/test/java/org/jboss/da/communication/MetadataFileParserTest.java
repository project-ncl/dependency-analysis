package org.jboss.da.communication;

import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.aprox.impl.MetadataFileParser;
import org.jboss.da.communication.aprox.model.VersionResponse;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class MetadataFileParserTest {

    private static VersionResponse versionResponse;

    @BeforeClass
    public static void init() throws JAXBException, IOException, CommunicationException {
        try (InputStream in = getResourceSteam("maven-metadata.xml")) {
            versionResponse = MetadataFileParser.parseMavenMetadata(in);
        }
    }

    @Test
    public void availableVersionsTest() throws JAXBException, IOException, CommunicationException {
        // given
        List<String> availableVersions = versionResponse.getVersioning().getVersions().getVersion();

        // expect
        Assert.assertTrue("Missing version.", availableVersions.contains("1.1.0.Beta1"));
        Assert.assertTrue("Missing version.", availableVersions.contains("1.4.1.Final-redhat-1"));
        Assert.assertTrue("Missing version.", availableVersions.contains("1.4.1.Final-redhat-2"));
        Assert.assertTrue("Missing version.", availableVersions.contains("1.4.2.Final"));
        Assert.assertTrue("Missing version.", availableVersions.contains("1.5.1.Final-redhat-4"));
        Assert.assertFalse("Version should not be present.", availableVersions.contains("1.5.1.Final-redhat-10"));
    }

    @Test
    public void latestTest() throws JAXBException, IOException, CommunicationException {
        // given
        String latestVersion = versionResponse.getVersioning().getLatestVersion();

        // expect
        Assert.assertEquals("1.4.2.Final", latestVersion);
    }

    @Test
    public void releaseTest() throws JAXBException, IOException, CommunicationException {
        // given
        String latestReleaseVersion = versionResponse.getVersioning().getLatestRelease();

        // expect
        Assert.assertEquals("1.4.1.Final-redhat-2", latestReleaseVersion);
    }

    private static InputStream getResourceSteam(String file) {
        return MetadataFileParserTest.class.getClassLoader().getResourceAsStream(file);
    }
}
