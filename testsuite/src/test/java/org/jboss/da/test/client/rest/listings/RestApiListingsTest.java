package org.jboss.da.test.client.rest.listings;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.junit.Assert.assertEquals;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class RestApiListingsTest extends AbstractRestApiListingTest {

    private RequestGenerator generator = new RequestGenerator();

    @Test
    public void testAddProduct() throws Exception {
        String type = "productAdd";

        ClientResponse<String> response = manipulateEntity(ListEntityType.PRODUCT,
                OperationType.POST, type, true);

        checkExpectedResponse(response, "success");
    }

    @Test
    public void testChangeProduct() throws Exception {
        String type = "productAdd";

        ClientResponse<String> response = manipulateEntity(ListEntityType.PRODUCT,
                OperationType.POST, type, true);

        checkExpectedResponse(response, "success");

        type = "productChangeStatus";

        manipulateEntity(ListEntityType.PRODUCT, OperationType.PUT, type, true);
    }

    @Test
    public void testDeleteProduct() throws Exception {
        String type = "productAdd";

        ClientResponse<String> response = manipulateEntity(ListEntityType.PRODUCT,
                OperationType.POST, type, true);

        checkExpectedResponse(response, "success");

        response = manipulateEntity(ListEntityType.PRODUCT, OperationType.DELETE, type, true);

        checkExpectedResponse(response, "success");
    }

    @Test
    public void testAddBlackArtifact() throws Exception {
        String type = "gav";

        ClientResponse<String> response = manipulateEntity(ListEntityType.BLACK,
                OperationType.POST, type, true);

        checkExpectedResponse(response, "success");
    }

    @Test
    public void testDeleteBlackArtifact() throws Exception {
        String type = "gav";
        // add artifact
        manipulateEntity(ListEntityType.BLACK, OperationType.POST, type, true);

        // delete artifact
        ClientResponse<String> response = manipulateEntity(ListEntityType.BLACK,
                OperationType.DELETE, type, true);

        checkExpectedResponse(response, "success");
    }

    @Test
    public void testDeleteNonExistingBlackArtifact() throws Exception {
        String type = "gav";

        ClientResponse<String> response = manipulateEntity(ListEntityType.BLACK,
                OperationType.DELETE, type, true);

        checkExpectedResponse(response, "successFalse");
    }

    @Test
    public void testAlreadyAddedBlackArtifact() throws Exception {
        // add first black artifact
        String type = "gav";
        manipulateEntity(ListEntityType.BLACK, OperationType.POST, type, true);

        // add second black artifact
        ClientResponse<String> response = manipulateEntity(ListEntityType.BLACK,
                OperationType.POST, type, true);

        checkExpectedResponse(response, "successFalse");
    }

    @Test
    public void testAddWhiteArtifact() throws Exception {
        String type = "productAdd";

        ClientResponse<String> response = manipulateEntity(ListEntityType.PRODUCT,
                OperationType.POST, type, true);

        checkExpectedResponse(response, "success");

        type = generator.returnWhiteArtifactString("org.jboss.da", "dependency-analyzer", "0.3.0",
                getIdOfProduct("test", "1.0.0"));

        response = manipulateEntity(ListEntityType.WHITE, OperationType.POST, type, true);

        checkExpectedResponse(response, "success");
    }

    @Test
    public void testDeleteWhiteArtifact() throws Exception {
        String type = "productAdd";

        ClientResponse<String> response = manipulateEntity(ListEntityType.PRODUCT,
                OperationType.POST, type, true);

        checkExpectedResponse(response, "success");

        type = generator.returnWhiteArtifactString("org.jboss.da", "dependency-analyzer", "0.3.0",
                getIdOfProduct("test", "1.0.0"));

        response = manipulateEntity(ListEntityType.WHITE, OperationType.POST, type, true);

        checkExpectedResponse(response, "success");
        // delete artifact
        type = "gav";

        response = manipulateEntity(ListEntityType.WHITE, OperationType.DELETE, type, true);

        checkExpectedResponse(response, "success");
    }

    @Test
    public void testDeleteNonExistingWhiteArtifact() throws Exception {
        String type = "gav";

        ClientResponse<String> response = manipulateEntity(ListEntityType.WHITE,
                OperationType.DELETE, type, true);

        checkExpectedResponse(response, "successFalse");

    }

    @Test
    public void testAddBlacklistedArtifactToWhitelist() throws Exception {
        // Add artifact to blacklist
        String type = "gav";
        ClientResponse<String> response = manipulateEntity(ListEntityType.BLACK,
                OperationType.POST, type, true);
        checkExpectedResponse(response, "success");
        // Try to add artifact to whitelist

        type = "productAdd";

        response = manipulateEntity(ListEntityType.PRODUCT, OperationType.POST, type, true);

        checkExpectedResponse(response, "success");

        type = generator.returnWhiteArtifactString("org.jboss.da", "dependency-analyzer", "0.3.0",
                getIdOfProduct("test", "1.0.0"));

        response = manipulateEntity(ListEntityType.WHITE, OperationType.POST, type, false);

        assertEquals(409, response.getStatus());
    }

    @Test
    public void testAddWhitelistedArtifactToBlacklist() throws Exception {
        String type = "productAdd";

        manipulateEntity(ListEntityType.PRODUCT, OperationType.POST, type, true);

        // Add artifact to whitelist
        type = generator.returnWhiteArtifactString("org.jboss.da", "dependency-analyzer", "0.3.0",
                getIdOfProduct("test", "1.0.0"));

        manipulateEntity(ListEntityType.WHITE, OperationType.POST, type, true);
        // Add artifact to blacklist
        type = "gav";

        ClientResponse<String> response = manipulateEntity(ListEntityType.BLACK,
                OperationType.POST, type, true);

        checkExpectedResponse(response, "successMessage");

        assertEquals(0, getAllArtifactsFromList(PATH_WHITE_LIST).size());
    }

    @Test
    public void testAddMultipleTimeWhitelistedArtifactToBlacklist() throws Exception {
        String type = "productAdd";

        manipulateEntity(ListEntityType.PRODUCT, OperationType.POST, type, true);

        type = "productAdd2";

        manipulateEntity(ListEntityType.PRODUCT, OperationType.POST, type, true);
        // Add artifacts to whitelist
        type = generator.returnWhiteArtifactString("org.jboss.da", "dependency-analyzer", "0.3.0",
                getIdOfProduct("test", "1.0.0"));

        manipulateEntity(ListEntityType.WHITE, OperationType.POST, type, true);

        type = generator.returnWhiteArtifactString("org.jboss.da", "dependency-analyzer", "0.3.0",
                getIdOfProduct("test", "2.0.0"));

        manipulateEntity(ListEntityType.WHITE, OperationType.POST, type, true);
        // Add artifact to blacklist
        type = "gav";
        ClientResponse<String> response = manipulateEntity(ListEntityType.BLACK,
                OperationType.POST, type, true);

        checkExpectedResponse(response, "successMessage");
    }

    @Test
    public void testAlreadyAddedWhiteArtifact() throws Exception {
        String type = "productAdd";

        manipulateEntity(ListEntityType.PRODUCT, OperationType.POST, type, true);

        type = generator.returnWhiteArtifactString("org.jboss.da", "dependency-analyzer", "0.3.0",
                getIdOfProduct("test", "1.0.0"));

        manipulateEntity(ListEntityType.WHITE, OperationType.POST, type, true);

        // add second white artifact
        ClientResponse<String> response = manipulateEntity(ListEntityType.WHITE,
                OperationType.POST, type, true);

        checkExpectedResponse(response, "successFalse");
    }

    @Test
    public void testGetAllWhiteArtifacts() throws Exception {
        // Add artifacts to whitelist
        String type = "productAdd";

        manipulateEntity(ListEntityType.PRODUCT, OperationType.POST, type, true);

        type = "productAdd2";

        manipulateEntity(ListEntityType.PRODUCT, OperationType.POST, type, true);
        // Add artifacts to whitelist
        type = generator.returnWhiteArtifactString("org.jboss.da", "dependency-analyzer", "0.3.0",
                getIdOfProduct("test", "1.0.0"));

        manipulateEntity(ListEntityType.WHITE, OperationType.POST, type, true);

        type = generator.returnWhiteArtifactString("org.jboss.da", "dependency-analyzer", "0.3.0",
                getIdOfProduct("test", "2.0.0"));

        manipulateEntity(ListEntityType.WHITE, OperationType.POST, type, true);
        // Get list

        ClientResponse<String> response = new ClientRequest(restApiURL + PATH_WHITE_LIST)
                .get(String.class);
        assertEquals(200, response.getStatus());

        checkExpectedResponse(response, "gavWhiteList");
    }

    @Test
    public void testGetAllBlackArtifacts() throws Exception {
        // Add artifacts to blacklist
        String type = "gav";
        manipulateEntity(ListEntityType.BLACK, OperationType.POST, type, true);

        type = "gav2";
        manipulateEntity(ListEntityType.BLACK, OperationType.POST, type, true);

        // Get list

        ClientResponse<String> response = new ClientRequest(restApiURL + PATH_BLACK_LIST)
                .get(String.class);
        assertEquals(200, response.getStatus());

        checkExpectedResponse(response, "gavBlackList");
    }

    @Test
    public void testCheckRHBlackArtifact() throws Exception {
        String type = "gav";
        manipulateEntity(ListEntityType.BLACK, OperationType.POST, type, true);

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
        String type = "gav";
        manipulateEntity(ListEntityType.BLACK, OperationType.POST, type, true);

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
        String type = "gav";
        manipulateEntity(ListEntityType.BLACK, OperationType.POST, type, true);

        ClientResponse<String> response = new ClientRequest(restApiURL + PATH_BLACK_LIST
                + "?groupid=org.jboss.da&artifactid=dependency-analyzer&version=0.3")
                .get(String.class);
        assertEquals(200, response.getStatus());

        checkExpectedResponse(response, "gavResponse");
    }

    @Test
    public void testCheckRHWhiteArtifact() throws Exception {
        String type = "productAdd";

        manipulateEntity(ListEntityType.PRODUCT, OperationType.POST, type, true);

        type = generator.returnWhiteArtifactString("org.jboss.da", "dependency-analyzer",
                "0.3.0.redhat-1", getIdOfProduct("test", "1.0.0"));

        manipulateEntity(ListEntityType.WHITE, OperationType.POST, type, true);

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
        String type = "productAdd";

        manipulateEntity(ListEntityType.PRODUCT, OperationType.POST, type, true);

        type = generator.returnWhiteArtifactString("org.jboss.da", "dependency-analyzer",
                "0.3.0.redhat-1", getIdOfProduct("test", "1.0.0"));

        manipulateEntity(ListEntityType.WHITE, OperationType.POST, type, true);

        ClientResponse<String> response = new ClientRequest(restApiURL + PATH_WHITE_LIST
                + "?groupid=org.jboss.da&artifactid=dependency-analyzer&version=0.3.0")
                .get(String.class);
        assertEquals(200, response.getStatus());

        checkExpectedResponse(response, "gavRhResponse");
    }

    private void checkExpectedResponse(ClientResponse<String> response, String expectedFile)
            throws IOException {
        File expectedResponseFile = getJsonResponseFile(PATH_FILES_LISTINGS_GAV, expectedFile);
        assertEqualsJson(readFileToString(expectedResponseFile), response.getEntity(String.class));
    }

}
