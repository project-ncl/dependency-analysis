package org.jboss.da.communication.model;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Paths;

import org.jboss.da.communication.aprox.model.VersionResponse;
import org.jboss.da.communication.aprox.model.Versioning;
import org.jboss.da.communication.aprox.model.Versions;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.json.JSONException;
import static org.junit.Assert.fail;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

/**
 *
 * @author Stanislav Knot <sknot@redhat.com>
 */
public class BackwardCompatibilityTest {

    private static final String EXPECETD_PATH = "src/test/resources/backwardCompatibilityTest";

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testVersionResponse() throws IOException {
        VersionResponse verResponse = new VersionResponse();

        compare(verResponse, "VersionResponse");
    }

    @Test
    public void testVersioning() throws IOException, NoSuchFieldException {
        Versioning verisoning = new Versioning();

        compare(verisoning, "Versioning");
    }

    @Test
    public void testVersions() throws IOException, NoSuchFieldException {
        Versions versions = new Versions();

        compare(versions, "Versions");
    }

    protected void assertEqualsJson(String expected, String actual) {
        try {
            JSONAssert.assertEquals(expected, actual, JSONCompareMode.NON_EXTENSIBLE);
        } catch (JSONException ex) {
            fail("The test wasn't able to compare JSON strings" + ex);
        }
    }

    private void compare(Object obj, String expectedFile) throws IOException {
        StringWriter actual = new StringWriter();
        mapper.writeValue(actual, obj);
        Path expectedResponseFile = getJsonResponseFile(EXPECETD_PATH, expectedFile);
        String expected = Files.lines(expectedResponseFile).collect(Collectors.joining());
        assertEqualsJson(expected, actual.toString());
    }

    protected Path getJsonResponseFile(String path, String variant) {
        return Paths.get(path, variant + ".json");
    }

}