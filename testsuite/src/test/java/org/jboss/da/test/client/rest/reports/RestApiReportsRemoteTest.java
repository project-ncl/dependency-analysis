package org.jboss.da.test.client.rest.reports;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.junit.Assert.assertEquals;

import org.apache.commons.io.FileUtils;
import org.jboss.da.test.client.AbstractRestReportsTest;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Test;

import javax.ws.rs.core.MediaType;

import java.io.File;

public class RestApiReportsRemoteTest extends AbstractRestReportsTest {

    private static String ENCODING = "utf-8";

    private static String PATH_REPORTS_GAV = "/reports/gav";

    private static String PATH_LOOKUP_GAVS = "/reports/lookup/gavs";

    private static String PATH_SCM = "/reports/scm";

    @Test
    public void testGavReportBasic() throws Exception {
        String gavGuava18 = "guava18";
        File jsonRequestFile = getJsonRequestFile(PATH_REPORTS_GAV, gavGuava18);

        ClientRequest request = createClientRequest(PATH_REPORTS_GAV,
                FileUtils.readFileToString(jsonRequestFile, ENCODING));

        ClientResponse<String> response = request.post(String.class);

        File expectedResponseFile = new ExpectedResponseFilenameBuilder(
                restApiExpectedResponseFolder, PATH_REPORTS_GAV, APPLICATION_JSON, gavGuava18)
                .getFile();

        assertEqualsJson(readFileToString(expectedResponseFile).trim(),
                response.getEntity(String.class).trim());
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGavReportNonexisting() throws Exception {
        String gavNonexisting = "gavNonexisting";
        File jsonRequestFile = getJsonRequestFile(PATH_REPORTS_GAV, gavNonexisting);

        ClientRequest request = createClientRequest(PATH_REPORTS_GAV,
                FileUtils.readFileToString(jsonRequestFile, ENCODING));

        ClientResponse<String> response = request.post(String.class);

        assertEquals(404, response.getStatus());
    }

    @Test
    public void testGavLookupSingle() throws Exception {
        String gavGuava13 = "guava13";
        File jsonRequestFile = getJsonRequestFile(PATH_LOOKUP_GAVS, gavGuava13);

        ClientRequest request = createClientRequest(PATH_LOOKUP_GAVS,
                FileUtils.readFileToString(jsonRequestFile, ENCODING));

        ClientResponse<String> response = request.post(String.class);

        File expectedResponseFile = new ExpectedResponseFilenameBuilder(
                restApiExpectedResponseFolder, PATH_LOOKUP_GAVS, APPLICATION_JSON, gavGuava13)
                .getFile();

        assertEqualsJson(readFileToString(expectedResponseFile).trim(),
                response.getEntity(String.class).trim());
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGavLookupList() throws Exception {
        String gavGuava13List = "guava13List";
        File jsonRequestFile = getJsonRequestFile(PATH_LOOKUP_GAVS, gavGuava13List);

        ClientRequest request = createClientRequest(PATH_LOOKUP_GAVS,
                FileUtils.readFileToString(jsonRequestFile, ENCODING));

        ClientResponse<String> response = request.post(String.class);

        File expectedResponseFile = new ExpectedResponseFilenameBuilder(
                restApiExpectedResponseFolder, PATH_LOOKUP_GAVS, APPLICATION_JSON, gavGuava13List)
                .getFile();

        assertEqualsJson(readFileToString(expectedResponseFile).trim(),
                response.getEntity(String.class).trim());
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testScmReportBasic() throws Exception {
        String dependencyAnalysis = "dependency-analysis";
        File jsonRequestFile = getJsonRequestFile(PATH_SCM, dependencyAnalysis);

        ClientRequest request = createClientRequest(PATH_SCM,
                FileUtils.readFileToString(jsonRequestFile, ENCODING));

        ClientResponse<String> response = request.post(String.class);

        File expectedResponseFile = new ExpectedResponseFilenameBuilder(
                restApiExpectedResponseFolder, PATH_SCM, APPLICATION_JSON, dependencyAnalysis)
                .getFile();

        assertEqualsJson(readFileToString(expectedResponseFile).trim(),
                response.getEntity(String.class).trim());
        assertEquals(200, response.getStatus());
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
