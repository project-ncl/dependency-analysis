package org.jboss.da.test.client.rest.listings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.jboss.da.test.client.rest.AbstractRestReportsTest;
import org.junit.jupiter.api.AfterEach;

/**
 *
 * @author jbrazdil
 */
public abstract class AbstractRestApiListingTest extends AbstractRestReportsTest {

    protected enum ListEntityType {
        BLACK
    }

    protected enum OperationType {
        POST, DELETE, PUT
    }

    protected static final String PATH_FILES_LISTINGS_GAV = "/listings";

    protected static final String PATH_BLACK_LIST = "/listings/blacklist";

    public static final String PATH_BLACK_LISTINGS_GAV = "/listings/blacklist/gav";

    protected static final String PATH_BLACK_LISTINGS_GA = "/listings/blacklist/ga";

    @AfterEach
    public void dropTables() {
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

    protected List<RestArtifact> getAllArtifactsFromList(String listUrl) {
        return processGetRequest(new GenericType<>() {
        }, listUrl);
    }

    private <T> T processGetRequest(GenericType<T> type, String url) {
        Response response = createClientRequest(url).get();

        if (response.getStatus() != 200) {
            System.out.println("Response: " + response.readEntity(String.class));
            fail("Failed to get entity via REST API. Status " + response.getStatusInfo());
        }

        return response.readEntity(type);
    }

    protected String readJsonFile(String file) throws IOException {
        File jsonRequestFile = getJsonRequestFile(PATH_FILES_LISTINGS_GAV, file);
        return FileUtils.readFileToString(jsonRequestFile, ENCODING);
    }

    protected Response manipulateEntityFile(OperationType operation, String file) throws Exception {
        return manipulateEntityString(operation, readJsonFile(file));
    }

    protected Response manipulateEntityString(OperationType operation, String requestString) {
        String path = PATH_BLACK_LISTINGS_GAV;
        Response response = switch (operation) {
            case POST -> createClientRequest(path).post(Entity.json(requestString));
            case DELETE -> createClientRequest(path).method("DELETE", Entity.json(requestString));
            case PUT -> createClientRequest(path).put(Entity.json(requestString));
        };
        assertEquals(200, response.getStatus());
        return response;
    }

}
