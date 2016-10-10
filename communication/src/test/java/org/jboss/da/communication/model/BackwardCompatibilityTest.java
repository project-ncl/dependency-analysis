package org.jboss.da.communication.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.jboss.da.communication.aprox.model.Repository;
import org.jboss.da.communication.aprox.model.VersionResponse;
import org.jboss.da.communication.aprox.model.Versioning;
import org.jboss.da.communication.aprox.model.Versions;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.json.JSONException;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.junit.Assert.fail;

/**
 *
 * @author Stanislav Knot <sknot@redhat.com>
 */
public class BackwardCompatibilityTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testRepository() throws IOException {
        Repository repository = new Repository();

        StringWriter actual = new StringWriter();
        mapper.writeValue(actual, repository);
        File expectedResponseFile = getJsonResponseFile(
                "src/test/resources/backwardCompatibilityTest", "Repository");
        assertEqualsJson(actual.toString(), readFileToString(expectedResponseFile));
    }

    @Test
    public void testVersionResponse() throws IOException {
        VersionResponse verResponse = new VersionResponse();
        StringWriter actual = new StringWriter();
        mapper.writeValue(actual, verResponse);

        File expectedResponseFile = getJsonResponseFile(
                "src/test/resources/backwardCompatibilityTest", "VersionResponse");
        assertEqualsJson(actual.toString(), readFileToString(expectedResponseFile));
    }

    @Test
    public void testVersioning() throws IOException, NoSuchFieldException {
        Versioning verisoning = new Versioning();
        StringWriter actual = new StringWriter();
        mapper.writeValue(actual, verisoning);

        File expectedResponseFile = getJsonResponseFile(
                "src/test/resources/backwardCompatibilityTest", "Versioning");
        assertEqualsJson(actual.toString(), readFileToString(expectedResponseFile));
    }

    @Test
    public void testVersions() throws IOException, NoSuchFieldException {
        Versions versions = new Versions();
        StringWriter actual = new StringWriter();
        mapper.writeValue(actual, versions);

        File expectedResponseFile = getJsonResponseFile(
                "src/test/resources/backwardCompatibilityTest", "Versions");
        assertEqualsJson(actual.toString(), readFileToString(expectedResponseFile));
    }

    protected void assertEqualsJson(String expected, String actual) {
        try {
            JSONAssert.assertEquals(expected, actual, JSONCompareMode.NON_EXTENSIBLE);
        } catch (JSONException ex) {
            fail("The test wasn't able to compare JSON strings" + ex);
        }
    }

    protected File getJsonResponseFile(String path, String variant) {
        return new ExpectedResponseFilenameBuilder(Paths.get(""), path, variant).getFile();
    }

    protected static class ExpectedResponseFilenameBuilder {

        protected static final String DEFAULT_VARIANT = "";

        private final Path folder;

        private final String path;

        private final String variant;

        public ExpectedResponseFilenameBuilder(Path folder, String path, String variant) {
            this.folder = folder;
            this.path = path;
            this.variant = variant;
        }

        public File getFile() {
            return getPath().toFile();
        }

        public Path getPath() {
            return folder.resolve(convertPath(path) + convertVariant(variant) + ".json");
        }

        private String convertVariant(String path) {
            return variant.equals(DEFAULT_VARIANT) ? "" : "/" + variant;
        }

        private String convertPath(String path) {
            String convertedPath = path.endsWith("/") ? path.substring(0, path.length() - 1)
                    + "index" : path;
            return convertedPath.startsWith("/") ? convertedPath.substring(1) : convertedPath;
        }

    }

}