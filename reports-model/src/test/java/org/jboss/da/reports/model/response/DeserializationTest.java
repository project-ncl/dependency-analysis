package org.jboss.da.reports.model.response;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jboss.da.lookup.model.MavenLookupResult;
import org.jboss.da.lookup.model.NPMLookupResult;
import org.jboss.da.reports.model.request.VersionsNPMRequest;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import static org.assertj.core.api.Assertions.*;
import static org.jboss.da.reports.model.request.VersionsNPMRequest.VersionFilter.MAJOR_MINOR;

public class DeserializationTest {

    private static final String EXPECTED_PATH = "src/test/resources/deserializeTest";

    @Test
    public void deserializeLookupReport() throws IOException {

        Path fileJson = getJsonResponseFile(EXPECTED_PATH, "mavenLookupReport");
        String content = Files.lines(fileJson).collect(Collectors.joining());

        ObjectMapper mapper = new ObjectMapper();
        LookupReport reported = mapper.readValue(content, LookupReport.class);

        assertThat(reported.getGav()).isNotNull();
        assertThat(reported.getGav().getArtifactId()).isEqualTo("xom");
        assertThat(reported.getGav().getVersion()).isEqualTo("1.2.5");
    }

    @Test
    public void deserializeLookupResult() throws IOException {

        Path fileJson = getJsonResponseFile(EXPECTED_PATH, "mavenLookupResult");
        String content = Files.lines(fileJson).collect(Collectors.joining());

        ObjectMapper mapper = new ObjectMapper();
        MavenLookupResult reported = mapper.readValue(content, MavenLookupResult.class);

        assertThat(reported.getGav()).isNotNull();
        assertThat(reported.getGav().getArtifactId()).isEqualTo("xom");
        assertThat(reported.getGav().getVersion()).isEqualTo("1.2.5");
    }

    @Test
    public void deserializeNPMLookupReport() throws IOException {

        Path fileJson = getJsonResponseFile(EXPECTED_PATH, "npmLookupReport");
        String content = Files.lines(fileJson).collect(Collectors.joining());

        ObjectMapper mapper = new ObjectMapper();
        NPMLookupReport reported = mapper.readValue(content, NPMLookupReport.class);
        assertThat(reported.getNpmPackage()).isNotNull();
        assertThat(reported.getNpmPackage().getVersion()).isEqualTo("1.2.3");
    }

    @Test
    public void deserializeNPMLookupResult() throws IOException {

        Path fileJson = getJsonResponseFile(EXPECTED_PATH, "npmLookupResult");
        String content = Files.lines(fileJson).collect(Collectors.joining());

        ObjectMapper mapper = new ObjectMapper();
        NPMLookupResult reported = mapper.readValue(content, NPMLookupResult.class);
        assertThat(reported.getNpmPackage()).isNotNull();
        assertThat(reported.getNpmPackage().getVersion()).isEqualTo("1.2.3");
    }

    @Test
    public void deserializeVersionsNPMRequest() throws IOException {
        Path fileJson = getJsonResponseFile(EXPECTED_PATH, "versionsNPMRequest");
        String content = Files.lines(fileJson).collect(Collectors.joining());

        ObjectMapper mapper = new ObjectMapper();
        VersionsNPMRequest request = mapper.readValue(content, VersionsNPMRequest.class);
        assertThat(request.getMode()).isEqualTo("FOO");
        assertThat(request.getVersionFilter()).isEqualTo(MAJOR_MINOR);
        assertThat(request.getPackages()).hasSize(1);
        assertThat(request.getPackages().get(0).getName()).isEqualTo("abab");
        assertThat(request.getPackages().get(0).getVersion()).isEqualTo("1.2.3");
        assertThat(request.isIncludeAll()).isFalse();
    }

    protected Path getJsonResponseFile(String path, String variant) {
        return Paths.get(path, variant + ".json");
    }
}