package org.jboss.da.test.client.rest.listings;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.junit.Assert.*;

import org.apache.commons.io.FileUtils;
import org.jboss.da.test.client.AbstractRestApiTest;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.util.GenericType;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.core.MediaType;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

public class RestApiListingsTest extends AbstractRestApiTest {

    private enum ListType {
        BLACK, WHITE
    }

    private enum OperationType {
        ADD, DELETE
    }

    private static String ENCODING = "utf-8";

    private static String PATH_FILES_LISTINGS_GAV = "/listings";

    private static String PATH_WHITE_LIST = "/listings/whitelist";

    private static String PATH_BLACK_LIST = "/listings/blacklist";

    private static String PATH_WHITE_LISTINGS_GAV = "/listings/whitelist/gav";

    private static String PATH_BLACK_LISTINGS_GAV = "/listings/blacklist/gav";

    private static final ObjectMapper mapper = new ObjectMapper();

    @After
    public void dropTables() throws Exception {
        List<RestArtifact> whitelistedArtifacts = getAllArtifactsFromList(PATH_WHITE_LIST);
        whitelistedArtifacts.forEach(gav -> removeGavFromList(PATH_WHITE_LISTINGS_GAV, gav));

        List<RestArtifact> blacklistedArtifacts = getAllArtifactsFromList(PATH_BLACK_LIST);
        blacklistedArtifacts.forEach(gav -> removeGavFromList(PATH_BLACK_LISTINGS_GAV, gav));
    }

    private void removeGavFromList(String listUrl, RestArtifact gav) {
        try {
            ClientRequest request = createClientRequest(listUrl, mapper.writeValueAsString(gav));
            request.delete(String.class);
        } catch (Exception e) {
            fail("Failed to remove GAV from the list using URL " + listUrl);
        }
    }

    private List<RestArtifact> getAllArtifactsFromList(String listUrl) throws Exception {
        return processGetRequest(new GenericType<List<RestArtifact>>() {}, restApiURL + listUrl);
    }

    private <T> T processGetRequest(GenericType<T> type, String url) throws Exception {
        ClientRequest request = new ClientRequest(url);
        request.accept(MediaType.APPLICATION_JSON);

        ClientResponse<T> response = request.get(type);

        if (response.getStatus() != 200)
            fail("Failed to get entity via REST API");

        return response.getEntity();
    }

    @Test
    public void testAddWhiteArtifact() throws Exception {
        String type = "gavRh";

        ClientResponse<String> response = manipulateArtifact(ListType.WHITE, OperationType.ADD,
                type, true);

        checkExpectedResponse(response, "success");
    }

    @Test
    public void testAddNonRHWhiteArtifact() throws Exception {
        String type = "gav";

        ClientResponse<String> response = manipulateArtifact(ListType.WHITE, OperationType.ADD,
                type, false);

        assertEquals(400, response.getStatus());
    }

    @Test
    public void testAddBlackArtifact() throws Exception {
        String type = "gav";

        ClientResponse<String> response = manipulateArtifact(ListType.BLACK, OperationType.ADD,
                type, true);

        checkExpectedResponse(response, "success");
    }

    @Test
    public void testAddBlackRHArtifact() throws Exception {
        String type = "gavRh";

        ClientResponse<String> response = manipulateArtifact(ListType.BLACK, OperationType.ADD,
                type, true);

        checkExpectedResponse(response, "success");
    }

    @Test
    public void testAddBlackNonOSGiArtifact() throws Exception {
        String type = "gavNonOSGi";

        ClientResponse<String> response = manipulateArtifact(ListType.BLACK, OperationType.ADD,
                type, true);

        checkExpectedResponse(response, "success");
    }

    @Test
    public void testAddBlacklistedArtifactToWhitelist() throws Exception {
        // Add artifact to blacklist
        String type = "gav";
        manipulateArtifact(ListType.BLACK, OperationType.ADD, type, true);

        // Try to add artifact to whitelist
        type = "gavRh";

        ClientResponse<String> response = manipulateArtifact(ListType.WHITE, OperationType.ADD,
                type, false);

        assertEquals(409, response.getStatus());
    }

