package org.jboss.da.test.client.rest.bc;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.jboss.da.test.client.rest.AbstractRestBCTest;
import org.json.JSONException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Ignore;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

public class RestApiBCTest extends AbstractRestBCTest {

    static final String PATH_BC_START = "/build-configuration/generate/product/start-process";

    private static final String PATH_BC_ITERATE = "/build-configuration/generate/product/analyse-next-level";

    private static final String PATH_BC_FINISH = "/build-configuration/generate/product/finish-process";

    @Test
    public void testStartDAParent() throws IOException, Exception {
        Response response = assertResponseForRequest(PATH_BC_START, "da-parent");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testIterateDAApplication() throws IOException, Exception {
        Response response = assertResponseForRequest(PATH_BC_ITERATE, "da-application-1");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testStartDACommon() throws IOException, Exception {
        Response response = assertResponseForRequest(PATH_BC_START, "da-common");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testIterateDACommon() throws IOException, Exception {
        Response response = assertResponseForRequest(PATH_BC_ITERATE, "da-common-1");
        assertEquals(200, response.getStatus());
    }

    @Test
    @Ignore
    public void testFinishDAParent() throws IOException, Exception {
        File jsonRequestFile = getJsonRequestFile(PATH_BC_FINISH, "da-parent");
        String input = FileUtils.readFileToString(jsonRequestFile, "utf-8");
        String number = Integer.toString(new Random().nextInt());
        Response response = createClientRequest(PATH_BC_FINISH).post(
                Entity.json(input.replace("PLACEHOLDER", number)));

        System.out.println("Response: " + response.readEntity(String.class));
    }

    @Override
    protected void assertEqualsJson(String expected, String actual) {
        try {
            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        } catch (JSONException ex) {
            fail("The test wasn't able to compare JSON strings" + ex);
        }
    }
}
