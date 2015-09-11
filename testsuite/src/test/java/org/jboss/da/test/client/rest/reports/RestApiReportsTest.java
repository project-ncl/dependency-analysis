package org.jboss.da.test.client.rest.reports;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.junit.Assert.assertEquals;

import org.apache.commons.io.FileUtils;
import org.jboss.da.test.client.AbstractRestApiTest;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Test;

import javax.ws.rs.core.MediaType;

import java.io.File;

public class RestApiReportsTest extends AbstractRestApiTest {

    private static String ENCODING = "utf-8";

    private static String GAV_GUAVA = "guava";

    private static String GAV_NONEXISTING = "gavNonexisting";

    private static String PATH_REPORTS_GAV = "/reports/gav";

    @Test
    public void gavReportBasicTest() throws Exception {
        File jsonRequestFile = new RequestFilenameBuilder(restApiRequestFolder, PATH_REPORTS_GAV,
                APPLICATION_JSON, GAV_GUAVA).getFile();

        ClientRequest request = createClientRequest(
                FileUtils.readFileToString(jsonRequestFile, ENCODING));
        
        ClientResponse<String> response = request.post(String.class);

        File expectedResponseFile = new ExpectedResponseFilenameBuilder(
                restApiExpectedResponseFolder, PATH_REPORTS_GAV, APPLICATION_JSON, GAV_GUAVA)
                .getFile();

        assertEquals(readFileToString(expectedResponseFile).trim(), response
                .getEntity(String.class).trim());
    }

    @Test
    public void gavReportNonexistingTest() throws Exception {
        File jsonRequestFile = new RequestFilenameBuilder(restApiRequestFolder, PATH_REPORTS_GAV,
                APPLICATION_JSON, GAV_NONEXISTING).getFile();

        ClientRequest request = createClientRequest(
                FileUtils.readFileToString(jsonRequestFile, ENCODING));

        ClientResponse<String> response = request.post(String.class);

        assertEquals(404, response.getStatus());
    }


    private ClientRequest createClientRequest(String jsonRequest) {
        ClientRequest request = new ClientRequest(restApiURL + PATH_REPORTS_GAV);
        request.header("Content-Type", APPLICATION_JSON);
        request.body(MediaType.APPLICATION_JSON_TYPE, jsonRequest);
        return request;
    }

}