    @Test
    public void testAddWhitelistedArtifactToBlacklist() throws Exception {
        // Add artifact to whitelist
        String type = "gavRh";
        manipulateArtifact(ListType.WHITE, OperationType.ADD, type, true);

        // Add artifact to blacklist
        type = "gav";

        ClientResponse<String> response = manipulateArtifact(ListType.BLACK, OperationType.ADD,
                type, true);

        checkExpectedResponse(response, "successMessage");

        assertEquals(0, getAllArtifactsFromList(PATH_WHITE_LIST).size());
    }

    /**
     * Artifact added to whitelist: 1.0.redhat-1
     * Artifact added to blacklis: 1.0.redhat-1
     * @throws Exception
     */
    @Ignore
    @Test
    public void testAddNonOSGiWhitelistedArtifactToBlacklist() throws Exception {
        checkBlacklistingArtifactTest("gavRhNonOSGi", null, "gavRhNonOSGi");
    }

    /**
     * Artifact added to whitelist: 1.0.redhat-1
     * Artifact added to blacklis: 1.0
     * @throws Exception
     */
    @Ignore
    @Test
    public void testAddNonOSGiWhitelistedArtifactToBlacklistv2() throws Exception {
        checkBlacklistingArtifactTest("gavRhNonOSGi", null, "gavRhNonOSGiv2");
    }

    /**
     * Artifact added to whitelist: 1.0.redhat-1
     * Artifact added to blacklis: 1.0.0
     * @throws Exception
     */
    @Ignore
    @Test
    public void testAddNonOSGiWhitelistedArtifactToBlacklistv3() throws Exception {
        checkBlacklistingArtifactTest("gavRhNonOSGi", null, "gavRhNonOSGiv3");
    }

    /**
     * Artifact added to whitelist: 1.0.redhat-1
     * Artifact added to blacklis: 1.0.0.redhat-1
     * @throws Exception
     */
    @Ignore
    @Test
    public void testAddNonOSGiWhitelistedArtifactToBlacklistv4() throws Exception {
        checkBlacklistingArtifactTest("gavRhNonOSGi", null, "gavRh4");
    }

    /**
     * Artifact added to whitelist: 1.0.redhat-1
     * Artifact added to blacklis: 1.0.0.redhat-2
     * @throws Exception
     */
    @Ignore
    @Test
    public void testAddNonOSGiWhitelistedArtifactToBlacklistv5() throws Exception {
        checkBlacklistingArtifactTest("gavRhNonOSGi", null, "gavRh3");
    }

    /**
     * Artifacts added to whitelist: 1.0.redhat-1, 1.0.0.redhat-2
     * Artifact added to blacklis: 1.0.redhat-1
     * @throws Exception
     */
    @Ignore
    @Test
    public void testAddNonOSGiAndOSGiWhitelistedArtifactsToBlacklist() throws Exception {
        checkBlacklistingArtifactTest("gavRhNonOSGi", "gavRh3", "gavRhNonOSGi");
    }

    /**
     * Artifacts added to whitelist: 1.0.redhat-1, 1.0.0.redhat-2
     * Artifact added to blacklis: 1.0
     * @throws Exception
     */
    @Ignore
    @Test
    public void testAddNonOSGiAndOSGiWhitelistedArtifactsToBlacklistv2() throws Exception {
        checkBlacklistingArtifactTest("gavRhNonOSGi", "gavRh3", "gavRhNonOSGiv2");
    }

    /**
     * Artifacts added to whitelist: 1.0.redhat-1, 1.0.0.redhat-2
     * Artifact added to blacklis: 1.0.0
     * @throws Exception
     */
    @Ignore
    @Test
    public void testAddNonOSGiAndOSGiWhitelistedArtifactsToBlacklistv3() throws Exception {
        checkBlacklistingArtifactTest("gavRhNonOSGi", "gavRh3", "gavRhNonOSGiv3");
    }

    /**
     * Artifacts added to whitelist: 1.0.redhat-1, 1.0.0.redhat-2
     * Artifact added to blacklis: 1.0.0.redhat-1
     * @throws Exception
     */
    @Ignore
    @Test
    public void testAddNonOSGiAndOSGiWhitelistedArtifactsToBlacklistv4() throws Exception {
        checkBlacklistingArtifactTest("gavRhNonOSGi", "gavRh3", "gavRh4");
    }

