package org.jboss.da.test.client.rest.listings;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.commons.io.FileUtils;
import org.jboss.da.test.client.AbstractRestReportsTest;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.util.GenericType;
import org.junit.After;
import org.junit.Test;

import javax.ws.rs.core.MediaType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

public class RestApiListingsTest extends AbstractRestReportsTest {

    private RequestGenerator generator = new RequestGenerator();

    private enum ListEntityType {
        BLACK, WHITE, PRODUCT;
    }

    private enum OperationType {
        POST, DELETE, PUT;
    }

    private static final String ENCODING = "utf-8";

    private static final String PATH_FILES_LISTINGS_GAV = "/listings";

    private static final String PATH_WHITE_LIST = "/listings/whitelist";

    private static final String PATH_BLACK_LIST = "/listings/blacklist";

    private static final String PATH_WHITE_LISTINGS_GAV = "/listings/whitelist/gav";

    private static final String PATH_WHITE_ARTIFACTS = "/listings/whitelist/artifacts/gav";

    private static final String PATH_BLACK_LISTINGS_GAV = "/listings/blacklist/gav";

    private static final String PATH_PRODUCT = "/listings/whitelist/product";

    private static final String PATH_PRODUCTS = "/listings/whitelist/products";

    private static final ObjectMapper mapper = new ObjectMapper();

    @After
    public void dropTables() throws Exception {
        List<RestWhiteArtifact> whitelistedArtifacts = getAllWhiteArtifactsFromList(PATH_WHITE_LIST);
        List<RestArtifact> artifacts = whiteToRestArtifactList(whitelistedArtifacts);
        artifacts.forEach(gav -> removeGavFromList(PATH_WHITE_LISTINGS_GAV, gav));

        List<RestArtifact> blacklistedArtifacts = getAllArtifactsFromList(PATH_BLACK_LIST);
        blacklistedArtifacts.forEach(gav -> removeGavFromList(PATH_BLACK_LISTINGS_GAV, gav));

        List<RestProduct> products = getAllProductsFromList(PATH_PRODUCTS);
        products.forEach(product -> removeProductFromList(PATH_PRODUCT, product));
    }

    private void removeGavFromList(String listUrl, RestArtifact gav) {
        try {
            ClientRequest request = createClientRequest(listUrl, mapper.writeValueAsString(gav));
            request.delete(String.class);
        } catch (Exception e) {
            fail("Failed to remove GAV from the list using URL " + listUrl);
        }
    }

    private void removeProductFromList(String url, RestProduct product) {
        try {
            ClientRequest request = createClientRequest(url, toRestProductRequest(product));
            request.delete(String.class);
        } catch (Exception e) {
            fail("Failed to remove product from the list using URL " + url);
        }

    }

    private String toRestProductRequest(RestProduct p) {
        return "{" + "\"name\":" + "\"" + p.getName() + "\"," + "\"version\":" + "\""
                + p.getVersion() + "\"" + "}";
    }

    private List<RestArtifact> getAllArtifactsFromList(String listUrl) throws Exception {
        return processGetRequest(new GenericType<List<RestArtifact>>() {}, restApiURL + listUrl);
    }

    private List<RestWhiteArtifact> getAllWhiteArtifactsFromList(String listUrl) throws Exception {
        return processGetRequest(new GenericType<List<RestWhiteArtifact>>() {}, restApiURL
                + listUrl);
    }

    private List<RestProduct> getAllProductsFromList(String listUrl) throws Exception {
        return processGetRequest(new GenericType<List<RestProduct>>() {}, restApiURL + listUrl);
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

        response = manipulateEntity(ListEntityType.PRODUCT, OperationType.PUT, type, true);
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

        ClientResponse<String> response = manipulateEntity(ListEntityType.PRODUCT,
                OperationType.POST, type, true);

        // Add artifact to whitelist
        type = generator.returnWhiteArtifactString("org.jboss.da", "dependency-analyzer", "0.3.0",
                getIdOfProduct("test", "1.0.0"));

        response = manipulateEntity(ListEntityType.WHITE, OperationType.POST, type, true);
        // Add artifact to blacklist
        type = "gav";

        response = manipulateEntity(ListEntityType.BLACK, OperationType.POST, type, true);

        checkExpectedResponse(response, "successMessage");

        assertEquals(0, getAllArtifactsFromList(PATH_WHITE_LIST).size());
    }

