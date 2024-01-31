package org.jboss.da.test.client.rest.listings;

import org.apache.commons.io.FileUtils;
import org.jboss.da.test.client.rest.AbstractRestReportsTest;
import org.junit.After;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

/**
 *
 * @author jbrazdil
 */
public abstract class AbstractRestApiListingTest extends AbstractRestReportsTest {

    protected enum ListEntityType {
        BLACK, WHITE, PRODUCT;
    }

    protected enum OperationType {
        POST, DELETE, PUT;
    }

    protected static final String PATH_FILES_LISTINGS_GAV = "/listings";

    protected static final String PATH_WHITE_LIST = "/listings/whitelist";

    protected static final String PATH_BLACK_LIST = "/listings/blacklist";

    protected static final String PATH_WHITE_LISTINGS_GAV = "/listings/whitelist/gav";

    protected static final String PATH_WHITE_ARTIFACTS = "/listings/whitelist/artifacts/gav";

    public static final String PATH_BLACK_LISTINGS_GAV = "/listings/blacklist/gav";

    protected static final String PATH_BLACK_LISTINGS_GA = "/listings/blacklist/ga";

    protected static final String PATH_PRODUCT = "/listings/whitelist/product";

    protected static final String PATH_PRODUCTS = "/listings/whitelist/products";

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
            createClientRequest(listUrl).method("DELETE", Entity.json(gav), String.class);
        } catch (Exception e) {
            fail("Failed to remove GAV from the list using URL " + listUrl);
        }
    }

    private void removeProductFromList(String url, RestProduct product) {
        try {
            createClientRequest(url).method("DELETE", Entity.json(toRestProductRequest(product)), String.class);
        } catch (Exception e) {
            fail("Failed to remove product from the list using URL " + url);
        }

    }

    private String toRestProductRequest(RestProduct p) {
        return "{" + "\"name\":" + "\"" + p.getName() + "\"," + "\"version\":" + "\"" + p.getVersion() + "\"" + "}";
    }

    protected List<RestArtifact> getAllArtifactsFromList(String listUrl) throws Exception {
        return processGetRequest(new GenericType<List<RestArtifact>>() {
        }, listUrl);
    }

    private List<RestWhiteArtifact> getAllWhiteArtifactsFromList(String listUrl) throws Exception {
        return processGetRequest(new GenericType<List<RestWhiteArtifact>>() {
        }, listUrl);
    }

    private List<RestProduct> getAllProductsFromList(String listUrl) throws Exception {
        return processGetRequest(new GenericType<List<RestProduct>>() {
        }, listUrl);
    }

    private <T> T processGetRequest(GenericType<T> type, String url) throws Exception {
        Response response = createClientRequest(url).get();

        if (response.getStatus() != 200) {
            System.out.println("Respose: " + response.readEntity(String.class));
            fail("Failed to get entity via REST API. Status " + response.getStatusInfo());
        }

        return response.readEntity(type);
    }

    protected String readJsonFile(String file) throws IOException {
        File jsonRequestFile = getJsonRequestFile(PATH_FILES_LISTINGS_GAV, file);
        return FileUtils.readFileToString(jsonRequestFile, ENCODING);
    }

    protected Response manipulateEntityFile(
            ListEntityType entity,
            OperationType operation,
            String file,
            Boolean checkSuccess) throws Exception {
        return manipulateEntityString(entity, operation, readJsonFile(file), checkSuccess);
    }

    protected Response manipulateEntityString(
            ListEntityType entity,
            OperationType operation,
            String requestString,
            Boolean checkSuccess) throws Exception {
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

        Invocation.Builder request = createClientRequest(path);
        Response response;
        switch (operation) {
            case POST:
                response = request.post(Entity.json(requestString));
                break;

            case DELETE:
                response = request.method("DELETE", Entity.json(requestString));
                break;
            case PUT:
                response = request.put(Entity.json(requestString));
                break;
            default:
                throw new UnsupportedOperationException("Unknonw operation " + operation);
        }
        if (checkSuccess)
            assertEquals(200, response.getStatus());
        return response;
    }

    private List<RestArtifact> whiteToRestArtifactList(List<RestWhiteArtifact> whites) {
        List<RestArtifact> artifacts = new ArrayList<>();
        for (RestWhiteArtifact w : whites) {
            RestArtifact a = new RestArtifact();
            a.setArtifactId(w.getGav().artifactId);
            a.setGroupId(w.getGav().getGroupId());
            a.setVersion(w.getGav().getVersion());
            artifacts.add(a);
        }
        return artifacts;
    }

    protected long getIdOfProduct(String name, String version) throws Exception {
        return createClientRequest(PATH_PRODUCT + "?name=" + name + "&version=" + version).get(RestProduct[].class)[0]
                .getId();
    }
}