    @Test
    public void testAddWhitelistedNonOSGiArtifactToBlacklist() throws Exception {
        // Add artifact to whitelist
        String type = "gavRh";
        manipulateArtifact(ListType.WHITE, OperationType.ADD, type, true);

        // Add artifact to blacklist
        type = "gavNonOSGi";

        ClientResponse<String> response = manipulateArtifact(ListType.BLACK, OperationType.ADD,
                type, true);

        checkExpectedResponse(response, "successMessage");

        assertEquals(0, getAllArtifactsFromList(PATH_WHITE_LIST).size());
    }

    @Test
    public void testAddMultipleTimeWhitelistedArtifactToBlacklist() throws Exception {
        // Add artifacts to whitelist
        String type = "gavRh";
        manipulateArtifact(ListType.WHITE, OperationType.ADD, type, true);

        type = "gavRh2";
        manipulateArtifact(ListType.WHITE, OperationType.ADD, type, true);
        // Add artifact to blacklist
        type = "gav";
        ClientResponse<String> response = manipulateArtifact(ListType.BLACK, OperationType.ADD,
                type, true);

        checkExpectedResponse(response, "successMessage");
    }

    @Test
    public void testAlreadyAddedWhiteArtifact() throws Exception {
        // add first white artifact
        String type = "gavRh";
        manipulateArtifact(ListType.WHITE, OperationType.ADD, type, true);

        // add second white artifact
        ClientResponse<String> response = manipulateArtifact(ListType.WHITE, OperationType.ADD,
                type, true);

        checkExpectedResponse(response, "successFalse");
    }

    @Test
    public void testAlreadyAddedBlackArtifact() throws Exception {
        // add first black artifact
        String type = "gav";
        manipulateArtifact(ListType.BLACK, OperationType.ADD, type, true);

        // add second black artifact
        ClientResponse<String> response = manipulateArtifact(ListType.BLACK, OperationType.ADD,
                type, true);

        checkExpectedResponse(response, "successFalse");
    }

    @Test
    public void testDeleteWhiteArtifact() throws Exception {
        String type = "gavRh";
        // add artifact
        manipulateArtifact(ListType.WHITE, OperationType.ADD, type, true);

        // delete artifact
        ClientResponse<String> response = manipulateArtifact(ListType.WHITE, OperationType.DELETE,
                type, true);

        checkExpectedResponse(response, "success");
    }

    @Test
    public void testDeleteBlackArtifact() throws Exception {
        String type = "gav";
        // add artifact
        manipulateArtifact(ListType.BLACK, OperationType.ADD, type, true);

        // delete artifact
        ClientResponse<String> response = manipulateArtifact(ListType.BLACK, OperationType.DELETE,
                type, true);

        checkExpectedResponse(response, "success");
    }

    @Test
    public void testDeleteNonExistingWhiteArtifact() throws Exception {
        String type = "gavRh";

        ClientResponse<String> response = manipulateArtifact(ListType.WHITE, OperationType.DELETE,
                type, true);

        checkExpectedResponse(response, "successFalse");

    }

    @Test
    public void testDeleteNonExistingBlackArtifact() throws Exception {
        String type = "gav";

        ClientResponse<String> response = manipulateArtifact(ListType.BLACK, OperationType.DELETE,
                type, true);

        checkExpectedResponse(response, "successFalse");

    }

    @Test
    public void testCheckRHWhiteArtifact() throws Exception {
        String type = "gavRh";
        manipulateArtifact(ListType.WHITE, OperationType.ADD, type, true);

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
        String type = "gavRh";
        manipulateArtifact(ListType.WHITE, OperationType.ADD, type, true);

        ClientResponse<String> response = new ClientRequest(restApiURL + PATH_WHITE_LIST
                + "?groupid=org.jboss.da&artifactid=dependency-analyzer&version=0.3.0")
                .get(String.class);
        assertEquals(200, response.getStatus());

        checkExpectedResponse(response, "gavRhResponse");
    }

    /**
     * Added artifact: 0.3.0.redhat-1
     * Looking for artifact version: 0.3
     * Should find artifact: 0.3.0.redhat-1
     * 
     * @throws Exception
     */
    @Test
    public void testCheckNonRHNonOSGiWhiteArtifact() throws Exception {
        checkArtifactTest("gavRh", null, "0.3", "gavRhResponse");
    }

