package org.jboss.da.test.client.rest.listings;

import org.apache.commons.io.FileUtils;
import org.jboss.da.test.client.rest.AbstractRestReportsTest;
import org.junit.After;

import java.io.File;
import java.io.IOException;
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
        BLACK;
    }

    protected enum OperationType {
        POST, DELETE, PUT;
    }

    protected static final String PATH_FILES_LISTINGS_GAV = "/listings";

    protected static final String PATH_BLACK_LIST = "/listings/blacklist";

    public static final String PATH_BLACK_LISTINGS_GAV = "/listings/blacklist/gav";

    protected static final String PATH_BLACK_LISTINGS_GA = "/listings/blacklist/ga";

    @After
    public void dropTables() throws Exception {
        List<RestArtifact> blacklistedArtifacts = getAllArtifactsFromList(PATH_BLACK_LIST);
        blacklistedArtifacts.forEach(gav -> removeGavFromList(PATH_BLACK_LISTINGS_GAV, gav));
    }

    private void removeGavFromList(String listUrl, RestArtifact gav) {
        try {
            createClientRequest(listUrl).method("DELETE", Entity.json(gav), String.class);
        } catch (Exception e) {
            fail("Failed to remove GAV from the list using URL " + listUrl);
        }
    }

    private String toRestProductRequest(RestProduct p) {
        return "{" + "\"name\":" + "\"" + p.getName() + "\"," + "\"version\":" + "\"" + p.getVersion() + "\"" + "}";
    }

    protected List<RestArtifact> getAllArtifactsFromList(String listUrl) throws Exception {
        return processGetRequest(new GenericType<List<RestArtifact>>() {
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
            case BLACK:
                path = PATH_BLACK_LISTINGS_GAV;
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

}