    @Test
    public void testAddMultipleTimeWhitelistedArtifactToBlacklist() throws Exception {
        String type = "productAdd";

        ClientResponse<String> response = manipulateEntity(ListEntityType.PRODUCT,
                OperationType.POST, type, true);

        type = "productAdd2";

        response = manipulateEntity(ListEntityType.PRODUCT, OperationType.POST, type, true);
        // Add artifacts to whitelist
        type = generator.returnWhiteArtifactString("org.jboss.da", "dependency-analyzer", "0.3.0",
                getIdOfProduct("test", "1.0.0"));

        response = manipulateEntity(ListEntityType.WHITE, OperationType.POST, type, true);

        type = generator.returnWhiteArtifactString("org.jboss.da", "dependency-analyzer", "0.3.0",
                getIdOfProduct("test", "2.0.0"));

        response = manipulateEntity(ListEntityType.WHITE, OperationType.POST, type, true);
        // Add artifact to blacklist
        type = "gav";
        response = manipulateEntity(ListEntityType.BLACK, OperationType.POST, type, true);

        checkExpectedResponse(response, "successMessage");
    }

    @Test
    public void testAlreadyAddedWhiteArtifact() throws Exception {
        String type = "productAdd";

        ClientResponse<String> response = manipulateEntity(ListEntityType.PRODUCT,
                OperationType.POST, type, true);

        type = generator.returnWhiteArtifactString("org.jboss.da", "dependency-analyzer", "0.3.0",
                getIdOfProduct("test", "1.0.0"));

        response = manipulateEntity(ListEntityType.WHITE, OperationType.POST, type, true);

        // add second white artifact
        response = manipulateEntity(ListEntityType.WHITE, OperationType.POST, type, true);

        checkExpectedResponse(response, "successFalse");
    }