    /**
     * Added artifact: 1.0.redhat-1
     * Looking for artifact version: 1.0.redhat-1
     * Should find artifact: 1.0.redhat-1
     * 
     * @throws Exception
     */
    @Test
    public void testCheckRHNonOSGiArtifact() throws Exception {
        checkArtifactTest("gavRhNonOSGi", null, "1.0.redhat-1", "gavRhNonOSGiResponse");
    }

    /**
     * Added artifact: 1.0.redhat-1
     * Looking for artifact version: 1.0
     * Should find artifact: 1.0.redhat-1
     * 
     * @throws Exception
     */
    @Test
    public void testCheckRHNonOSGiArtifactv2() throws Exception {
        checkArtifactTest("gavRhNonOSGi", null, "1.0", "gavRhNonOSGiResponse");
    }

    /**
     * Added artifact: 1.0.redhat-1
     * Looking for artifact version: 1.0.0
     * Should find artifact: 1.0.redhat-1
     * 
     * @throws Exception
     */
    @Test
    public void testCheckRHNonOSGiArtifactv3() throws Exception {
        checkArtifactTest("gavRhNonOSGi", null, "1.0.0", "gavRhNonOSGiResponse");
    }

    /**
     * Added artifact: 1.0.redhat-1
     * Looking for artifact version: 1.0.0.redhat-1
     * Should find artifact: 1.0.redhat-1
     * 
     * @throws Exception
     */
    @Test
    public void testCheckRHNonOSGiArtifactv4() throws Exception {
        checkArtifactTest("gavRhNonOSGi", null, "1.0.0.redhat-1", "gavRhNonOSGiResponse");
    }

    /**
     * Added artifacts: 1.0.redhat-1, 1.0.0.redhat-2
     * Looking for artifact version: 1.0.redhat-1
     * Should find artifact: 1.0.redhat-1, 1.0.0.redhat-2
     * 
     * @throws Exception
     */
    @Test
    public void testCheckRHNonOSGiandOSGiArtifacts() throws Exception {
        checkArtifactTest("gavRhNonOSGi", "gavRh3", "1.0.redhat-1", "gavNonOSGiList");
    }

    /**
     * Added artifacts: 1.0.redhat-1, 1.0.0.redhat-2
     * Looking for artifact version: 1.0
     * Should find artifact: 1.0.redhat-1, 1.0.0.redhat-2
     * 
     * @throws Exception
     */
    @Test
    public void testCheckRHNonOSGiandOSGiArtifactsv2() throws Exception {
        checkArtifactTest("gavRhNonOSGi", "gavRh3", "1.0", "gavNonOSGiList");
    }

    /**
     * Added artifacts: 1.0.redhat-1, 1.0.0.redhat-2
     * Looking for artifact version: 1.0.0
     * Should find artifact: 1.0.redhat-1, 1.0.0.redhat-2
     * 
     * @throws Exception
     */
    @Test
    public void testCheckRHNonOSGiandOSGiArtifactsv3() throws Exception {
        checkArtifactTest("gavRhNonOSGi", "gavRh3", "1.0.0", "gavNonOSGiList");
    }

    /**
     * Added artifacts: 1.0.redhat-1, 1.0.0.redhat-2
     * Looking for artifact version: 1.0.0.redhat-1
     * Should find artifact: 1.0.redhat-1, 1.0.0.redhat-2
     * 
     * @throws Exception
     */
    @Test
    public void testCheckRHNonOSGiandOSGiArtifactsv4() throws Exception {
        checkArtifactTest("gavRhNonOSGi", "gavRh3", "1.0.0.redhat-1", "gavNonOSGiList");
    }

    @Test
    public void testCheckRHBlackArtifact() throws Exception {
        String type = "gav";
        manipulateArtifact(ListType.BLACK, OperationType.ADD, type, true);

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
        manipulateArtifact(ListType.BLACK, OperationType.ADD, type, true);

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
        manipulateArtifact(ListType.BLACK, OperationType.ADD, type, true);

        ClientResponse<String> response = new ClientRequest(restApiURL + PATH_BLACK_LIST
                + "?groupid=org.jboss.da&artifactid=dependency-analyzer&version=0.3")
                .get(String.class);
        assertEquals(200, response.getStatus());

        checkExpectedResponse(response, "gavResponse");
    }

