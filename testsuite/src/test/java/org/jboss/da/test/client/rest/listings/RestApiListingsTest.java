package org.jboss.da.test.client.rest.listings;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.apache.commons.io.FileUtils.readFileToString;
import org.json.JSONException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

public class RestApiListingsTest extends AbstractRestApiListingTest {

    private RequestGenerator generator = new RequestGenerator();

    @Test
    public void testAddProduct() throws Exception {
        ClientResponse<String> response = manipulateEntityFile(ListEntityType.PRODUCT,
                OperationType.POST, "productAdd", true);

        checkExpectedResponse(response, "success");
    }

    @Test
    public void testChangeProduct() throws Exception {
        ClientResponse<String> response = manipulateEntityFile(ListEntityType.PRODUCT,
                OperationType.POST, "productAdd", true);

        checkExpectedResponse(response, "success");

        manipulateEntityFile(ListEntityType.PRODUCT, OperationType.PUT, "productChangeStatus", true);
    }

    @Test
    public void testDeleteProduct() throws Exception {
        ClientResponse<String> response = manipulateEntityFile(ListEntityType.PRODUCT,
                OperationType.POST, "productAdd", true);

        checkExpectedResponse(response, "success");

        response = manipulateEntityFile(ListEntityType.PRODUCT, OperationType.DELETE, "productAdd",
                true);

        checkExpectedResponse(response, "success");
    }

    @Test
    public void testAddBlackArtifact() throws Exception {
        ClientResponse<String> response = manipulateEntityFile(ListEntityType.BLACK,
                OperationType.POST, "gav", true);

        checkExpectedResponse(response, "success");
    }

    @Test
    public void testDeleteBlackArtifact() throws Exception {
        // add artifact
        manipulateEntityFile(ListEntityType.BLACK, OperationType.POST, "gav", true);

        // delete artifact
        ClientResponse<String> response = manipulateEntityFile(ListEntityType.BLACK,
                OperationType.DELETE, "gav", true);

        checkExpectedResponse(response, "success");
    }

    @Test
    public void testDeleteNonExistingBlackArtifact() throws Exception {
        ClientResponse<String> response = manipulateEntityFile(ListEntityType.BLACK,
                OperationType.DELETE, "gav", true);

        checkExpectedResponse(response, "successFalse");
    }

    @Test
    public void testAlreadyAddedBlackArtifact() throws Exception {
        // add first black artifact
        manipulateEntityFile(ListEntityType.BLACK, OperationType.POST, "gav", true);

        // add second black artifact
        ClientResponse<String> response = manipulateEntityFile(ListEntityType.BLACK,
                OperationType.POST, "gav", true);

        checkExpectedResponse(response, "successFalse");
    }

    @Test
    public void testAddWhiteArtifact() throws Exception {
        ClientResponse<String> response = manipulateEntityFile(ListEntityType.PRODUCT,
                OperationType.POST, "productAdd", true);

        checkExpectedResponse(response, "success");

        String artifact = generator.returnWhiteArtifactString("org.jboss.da",
                "dependency-analyzer", "0.3.0", getIdOfProduct("test", "1.0.0"));

        response = manipulateEntityString(ListEntityType.WHITE, OperationType.POST, artifact, true);

        checkExpectedResponse(response, "success");
    }

    @Test
    public void testDeleteWhiteArtifact() throws Exception {
        ClientResponse<String> response = manipulateEntityFile(ListEntityType.PRODUCT,
                OperationType.POST, "productAdd", true);

        checkExpectedResponse(response, "success");

        String artifact = generator.returnWhiteArtifactString("org.jboss.da",
                "dependency-analyzer", "0.3.0", getIdOfProduct("test", "1.0.0"));

        response = manipulateEntityString(ListEntityType.WHITE, OperationType.POST, artifact, true);

        checkExpectedResponse(response, "success");

        // delete artifact
        response = manipulateEntityFile(ListEntityType.WHITE, OperationType.DELETE, "gav", true);

        checkExpectedResponse(response, "success");
    }

