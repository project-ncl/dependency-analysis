package org.jboss.da.reports.model.response;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.jboss.da.lookup.model.MavenLookupResult;
import org.jboss.da.lookup.model.NPMLookupResult;
import org.jboss.da.reports.model.request.VersionsNPMRequest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.jboss.da.reports.model.request.VersionsNPMRequest.VersionFilter.MAJOR_MINOR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DeserializationTest {

    private static final String EXPECTED_PATH = "src/test/resources/deserializeTest";

    @Test
    public void deserializeLookupReport() throws IOException {

        Path fileJson = getJsonResponseFile(EXPECTED_PATH, "mavenLookupReport");
        String content = Files.lines(fileJson).collect(Collectors.joining());

        ObjectMapper mapper = new ObjectMapper();
        LookupReport reported = mapper.readValue(content, LookupReport.class);

        assertNotNull(reported.getGav());
        assertEquals("xom", reported.getGav().getArtifactId());
        assertEquals("1.2.5", reported.getGav().getVersion());
    }

    @Test
    public void deserializeLookupResult() throws IOException {

        Path fileJson = getJsonResponseFile(EXPECTED_PATH, "mavenLookupResult");
        String content = Files.lines(fileJson).collect(Collectors.joining());

        ObjectMapper mapper = new ObjectMapper();
        MavenLookupResult reported = mapper.readValue(content, MavenLookupResult.class);

        assertNotNull(reported.getGav());
        assertEquals("xom", reported.getGav().getArtifactId());
        assertEquals("1.2.5", reported.getGav().getVersion());
    }

    @Test
    public void deserializeNPMLookupReport() throws IOException {

        Path fileJson = getJsonResponseFile(EXPECTED_PATH, "npmLookupReport");
        String content = Files.lines(fileJson).collect(Collectors.joining());

        ObjectMapper mapper = new ObjectMapper();
        NPMLookupReport reported = mapper.readValue(content, NPMLookupReport.class);
        assertNotNull(reported.getNpmPackage());
        assertEquals("1.2.3", reported.getNpmPackage().getVersion());
    }

    @Test
    public void deserializeNPMLookupResult() throws IOException {

        Path fileJson = getJsonResponseFile(EXPECTED_PATH, "npmLookupResult");
        String content = Files.lines(fileJson).collect(Collectors.joining());

        ObjectMapper mapper = new ObjectMapper();
        NPMLookupResult reported = mapper.readValue(content, NPMLookupResult.class);
        assertNotNull(reported.getNpmPackage());
        assertEquals("1.2.3", reported.getNpmPackage().getVersion());
    }

    @Test
    public void deserializeVersionsNPMRequest() throws IOException {
        Path fileJson = getJsonResponseFile(EXPECTED_PATH, "versionsNPMRequest");
        String content = Files.lines(fileJson).collect(Collectors.joining());

        ObjectMapper mapper = new ObjectMapper();
        VersionsNPMRequest request = mapper.readValue(content, VersionsNPMRequest.class);
        assertEquals("FOO", request.getMode());
        assertEquals(MAJOR_MINOR, request.getVersionFilter());
        assertEquals(1, request.getPackages().size());
        assertEquals("abab", request.getPackages().get(0).getName());
        assertEquals("1.2.3", request.getPackages().get(0).getVersion());
        assertFalse(request.isIncludeAll());
    }

    protected Path getJsonResponseFile(String path, String variant) {
        return Paths.get(path, variant + ".json");
    }
}