    @Test
    public void testGetAllWhiteArtifacts() throws Exception {
        // Add artifacts to whitelist
        String type = "gavRh";
        manipulateArtifact(ListType.WHITE, OperationType.ADD, type, true);

        type = "gavRh2";
        manipulateArtifact(ListType.WHITE, OperationType.ADD, type, true);

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
        manipulateArtifact(ListType.BLACK, OperationType.ADD, type, true);

        type = "gav2";
        manipulateArtifact(ListType.BLACK, OperationType.ADD, type, true);

        // Get list

        ClientResponse<String> response = new ClientRequest(restApiURL + PATH_BLACK_LIST)
                .get(String.class);
        assertEquals(200, response.getStatus());

        checkExpectedResponse(response, "gavBlackList");
    }

    private ClientResponse<String> manipulateArtifact(ListType list, OperationType operation,
            String file, Boolean checkAdd) throws Exception {
        String type = file;
        File jsonRequestFile = getJsonRequestFile(PATH_FILES_LISTINGS_GAV, type);
        String path = null;
        switch (list) {
            case WHITE:
                path = PATH_WHITE_LISTINGS_GAV;
                break;
            case BLACK:
                path = PATH_BLACK_LISTINGS_GAV;
                break;
        }
        ClientRequest request = createClientRequest(path,
                FileUtils.readFileToString(jsonRequestFile, ENCODING));
        ClientResponse<String> response = null;
        switch (operation) {
            case ADD:
                response = request.post(String.class);
                break;

            case DELETE:
                response = request.delete(String.class);
                break;
        }

        if (checkAdd)
            assertEquals(200, response.getStatus());
        return response;
    }

    private void checkBlacklistingArtifactTest(String artifact1, String artifact2, String addToBL)
            throws Exception {
        manipulateArtifact(ListType.WHITE, OperationType.ADD, artifact1, true);
        if (artifact2 != null) {
            manipulateArtifact(ListType.WHITE, OperationType.ADD, artifact2, true);
        }
        manipulateArtifact(ListType.BLACK, OperationType.ADD, addToBL, true);

        assertEquals(0, getAllArtifactsFromList(PATH_WHITE_LIST).size());
        assertEquals(1, getAllArtifactsFromList(PATH_BLACK_LIST).size());
        assertEquals("1.0.0", getAllArtifactsFromList(PATH_BLACK_LIST).get(0).getVersion());
    }

    private void checkArtifactTest(String artifact1, String artifact2, String testVersion,
            String expectedResult) throws Exception {
        manipulateArtifact(ListType.WHITE, OperationType.ADD, artifact1, true);

        if (artifact2 != null) {
            manipulateArtifact(ListType.WHITE, OperationType.ADD, artifact2, true);
        }

        ClientResponse<String> response = new ClientRequest(restApiURL + PATH_WHITE_LIST
                + "?groupid=org.jboss.da&artifactid=dependency-analyzer&version=" + testVersion)
                .get(String.class);
        assertEquals(200, response.getStatus());

        checkExpectedResponse(response, expectedResult);
    }

    private void checkExpectedResponse(ClientResponse<String> response, String expectedFile)
            throws IOException {
        File expectedResponseFile = new ExpectedResponseFilenameBuilder(
                restApiExpectedResponseFolder, PATH_FILES_LISTINGS_GAV, APPLICATION_JSON,
                expectedFile).getFile();
        assertEquals(readFileToString(expectedResponseFile).trim(), response
                .getEntity(String.class).trim());
    }

    private File getJsonRequestFile(String path, String variant) {
        return new RequestFilenameBuilder(restApiRequestFolder, path, APPLICATION_JSON, variant)
                .getFile();
    }

    private ClientRequest createClientRequest(String relativePath, String jsonRequest) {
        ClientRequest request = new ClientRequest(restApiURL + relativePath);
        request.header("Content-Type", APPLICATION_JSON);
        request.body(MediaType.APPLICATION_JSON_TYPE, jsonRequest);
        return request;
    }
}
