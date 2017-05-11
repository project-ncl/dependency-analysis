package org.jboss.da.reports.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import static org.apache.commons.io.FileUtils.readFileToString;
import org.jboss.da.listings.api.model.ProductVersion;
import org.jboss.da.model.rest.GAV;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.json.JSONException;

/**
 *
 * @author Stanislav Knot <sknot@redhat.com>
 */
public class BackwardCompatibilityTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testArtifactReport() throws IOException {
        ArtifactReport artifactReport = new ArtifactReport(new GAV("g", "a", "v"));
        artifactReport.setBlacklisted(true);
        artifactReport.setWhitelisted(new ArrayList<ProductVersion>());
        StringWriter actual = new StringWriter();
        mapper.writeValue(actual, artifactReport);

        File expectedResponseFile = getJsonResponseFile(
                "src/test/resources/backwardCompatibilityTest", "ArtifactReport");
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
