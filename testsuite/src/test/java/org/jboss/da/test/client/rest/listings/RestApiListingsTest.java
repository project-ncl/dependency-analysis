package org.jboss.da.test.client.rest.listings;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class RestApiListingsTest extends AbstractRestApiListingTest {

    private final RequestGenerator generator = new RequestGenerator();

    @Test
    public void testAddProduct() throws Exception {
        Response response = manipulateEntityFile(ListEntityType.PRODUCT, OperationType.POST, "productAdd", true);

        checkExpectedResponse(response, "success");
    }

    @Test
    public void testChangeProduct() throws Exception {
        Response response = manipulateEntityFile(ListEntityType.PRODUCT, OperationType.POST, "productAdd", true);

        checkExpectedResponse(response, "success");

        manipulateEntityFile(ListEntityType.PRODUCT, OperationType.PUT, "productChangeStatus", true);
    }

    @Test
    public void testDeleteProduct() throws Exception {
        Response response = manipulateEntityFile(ListEntityType.PRODUCT, OperationType.POST, "productAdd", true);

        checkExpectedResponse(response, "success");

        response = manipulateEntityFile(ListEntityType.PRODUCT, OperationType.DELETE, "productAdd", true);

        checkExpectedResponse(response, "success");
    }

    @Test
    public void testAddBlackArtifact() throws Exception {
        Response response = manipulateEntityFile(ListEntityType.BLACK, OperationType.POST, "gav", true);

        checkExpectedResponse(response, "success");
    }

    @Test
    public void testDeleteBlackArtifact() throws Exception {
        // add artifact
        manipulateEntityFile(ListEntityType.BLACK, OperationType.POST, "gav", true);

        // delete artifact
        Response response = manipulateEntityFile(ListEntityType.BLACK, OperationType.DELETE, "gav", true);

        checkExpectedResponse(response, "success");
    }

    @Test
    public void shouldBlackListSpecificRedhatBuild() throws Exception {
        String g = "org.jboss.da";
        String a = "dependency-analyzer";
        String v = "0.3.0";
        manipulateEntityFile(ListEntityType.BLACK, OperationType.POST, "gavRh", true);

        Response response = getBlacklistedGAV(g, a, v);
        assertEquals(404, response.getStatus());
        response = getBlacklistedGAV(g, a, v + ".redhat-2");
        assertEquals(404, response.getStatus());
        response = getBlacklistedGAV(g, a, v + ".redhat-1");
        assertEquals(200, response.getStatus());
        checkExpectedResponse(response, "gavRhNonOSGiResponse");

    }

    @Test
    public void shouldBlacklistWholeVersions() throws Exception { //
        String g = "org.jboss.da";
        String a = "dependency-analyzer";
        String v = "0.3.0";
        manipulateEntityFile(ListEntityType.BLACK, OperationType.POST, "gav", true);

        Response response = getBlacklistedGAV(g, a, v + "-redhat-1");
        assertEquals(200, response.getStatus());
        checkExpectedResponse(response, "gavNonRhResponse");
        response = getBlacklistedGAV(g, a, v);
        assertEquals(200, response.getStatus());
        checkExpectedResponse(response, "gavNonRhResponse");
        response = getBlacklistedGAV(g, a, v + "-redhat-2");
        assertEquals(200, response.getStatus());
        checkExpectedResponse(response, "gavNonRhResponse");
    }

    @Test
    public void shouldUnBlacklistWithoutAndWithRHSuffix() throws Exception {
        String g = "org.jboss.da";
        String a = "dependency-analyzer";
        String v = "0.3.0";
        manipulateEntityFile(ListEntityType.BLACK, OperationType.POST, "gavRh", true);
        manipulateEntityFile(ListEntityType.BLACK, OperationType.POST, "gav", true);
        Response response = getBlacklistedGAV(g, a, v);
        checkExpectedResponse(response, "gavNonRhResponse");
        response = getBlacklistedGAV(g, a, v + "-redhat-1");
        checkExpectedResponse(response, "gavNonRhResponse");
        response = getBlacklistedGAV(g, a, v + "-redhat-2");
        checkExpectedResponse(response, "gavNonRhResponse");

        manipulateEntityFile(ListEntityType.BLACK, OperationType.DELETE, "gav", true);
        response = getBlacklistedGAV(g, a, v);
        assertEquals(404, response.getStatus());
        response = getBlacklistedGAV(g, a, v + ".redhat-2");
        assertEquals(404, response.getStatus());
        response = getBlacklistedGAV(g, a, v + ".redhat-1");
        assertEquals(200, response.getStatus());
        checkExpectedResponse(response, "gavRhNonOSGiResponse");

        manipulateEntityFile(ListEntityType.BLACK, OperationType.DELETE, "gavRh", true);
        response = getBlacklistedGAV(g, a, v);
        assertEquals(404, response.getStatus());
        response = getBlacklistedGAV(g, a, v + ".redhat-2");
        assertEquals(404, response.getStatus());
        response = getBlacklistedGAV(g, a, v + ".redhat-1");
        assertEquals(404, response.getStatus());
    }

    @Test
    public void testDeleteNonExistingBlackArtifact() throws Exception {
        Response response = manipulateEntityFile(ListEntityType.BLACK, OperationType.DELETE, "gav", true);

        checkExpectedResponse(response, "successFalse");
    }

    @Test
    public void testAlreadyAddedBlackArtifact() throws Exception {
        // add first black artifact
        manipulateEntityFile(ListEntityType.BLACK, OperationType.POST, "gav", true);

        // add second black artifact
        Response response = manipulateEntityFile(ListEntityType.BLACK, OperationType.POST, "gav", true);

        checkExpectedResponse(response, "successFalse");
    }

    @Test
    public void testAddWhiteArtifact() throws Exception {
        Response response = manipulateEntityFile(ListEntityType.PRODUCT, OperationType.POST, "productAdd", true);

        checkExpectedResponse(response, "success");

        String artifact = generator.returnWhiteArtifactString(
                "org.jboss.da",
                "dependency-analyzer",
                "0.3.0",
                getIdOfProduct("test", "1.0.0"));

        response = manipulateEntityString(ListEntityType.WHITE, OperationType.POST, artifact, true);

        checkExpectedResponse(response, "success");
    }

    @Test
    public void testDeleteWhiteArtifact() throws Exception {
        Response response = manipulateEntityFile(ListEntityType.PRODUCT, OperationType.POST, "productAdd", true);

        checkExpectedResponse(response, "success");

        String artifact = generator.returnWhiteArtifactString(
                "org.jboss.da",
                "dependency-analyzer",
                "0.3.0",
                getIdOfProduct("test", "1.0.0"));

        response = manipulateEntityString(ListEntityType.WHITE, OperationType.POST, artifact, true);

        checkExpectedResponse(response, "success");

        // delete artifact
        response = manipulateEntityFile(ListEntityType.WHITE, OperationType.DELETE, "gav", true);

        checkExpectedResponse(response, "success");
    }

    @Test
    public void testDeleteNonExistingWhiteArtifact() throws Exception {
        Response response = manipulateEntityFile(ListEntityType.WHITE, OperationType.DELETE, "gav", true);

        checkExpectedResponse(response, "successFalse");
    }

    @Test
    public void testAddBlacklistedArtifactToWhitelist() throws Exception {
        // Add artifact to blacklist
        Response response = manipulateEntityFile(ListEntityType.BLACK, OperationType.POST, "gav", true);
        checkExpectedResponse(response, "success");

        // Try to add artifact to whitelist
        response = manipulateEntityFile(ListEntityType.PRODUCT, OperationType.POST, "productAdd", true);

        checkExpectedResponse(response, "success");

        String artifact = generator.returnWhiteArtifactString(
                "org.jboss.da",
                "dependency-analyzer",
                "0.3.0",
                getIdOfProduct("test", "1.0.0"));

        response = manipulateEntityString(ListEntityType.WHITE, OperationType.POST, artifact, false);

        assertEquals(200, response.getStatus());
    }

    @Test
    public void testAddWhitelistedArtifactToBlacklist() throws Exception {
        manipulateEntityFile(ListEntityType.PRODUCT, OperationType.POST, "productAdd", true);

        // Add artifact to whitelist
        String artifact = generator.returnWhiteArtifactString(
                "org.jboss.da",
                "dependency-analyzer",
                "0.3.0",
                getIdOfProduct("test", "1.0.0"));

        manipulateEntityString(ListEntityType.WHITE, OperationType.POST, artifact, true);

        // Add artifact to blacklist
        Response response = manipulateEntityFile(ListEntityType.BLACK, OperationType.POST, "gav", true);

        checkExpectedResponse(response, "successMessage");

        assertEquals(0, getAllArtifactsFromList(PATH_WHITE_LIST).size());
    }

    @Test
    public void testAddMultipleTimeWhitelistedArtifactToBlacklist() throws Exception {
        manipulateEntityFile(ListEntityType.PRODUCT, OperationType.POST, "productAdd", true);
        manipulateEntityFile(ListEntityType.PRODUCT, OperationType.POST, "productAdd2", true);

        // Add artifacts to whitelist
        String artifact;
        artifact = generator.returnWhiteArtifactString(
                "org.jboss.da",
                "dependency-analyzer",
                "0.3.0",
                getIdOfProduct("test", "1.0.0"));

        manipulateEntityString(ListEntityType.WHITE, OperationType.POST, artifact, true);

        artifact = generator.returnWhiteArtifactString(
                "org.jboss.da",
                "dependency-analyzer",
                "0.3.0",
                getIdOfProduct("test", "2.0.0"));

        manipulateEntityString(ListEntityType.WHITE, OperationType.POST, artifact, true);
        // Add artifact to blacklist
        Response response = manipulateEntityFile(ListEntityType.BLACK, OperationType.POST, "gav", true);

        checkExpectedResponse(response, "successMessage");
    }

    @Test
    public void testAlreadyAddedWhiteArtifact() throws Exception {
        manipulateEntityFile(ListEntityType.PRODUCT, OperationType.POST, "productAdd", true);

        String artifact = generator.returnWhiteArtifactString(
                "org.jboss.da",
                "dependency-analyzer",
                "0.3.0",
                getIdOfProduct("test", "1.0.0"));

        manipulateEntityString(ListEntityType.WHITE, OperationType.POST, artifact, true);

        // add second white artifact
        Response response = manipulateEntityString(ListEntityType.WHITE, OperationType.POST, artifact, true);

        checkExpectedResponse(response, "successFalse");
    }

    @Test
    public void testGetAllWhiteArtifacts() throws Exception {
        // Add artifacts to whitelist
        manipulateEntityFile(ListEntityType.PRODUCT, OperationType.POST, "productAdd", true);
        manipulateEntityFile(ListEntityType.PRODUCT, OperationType.POST, "productAdd2", true);

        // Add artifacts to whitelist
        String artifact;
        artifact = generator.returnWhiteArtifactString(
                "org.jboss.da",
                "dependency-analyzer",
                "0.3.0",
                getIdOfProduct("test", "1.0.0"));

        manipulateEntityString(ListEntityType.WHITE, OperationType.POST, artifact, true);

        artifact = generator.returnWhiteArtifactString(
                "org.jboss.da",
                "dependency-analyzer",
                "0.3.0",
                getIdOfProduct("test", "2.0.0"));

        manipulateEntityString(ListEntityType.WHITE, OperationType.POST, artifact, true);

        // Get list
        Response response = createClientRequest(PATH_WHITE_LIST).get();
        assertEquals(200, response.getStatus());

        checkExpectedResponse(response, "gavWhiteList");
    }

    @Test
    public void testGetAllBlackArtifacts() throws Exception {
        // Add artifacts to blacklist
        manipulateEntityFile(ListEntityType.BLACK, OperationType.POST, "gav", true);

        manipulateEntityFile(ListEntityType.BLACK, OperationType.POST, "gav2", true);

        // Get list
        Response response = createClientRequest(PATH_BLACK_LIST).get();
        assertEquals(200, response.getStatus());

        checkExpectedResponse(response, "gavBlackList");
    }

    @Test
    public void testGetGABlackArtifacts() throws Exception {
        // Check empty list
        checkExpectedResponse(getBlacklisted("foo", "bar"), "gaBlacklistEmpty");

        // Add artifacts
        manipulateEntityFile(ListEntityType.BLACK, OperationType.POST, "gavFoobar", true);
        manipulateEntityFile(ListEntityType.BLACK, OperationType.POST, "gavFoobaz-1", true);
        manipulateEntityFile(ListEntityType.BLACK, OperationType.POST, "gavFoobaz-2", true);
        manipulateEntityFile(ListEntityType.BLACK, OperationType.POST, "gavFoobarbaz", true);
        manipulateEntityFile(ListEntityType.BLACK, OperationType.POST, "gavFoobarbaz-4", true);

        // Check responses
        checkExpectedResponse(getBlacklisted("foo", "bar"), "gaBlacklistFoobar");
        checkExpectedResponse(getBlacklisted("foo", "baz"), "gaBlacklistFoobaz");
        checkExpectedResponse(getBlacklisted("foo", "bar-baz"), "gaBlacklistFoobarbaz");
    }

    @Test
    public void testCheckRHBlackArtifact() throws Exception {
        manipulateEntityFile(ListEntityType.BLACK, OperationType.POST, "gav", true);

        Response response = getBlacklisted("org.jboss.da", "dependency-analyzer", "0.3.0.redhat-1");

        checkExpectedResponse(response, "gavResponse");
    }

    private Response getBlacklistedGAV(String groupId, String artifactId, String version) {
        Response response = createClientRequest(
                PATH_BLACK_LISTINGS_GAV + "?groupid=" + groupId + "&artifactid=" + artifactId + "&version=" + version)
                        .get();
        return response;
    }

    private Response getBlacklisted(String groupId, String artifactId, String version) {
        Response response = createClientRequest(
                PATH_BLACK_LIST + "?groupid=" + groupId + "&artifactid=" + artifactId + "&version=" + version).get();
        assertEquals(200, response.getStatus());
        return response;
    }

    private Response getBlacklisted(String groupId, String artifactId) {
        Response response = createClientRequest(
                PATH_BLACK_LISTINGS_GA + "?groupid=" + groupId + "&artifactid=" + artifactId).get();
        assertEquals(200, response.getStatus());
        return response;
    }

    /**
     * Non RedHat but OSGi compliant black artifact test
     * 
     * @throws Exception
     */
    @Test
    public void testCheckNonRHBlackArtifact() throws Exception {
        manipulateEntityFile(ListEntityType.BLACK, OperationType.POST, "gav", true);

        Response response = getBlacklisted("org.jboss.da", "dependency-analyzer", "0.3.0");

        checkExpectedResponse(response, "gavResponse");
    }

    /**
     * Non RedHat non OSGi compliant black artifact test
     * 
     * @throws Exception
     */
    @Test
    public void testCheckNonRHNonOSGiBlackArtifact() throws Exception {
        manipulateEntityFile(ListEntityType.BLACK, OperationType.POST, "gav", true);

        Response response = getBlacklisted("org.jboss.da", "dependency-analyzer", "0.3");

        checkExpectedResponse(response, "gavResponse");
    }

    @Test
    public void testCheckRHWhiteArtifact() throws Exception {
        manipulateEntityFile(ListEntityType.PRODUCT, OperationType.POST, "productAdd", true);

        String artifact = generator.returnWhiteArtifactString(
                "org.jboss.da",
                "dependency-analyzer",
                "0.3.0.redhat-1",
                getIdOfProduct("test", "1.0.0"));

        manipulateEntityString(ListEntityType.WHITE, OperationType.POST, artifact, true);

        Response response = createClientRequest(
                PATH_WHITE_LIST + "?groupid=org.jboss.da&artifactid=dependency-analyzer&version=0.3.0.redhat-1").get();
        assertEquals(200, response.getStatus());

        checkExpectedResponse(response, "gavRhResponse");
    }

    /**
     * Non RedHat but OSGi compliant white artifact test
     * 
     * @throws Exception
     */
    @Test
    public void testCheckNonRHWhiteArtifact() throws Exception {
        manipulateEntityFile(ListEntityType.PRODUCT, OperationType.POST, "productAdd", true);

        String artifact = generator.returnWhiteArtifactString(
                "org.jboss.da",
                "dependency-analyzer",
                "0.3.0.redhat-1",
                getIdOfProduct("test", "1.0.0"));

        manipulateEntityString(ListEntityType.WHITE, OperationType.POST, artifact, true);

        Response response = createClientRequest(
                PATH_WHITE_LIST + "?groupid=org.jboss.da&artifactid=dependency-analyzer&version=0.3.0").get();
        assertEquals(200, response.getStatus());

        checkExpectedResponse(response, "gavRhResponse");
    }

    @Override
    protected void assertEqualsJson(String expected, String actual) {
        try {

            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        } catch (JSONException ex) {
            fail("The test wasn't able to compare JSON strings" + ex);
        }
    }

    private void checkExpectedResponse(Response response, String expectedFile) throws IOException {
        File expectedResponseFile = getJsonResponseFile(PATH_FILES_LISTINGS_GAV, expectedFile);
        assertEqualsJson(readFileToString(expectedResponseFile), response.readEntity(String.class));
    }

}