    @Test
    public void testDeleteNonExistingWhiteArtifact() throws Exception {
        ClientResponse<String> response = manipulateEntityFile(ListEntityType.WHITE,
                OperationType.DELETE, "gav", true);

        checkExpectedResponse(response, "successFalse");
    }

    @Test
    public void testAddBlacklistedArtifactToWhitelist() throws Exception {
        // Add artifact to blacklist
        ClientResponse<String> response = manipulateEntityFile(ListEntityType.BLACK,
                OperationType.POST, "gav", true);
        checkExpectedResponse(response, "success");

        // Try to add artifact to whitelist
        response = manipulateEntityFile(ListEntityType.PRODUCT, OperationType.POST, "productAdd",
                true);

        checkExpectedResponse(response, "success");

        String artifact = generator.returnWhiteArtifactString("org.jboss.da",
                "dependency-analyzer", "0.3.0", getIdOfProduct("test", "1.0.0"));

        response = manipulateEntityString(ListEntityType.WHITE, OperationType.POST, artifact, false);

        assertEquals(200, response.getStatus());
    }

    @Test
    public void testAddWhitelistedArtifactToBlacklist() throws Exception {
        manipulateEntityFile(ListEntityType.PRODUCT, OperationType.POST, "productAdd", true);

        // Add artifact to whitelist
        String artifact = generator.returnWhiteArtifactString("org.jboss.da",
                "dependency-analyzer", "0.3.0", getIdOfProduct("test", "1.0.0"));

        manipulateEntityString(ListEntityType.WHITE, OperationType.POST, artifact, true);

        // Add artifact to blacklist
        ClientResponse<String> response = manipulateEntityFile(ListEntityType.BLACK,
                OperationType.POST, "gav", true);

        checkExpectedResponse(response, "successMessage");

        assertEquals(0, getAllArtifactsFromList(PATH_WHITE_LIST).size());
    }

    @Test
    public void testAddMultipleTimeWhitelistedArtifactToBlacklist() throws Exception {
        manipulateEntityFile(ListEntityType.PRODUCT, OperationType.POST, "productAdd", true);
        manipulateEntityFile(ListEntityType.PRODUCT, OperationType.POST, "productAdd2", true);

        // Add artifacts to whitelist
        String artifact;
        artifact = generator.returnWhiteArtifactString("org.jboss.da", "dependency-analyzer",
                "0.3.0", getIdOfProduct("test", "1.0.0"));

        manipulateEntityString(ListEntityType.WHITE, OperationType.POST, artifact, true);

        artifact = generator.returnWhiteArtifactString("org.jboss.da", "dependency-analyzer",
                "0.3.0", getIdOfProduct("test", "2.0.0"));

        manipulateEntityString(ListEntityType.WHITE, OperationType.POST, artifact, true);
        // Add artifact to blacklist
        ClientResponse<String> response = manipulateEntityFile(ListEntityType.BLACK,
                OperationType.POST, "gav", true);

        checkExpectedResponse(response, "successMessage");
    }

    @Test
    public void testAlreadyAddedWhiteArtifact() throws Exception {
        manipulateEntityFile(ListEntityType.PRODUCT, OperationType.POST, "productAdd", true);

        String artifact = generator.returnWhiteArtifactString("org.jboss.da",
                "dependency-analyzer", "0.3.0", getIdOfProduct("test", "1.0.0"));

        manipulateEntityString(ListEntityType.WHITE, OperationType.POST, artifact, true);

        // add second white artifact
        ClientResponse<String> response = manipulateEntityString(ListEntityType.WHITE,
                OperationType.POST, artifact, true);

        checkExpectedResponse(response, "successFalse");
    }

