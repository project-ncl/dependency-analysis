package org.jboss.da.test.client.rest.reports;

import static org.junit.Assert.assertEquals;

import org.apache.commons.io.FileUtils;
import org.jboss.da.test.client.rest.AbstractRestReportsTest;
import org.json.JSONException;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import java.io.File;

public class RestApiReportsRemoteTest extends AbstractRestReportsTest {

    private static final String ENCODING = "utf-8";

    private static final String PATH_REPORTS_GAV = "/reports/gav";

    static final String PATH_REPORTS_ALIGN = "/reports/align";

    private static final String PATH_LOOKUP_GAVS = "/reports/lookup/gavs";

    private static final String PATH_SCM = "/reports/scm";

    @Test
    public void testGavReportBasic() throws Exception {
        Response response = assertResponseForRequest(PATH_REPORTS_GAV, "guava18");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGavReportNonexisting() throws Exception {
        String gavNonexisting = "gavNonexisting";
        File jsonRequestFile = getJsonRequestFile(PATH_REPORTS_GAV, gavNonexisting);

        Response response = createClientRequest(PATH_REPORTS_GAV).post(
                Entity.json(FileUtils.readFileToString(jsonRequestFile, ENCODING)));

        assertEquals(404, response.getStatus());
    }

    @Test
    public void testGavLookupSingle() throws Exception {
        Response response = assertResponseForRequest(PATH_LOOKUP_GAVS, "guava13");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGavLookupList() throws Exception {
        Response response = assertResponseForRequest(PATH_LOOKUP_GAVS, "guava13List");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testScmReportBasic() throws Exception {
        Response response = assertResponseForRequest(PATH_SCM, "dependency-analysis");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testAlignReportBasic() throws Exception {
        Response response = assertResponseForRequest(PATH_REPORTS_ALIGN, "dependency-analysis");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testScmOptionalRepository() throws Exception {
        File jsonRequestFile = getJsonRequestFile(PATH_SCM, "keycloak-1.6.0.Final");

        Response response = createClientRequest(PATH_SCM).post(
                Entity.json(FileUtils.readFileToString(jsonRequestFile, ENCODING)));

        assertEquals(200, response.getStatus());
    }

    @Test
    public void testReportWithoutDependencies() throws Exception {
        Response responseWith = assertResponseForRequest(PATH_REPORTS_GAV, "withDependencies");
        assertEquals(200, responseWith.getStatus());
        Response responseWithout = assertResponseForRequest(PATH_REPORTS_GAV, "withoutDependencies");
        assertEquals(200, responseWithout.getStatus());
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
