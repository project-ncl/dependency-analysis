package org.jboss.da.test.client.rest.reports;

import org.apache.commons.io.FileUtils;
import org.apache.http.entity.ContentType;
import org.jboss.da.test.client.AbstractRestApiTest;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.io.File;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.junit.Assert.assertEquals;

public class RestApiReportsTest extends AbstractRestApiTest {

    @Test
    public void testGav() throws Exception {
        String path = "/reports/gav";
        ContentType contentType = APPLICATION_JSON;
        File jsonRequestFile = new RequestFilenameBuilder(restApiRequestFolder, path).getFile();
        String jsonRequest = FileUtils.readFileToString(jsonRequestFile, "utf-8");
        ClientRequest request = new ClientRequest(restApiURL + path);
        request.header("Content-Type", contentType);
        request.body(MediaType.APPLICATION_JSON_TYPE, jsonRequest);

        ClientResponse<String> response = request.post(String.class);

        File expectedResponseFile = new ExpectedResponseFilenameBuilder(
                restApiExpectedResponseFolder, path, contentType).getFile();
        assertEquals(readFileToString(expectedResponseFile), response.getEntity(String.class));
    }

    @Test
    public void testNonexistingGav() throws Exception {
        String path = "/reports/gav";
        ContentType contentType = APPLICATION_JSON;
        String variant = "nonexisting";
        File jsonRequestFile = new RequestFilenameBuilder(restApiRequestFolder, path, contentType,
                variant).getFile();
        String jsonRequest = FileUtils.readFileToString(jsonRequestFile, "utf-8");
        ClientRequest request = new ClientRequest(restApiURL + path);
        request.header("Content-Type", contentType);
        request.body(MediaType.APPLICATION_JSON_TYPE, jsonRequest);

        ClientResponse<String> response = request.post(String.class);

        assertEquals(404, response.getStatus());
    }
}
