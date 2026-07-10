package org.jboss.da.communication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.xml.bind.JAXBException;

import org.jboss.da.communication.repository.MetadataFileParser;
import org.jboss.da.communication.repository.model.VersionResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import io.quarkus.test.junit.QuarkusTest;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MetadataFileParserTest {

    @Inject
    MetadataFileParser parser;

    private VersionResponse versionResponse;

    @BeforeAll
    public void init() throws JAXBException, IOException {
        try (InputStream in = getResourceSteam("maven-metadata.xml")) {
            versionResponse = parser.parseMavenMetadata(in);
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