    @Test
    public void testGetAllWhiteArtifacts() throws Exception {
        // Add artifacts to whitelist
        String type = "productAdd";

        ClientResponse<String> response = manipulateEntity(ListEntityType.PRODUCT,
                OperationType.POST, type, true);

        type = "productAdd2";

        response = manipulateEntity(ListEntityType.PRODUCT, OperationType.POST, type, true);
        // Add artifacts to whitelist
        type = generator.returnWhiteArtifactString("org.jboss.da", "dependency-analyzer", "0.3.0",
                getIdOfProduct("test", "1.0.0"));

        response = manipulateEntity(ListEntityType.WHITE, OperationType.POST, type, true);

        type = generator.returnWhiteArtifactString("org.jboss.da", "dependency-analyzer", "0.3.0",
                getIdOfProduct("test", "2.0.0"));

        response = manipulateEntity(ListEntityType.WHITE, OperationType.POST, type, true);
        // Get list

        response = new ClientRequest(restApiURL + PATH_WHITE_LIST).get(String.class);
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

        ClientResponse<String> response = manipulateEntity(ListEntityType.PRODUCT,
                OperationType.POST, type, true);

        type = generator.returnWhiteArtifactString("org.jboss.da", "dependency-analyzer",
                "0.3.0.redhat-1", getIdOfProduct("test", "1.0.0"));

        response = manipulateEntity(ListEntityType.WHITE, OperationType.POST, type, true);

        response = new ClientRequest(restApiURL + PATH_WHITE_LIST
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

        ClientResponse<String> response = manipulateEntity(ListEntityType.PRODUCT,
                OperationType.POST, type, true);

        type = generator.returnWhiteArtifactString("org.jboss.da", "dependency-analyzer",
                "0.3.0.redhat-1", getIdOfProduct("test", "1.0.0"));

        response = manipulateEntity(ListEntityType.WHITE, OperationType.POST, type, true);

        response = new ClientRequest(restApiURL + PATH_WHITE_LIST
                + "?groupid=org.jboss.da&artifactid=dependency-analyzer&version=0.3.0")
                .get(String.class);
        assertEquals(200, response.getStatus());

        checkExpectedResponse(response, "gavRhResponse");
    }

    private ClientResponse<String> manipulateEntity(ListEntityType entity, OperationType operation,
            String file, Boolean checkSuccess) throws Exception {
        String type = file;
        String requestString = null;
        if (entity.equals(ListEntityType.WHITE) && operation.equals(OperationType.POST)) {
            requestString = file;
        } else {
            File jsonRequestFile = getJsonRequestFile(PATH_FILES_LISTINGS_GAV, type);
            requestString = FileUtils.readFileToString(jsonRequestFile, ENCODING);
        }
        String path = null;
        switch (entity) {
            case WHITE:
                path = PATH_WHITE_LISTINGS_GAV;
                break;
            case BLACK:
                path = PATH_BLACK_LISTINGS_GAV;
                break;
            case PRODUCT:
                path = PATH_PRODUCT;
                break;
        }

        ClientRequest request = createClientRequest(path, requestString);
        ClientResponse<String> response = null;
        switch (operation) {
            case POST:
                response = request.post(String.class);
                break;

            case DELETE:
                response = request.delete(String.class);
                break;
            case PUT:
                response = request.put(String.class);
                break;
        }
        if (checkSuccess)
            assertEquals(200, response.getStatus());
        return response;
    }

    private void checkBlacklistingArtifactTest(String artifact1, String artifact2, String addToBL)
            throws Exception {
        manipulateEntity(ListEntityType.WHITE, OperationType.POST, artifact1, true);
        if (artifact2 != null) {
            manipulateEntity(ListEntityType.WHITE, OperationType.POST, artifact2, true);
        }
        manipulateEntity(ListEntityType.BLACK, OperationType.POST, addToBL, true);

        assertEquals(0, getAllArtifactsFromList(PATH_WHITE_LIST).size());
        assertEquals(1, getAllArtifactsFromList(PATH_BLACK_LIST).size());
        assertEquals("1.0.0", getAllArtifactsFromList(PATH_BLACK_LIST).get(0).getVersion());
    }

    private void checkArtifactTest(String artifact1, String artifact2, String testVersion,
            String expectedResult) throws Exception {
        manipulateEntity(ListEntityType.WHITE, OperationType.POST, artifact1, true);

        if (artifact2 != null) {
            manipulateEntity(ListEntityType.WHITE, OperationType.POST, artifact2, true);
        }

        System.out.println(restApiURL + PATH_WHITE_ARTIFACTS
                + "?groupid=org.jboss.da&artifactid=dependency-analyzer&version=" + testVersion);

        ClientResponse<String> response = new ClientRequest(restApiURL + PATH_WHITE_ARTIFACTS
                + "?groupid=org.jboss.da&artifactid=dependency-analyzer&version=" + testVersion)
                .get(String.class);
        System.out.println("RESULT:" + response.getEntity());
        assertEquals(200, response.getStatus());
        checkExpectedResponse(response, expectedResult);
    }

    private void checkExpectedResponse(ClientResponse<String> response, String expectedFile)
            throws IOException {
        File expectedResponseFile = getJsonResponseFile(PATH_FILES_LISTINGS_GAV, expectedFile);
        assertEqualsJson(readFileToString(expectedResponseFile), response.getEntity(String.class));
    }

    private List<RestArtifact> whiteToRestArtifactList(List<RestWhiteArtifact> whites) {
        List<RestArtifact> artifacts = new ArrayList<RestArtifact>();
        for (RestWhiteArtifact w : whites) {
            RestArtifact a = new RestArtifact();
            a.setArtifactId(w.getGav().artifactId);
            a.setGroupId(w.getGav().getGroupId());
            a.setVersion(w.getGav().getVersion());
            artifacts.add(a);
        }
        return artifacts;
    }

    private long getIdOfProduct(String name, String version) throws Exception {
        ClientResponse<RestProduct[]> r = new ClientRequest(restApiURL + PATH_PRODUCT + "?name="
                + name + "&version=" + version).get(RestProduct[].class);
        return r.getEntity()[0].getId();
    }
}