    @Test
    public void testGetAllWhiteArtifacts() throws Exception {
        // Add artifacts to whitelist
        manipulateEntityFile(ListEntityType.PRODUCT, OperationType.POST, "productAdd", true);
        manipulateEntityFile(ListEntityType.PRODUCT, OperationType.POST, "productAdd2", true);

        // Add artifacts to whitelist
        String artifact;
        artifact = generator.returnWhiteArtifactString("org.jboss.da", "dependency-analyzer",
                "0.3.0", getIdOfProduct("test", "1.0.0"));

        manipulateEntityString(ListEntityType.WHITE, OperationType.POST, artifact, true);

        artifact = generator.returnWhiteArtifactString("org.jboss.da", "dependency-analyzer",
                "0.3.0", getIdOfProduct("test", "2.0.0"));

        manipulateEntityString(ListEntityType.WHITE, OperationType.POST, artifact, true);

        // Get list
        ClientResponse<String> response = new ClientRequest(restApiURL + PATH_WHITE_LIST)
                .get(String.class);
        assertEquals(200, response.getStatus());

        checkExpectedResponse(response, "gavWhiteList");
    }

    @Test
    public void testGetAllBlackArtifacts() throws Exception {
        // Add artifacts to blacklist
        manipulateEntityFile(ListEntityType.BLACK, OperationType.POST, "gav", true);

        manipulateEntityFile(ListEntityType.BLACK, OperationType.POST, "gav2", true);

        // Get list
        ClientResponse<String> response = new ClientRequest(restApiURL + PATH_BLACK_LIST)
                .get(String.class);
        assertEquals(200, response.getStatus());

        checkExpectedResponse(response, "gavBlackList");
    }

    @Test
    public void testCheckRHBlackArtifact() throws Exception {
        manipulateEntityFile(ListEntityType.BLACK, OperationType.POST, "gav", true);

        ClientResponse<String> response = new ClientRequest(restApiURL + PATH_BLACK_LIST
                + "?groupid=org.jboss.da&artifactid=dependency-analyzer&version=0.3.0.redhat-1")
                .get(String.class);
        assertEquals(200, response.getStatus());

        checkExpectedResponse(response, "gavResponse");
    }

    /**
     * Non RedHat but OSGi compliant black artifact test
     * 
     * @throws Exception
     */
    @Test
    public void testCheckNonRHBlackArtifact() throws Exception {
        manipulateEntityFile(ListEntityType.BLACK, OperationType.POST, "gav", true);

        ClientResponse<String> response = new ClientRequest(restApiURL + PATH_BLACK_LIST
                + "?groupid=org.jboss.da&artifactid=dependency-analyzer&version=0.3.0")
                .get(String.class);
        assertEquals(200, response.getStatus());

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

        ClientResponse<String> response = new ClientRequest(restApiURL + PATH_BLACK_LIST
                + "?groupid=org.jboss.da&artifactid=dependency-analyzer&version=0.3")
                .get(String.class);
        assertEquals(200, response.getStatus());

        checkExpectedResponse(response, "gavResponse");
    }

    @Test
    public void testCheckRHWhiteArtifact() throws Exception {
        manipulateEntityFile(ListEntityType.PRODUCT, OperationType.POST, "productAdd", true);

        String artifact = generator.returnWhiteArtifactString("org.jboss.da",
                "dependency-analyzer", "0.3.0.redhat-1", getIdOfProduct("test", "1.0.0"));

        manipulateEntityString(ListEntityType.WHITE, OperationType.POST, artifact, true);

        ClientResponse<String> response = new ClientRequest(restApiURL + PATH_WHITE_LIST
                + "?groupid=org.jboss.da&artifactid=dependency-analyzer&version=0.3.0.redhat-1")
                .get(String.class);
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

        String artifact = generator.returnWhiteArtifactString("org.jboss.da",
                "dependency-analyzer", "0.3.0.redhat-1", getIdOfProduct("test", "1.0.0"));

        manipulateEntityString(ListEntityType.WHITE, OperationType.POST, artifact, true);

        ClientResponse<String> response = new ClientRequest(restApiURL + PATH_WHITE_LIST
                + "?groupid=org.jboss.da&artifactid=dependency-analyzer&version=0.3.0")
                .get(String.class);
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

    private void checkExpectedResponse(ClientResponse<String> response, String expectedFile)
            throws IOException {
        File expectedResponseFile = getJsonResponseFile(PATH_FILES_LISTINGS_GAV, expectedFile);
        assertEqualsJson(readFileToString(expectedResponseFile), response.getEntity(String.class));
    }

}
