package org.jboss.da.test.client.ws.bc;

import org.apache.commons.io.FileUtils;

import java.io.IOException;

import org.json.JSONException;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Ignore;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.File;
import java.util.Map;
import java.util.Random;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

public class WebsocketApiBCTest extends AbstractWebsocketBCTest {

    private static final String PATH_BC_START = "/build-configuration/generate/product/start-process";

    private static final String METHOD_BC_START = "buildConfiguration.product.start";

    private static final String PATH_BC_ITERATE = "/build-configuration/generate/product/analyse-next-level";

    private static final String METHOD_BC_ITERATE = "buildConfiguration.product.nextLevel";

    private static final String PATH_BC_FINISH = "/build-configuration/generate/product/finish-process";

    private static final String METHOD_BC_FINISH = "buildConfiguration.product.finish";

    @Test
    public void testStartDAParent() throws IOException, Exception {
        assertResponseForRequest(PATH_BC_START, "da-parent", METHOD_BC_START);
    }

    @Test
    public void testIterateDAApplication() throws IOException, Exception {
        assertResponseForRequest(PATH_BC_ITERATE, "da-application-1", METHOD_BC_ITERATE);
    }

    @Test
    public void testStartDACommon() throws IOException, Exception {
        assertResponseForRequest(PATH_BC_START, "da-common", METHOD_BC_START);
    }

    @Test
    public void testIterateDACommon() throws IOException, Exception {
        assertResponseForRequest(PATH_BC_ITERATE, "da-common-1", METHOD_BC_ITERATE);
    }

    @Test
    @Ignore
    public void testFinishDAParent() throws IOException, Exception {
        File jsonRequestFile = getJsonRequestFile(PATH_BC_FINISH, "da-parent");
        String input = FileUtils.readFileToString(jsonRequestFile, "utf-8");
        String number = Integer.toString(new Random().nextInt());
        input = input.replace("PLACEHOLDER", number);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> parameters = mapper.readValue(input, Map.class);
        JSONRPC2Request jsonRequest = new JSONRPC2Request(METHOD_BC_FINISH, parameters, 1);

        JSONRPC2Response response = endpoint.sendRequest(jsonRequest);
        String responseString = mapper.writeValueAsString(response.getResult());

        assertTrue(response.indicatesSuccess());
        System.out.println("Response: " + responseString);
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
