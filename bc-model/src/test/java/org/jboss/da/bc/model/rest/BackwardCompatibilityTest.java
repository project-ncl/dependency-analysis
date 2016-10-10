package org.jboss.da.bc.model.rest;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import org.jboss.da.bc.model.BcError;
import org.jboss.da.bc.model.DependencyAnalysisStatus;
import org.jboss.da.model.rest.GAV;
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
    public void testBuildConfiguration() throws IOException {
        BuildConfiguration bc = new BuildConfiguration();
        bc.setAnalysisStatus(DependencyAnalysisStatus.ANALYSED);
        bc.setAvailableVersions(new ArrayList<String>());
        bc.setBcId(0);
        bc.setBuildScript("buildScript");
        bc.setDependencies(new ArrayList<BuildConfiguration>());
        bc.setDescription("description");
        bc.setEnvironmentId(1);
        EnumSet<BcError> errors = EnumSet.of(BcError.NO_DEPENDENCY, BcError.NO_ENV_SELECTED);
        bc.setErrors(errors);
        bc.setExistingBCs(new ArrayList<Integer>());
        bc.setGav(new GAV("g", "a", "v"));
        bc.setInternallyBuilt("internallyBuilt");
        bc.setName("name");
        bc.setProjectId(0);
        bc.setScmRevision("scmRevision");
        bc.setScmUrl("scmUrl");
        bc.setSelected(true);

        StringWriter actual = new StringWriter();
        mapper.writeValue(actual, bc);
        File expectedResponseFile = getJsonResponseFile(
                "src/test/resources/backwardCompatibilityTest", "BuildConfiguration");
        assertEqualsJson(actual.toString(), readFileToString(expectedResponseFile));
    }

    @Test
    public void testEntryEntity() throws IOException {
        EntryEntity entity = new EntryEntity();
        entity.setId(1);
        entity.setPomPath("./pom.xml");
        entity.setProductVersion("4-redhat");
        entity.setRepositories(new ArrayList<String>());
        entity.setScmRevision("scmRevision");
        entity.setScmUrl("scmUrl");

        StringWriter actual = new StringWriter();
        mapper.writeValue(actual, entity);
        File expectedResponseFile = getJsonResponseFile(
                "src/test/resources/backwardCompatibilityTest", "EntryEntity");
        assertEqualsJson(actual.toString(), readFileToString(expectedResponseFile));
    }

    @Test
    public void testProductFinishResponse() throws IOException {
        ProductFinishResponse fResponse = new ProductFinishResponse();
        fResponse.setCreatedEntityId(1);
        fResponse.setEntity(new ProductInfoEntity());
        fResponse.setErrorType("Error");
        fResponse.setMessage("Error");
        fResponse.setSuccess(true);

        StringWriter actual = new StringWriter();
        mapper.writeValue(actual, fResponse);
        File expectedResponseFile = getJsonResponseFile(
                "src/test/resources/backwardCompatibilityTest", "ProductFinishResponse");
        assertEqualsJson(actual.toString(), readFileToString(expectedResponseFile));
    }

    @Test
    public void testProductInfoEntity() throws IOException {
        ProductInfoEntity infoEntity = new ProductInfoEntity();
        infoEntity.setBcSetName("bcSetName");
        infoEntity.setId(1);
        infoEntity.setPomPath("./pom.xml");
        infoEntity.setProductVersion("4-redhat");
        infoEntity.setTopLevelBc(new BuildConfiguration());

        StringWriter actual = new StringWriter();
        mapper.writeValue(actual, infoEntity);
        File expectedResponseFile = getJsonResponseFile(
                "src/test/resources/backwardCompatibilityTest", "ProductInfoEntity");
        assertEqualsJson(actual.toString(), readFileToString(expectedResponseFile));
    }

    @Test
    public void testProjectFinishResponse() throws IOException {
        ProjectFinishResponse fResponse = new ProjectFinishResponse();
        fResponse.setCreatedEntityId(1);
        fResponse.setEntity(new ProjectInfoEntity());
        fResponse.setErrorType("Error");
        fResponse.setMessage("error");
        fResponse.setSuccess(true);

        StringWriter actual = new StringWriter();
        mapper.writeValue(actual, fResponse);
        File expectedResponseFile = getJsonResponseFile(
                "src/test/resources/backwardCompatibilityTest", "ProjectFinishResponse");
        assertEqualsJson(actual.toString(), readFileToString(expectedResponseFile));
    }

    @Test
    public void testProjectInfoEntity() throws IOException {
        ProjectInfoEntity infoEntity = new ProjectInfoEntity();
        infoEntity.setBcSetName("name");
        infoEntity.setId(1);
        infoEntity.setPomPath("./pom.xml");
        infoEntity.setTopLevelBc(new BuildConfiguration());

        StringWriter actual = new StringWriter();
        mapper.writeValue(actual, infoEntity);
        File expectedResponseFile = getJsonResponseFile(
                "src/test/resources/backwardCompatibilityTest", "ProjectInfoEntity");
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
