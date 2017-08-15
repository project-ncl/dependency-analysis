package org.jboss.da.bc.model.rest;

import java.io.IOException;
import java.io.StringWriter;

import com.fasterxml.jackson.databind.ObjectMapper;

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
    public void testBuildConfiguration() throws IOException {
        BuildConfiguration bc = new BuildConfiguration();
        bc.setAnalysisStatus(DependencyAnalysisStatus.ANALYSED);
        bc.setAvailableVersions(new ArrayList<>());
        bc.setBcId(0);
        bc.setBuildScript("buildScript");
        bc.setDependencies(new ArrayList<>());
        bc.setDescription("description");
        bc.setEnvironmentId(1);
        EnumSet<BcError> errors = EnumSet.of(BcError.NO_DEPENDENCY, BcError.NO_ENV_SELECTED);
        bc.setErrors(errors);
        bc.setExistingBCs(new ArrayList<>());
        bc.setGav(new GAV("g", "a", "v"));
        bc.setInternallyBuilt("internallyBuilt");
        bc.setName("name");
        bc.setProjectId(0);
        bc.setScmRevision("scmRevision");
        bc.setScmUrl("scmUrl");
        bc.setSelected(true);

        compare(bc, "BuildConfiguration");
    }

    @Test
    public void testEntryEntity() throws IOException {
        EntryEntity entity = new EntryEntity();
        entity.setId(1);
        entity.setPomPath("./pom.xml");
        entity.setProductVersion("4-redhat");
        entity.setRepositories(new ArrayList<>());
        entity.setScmRevision("scmRevision");
        entity.setScmUrl("scmUrl");

        compare(entity, "EntryEntity");
    }

    @Test
    public void testProductFinishResponse() throws IOException {
        ProductFinishResponse fResponse = new ProductFinishResponse();
        fResponse.setCreatedEntityId(1);
        fResponse.setEntity(new ProductInfoEntity());
        fResponse.setErrorType("Error");
        fResponse.setMessage("Error");
        fResponse.setSuccess(true);

        compare(fResponse, "ProductFinishResponse");
    }

    @Test
    public void testProductInfoEntity() throws IOException {
        ProductInfoEntity infoEntity = new ProductInfoEntity();
        infoEntity.setBcSetName("bcSetName");
        infoEntity.setId(1);
        infoEntity.setPomPath("./pom.xml");
        infoEntity.setProductVersion("4-redhat");
        infoEntity.setSecurityToken("qwertzuiop");
        infoEntity.setTopLevelBc(new BuildConfiguration());

        compare(infoEntity, "ProductInfoEntity");
    }

    @Test
    public void testProjectFinishResponse() throws IOException {
        ProjectFinishResponse fResponse = new ProjectFinishResponse();
        fResponse.setCreatedEntityId(1);
        fResponse.setEntity(new ProjectInfoEntity());
        fResponse.setErrorType("Error");
        fResponse.setMessage("error");
        fResponse.setSuccess(true);

        compare(fResponse, "ProjectFinishResponse");
    }

    @Test
    public void testProjectInfoEntity() throws IOException {
        ProjectInfoEntity infoEntity = new ProjectInfoEntity();
        infoEntity.setBcSetName("name");
        infoEntity.setId(1);
        infoEntity.setPomPath("./pom.xml");
        infoEntity.setSecurityToken("qwertzuiop");
        infoEntity.setTopLevelBc(new BuildConfiguration());

        compare(infoEntity, "ProjectInfoEntity");
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
