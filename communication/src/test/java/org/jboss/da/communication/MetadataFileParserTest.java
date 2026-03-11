package org.jboss.da.communication;

import org.jboss.da.communication.indy.impl.MetadataFileParser;
import org.jboss.da.communication.indy.model.VersionResponse;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.xml.bind.JAXBException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class MetadataFileParserTest {

    private static VersionResponse versionResponse;

    @BeforeAll
    public static void init() throws JAXBException, IOException {
        try (InputStream in = getResourceSteam("maven-metadata.xml")) {
            versionResponse = MetadataFileParser.parseMavenMetadata(in);
        }
    }

    @Test
    public void availableVersionsTest() {
        // given
        List<String> availableVersions = versionResponse.getVersioning().getVersions().getVersion();

        // expect
        assertTrue(availableVersions.contains("1.1.0.Beta1"), "Missing version.");
        assertTrue(availableVersions.contains("1.4.1.Final-redhat-1"), "Missing version.");
        assertTrue(availableVersions.contains("1.4.1.Final-redhat-2"), "Missing version.");
        assertTrue(availableVersions.contains("1.4.2.Final"), "Missing version.");
        assertTrue(availableVersions.contains("1.5.1.Final-redhat-4"), "Missing version.");
        assertFalse(availableVersions.contains("1.5.1.Final-redhat-10"), "Version should not be present.");
    }

    @Test
    public void latestTest() {
        // given
        String latestVersion = versionResponse.getVersioning().getLatestVersion();

        // expect
        assertEquals("1.4.2.Final", latestVersion);
    }

    @Test
    public void releaseTest() {
        // given
        String latestReleaseVersion = versionResponse.getVersioning().getLatestRelease();

        // expect
        assertEquals("1.4.1.Final-redhat-2", latestReleaseVersion);
    }

    private static InputStream getResourceSteam(String file) {
        return MetadataFileParserTest.class.getClassLoader().getResourceAsStream(file);
    }
}
