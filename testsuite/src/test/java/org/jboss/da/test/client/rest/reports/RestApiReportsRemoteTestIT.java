package org.jboss.da.test.client.rest.reports;

import static org.junit.Assert.assertEquals;

import org.apache.commons.io.FileUtils;
import org.jboss.da.test.client.rest.AbstractRestReportsTest;
import org.json.JSONException;
import static org.junit.Assert.fail;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import java.io.File;

public class RestApiReportsRemoteTestIT extends AbstractRestReportsTest {

    private static final String ENCODING = "utf-8";

    static final String PATH_REPORTS_ALIGN = "/reports/align";

    private static final String PATH_LOOKUP_GAVS = "/reports/lookup/gavs";

    private static final String PATH_LOOKUP_NPM = "/reports/lookup/npm";

    private static final String PATH_VERSIONS_NPM = "/reports/versions/npm";

    private static final String PATH_SCM = "/reports/scm";

    @Before
    public void workaroundNoHttpResponseException() throws InterruptedException {
        Thread.sleep(2000);
    }

    @Test
    public void testNPMLookupSingle() throws Exception {
        Response response = assertResponseForRequest(PATH_LOOKUP_NPM, "jquery151");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testNPMVersionsSingle() throws Exception {
        Response response = assertResponseForRequest(PATH_VERSIONS_NPM, "jquery151");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGavLookupSingle() throws Exception {
        Response response = assertResponseForRequest(PATH_LOOKUP_GAVS, "guava13");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGavLookupSingleTemporary() throws Exception {
        Response response = assertResponseForRequest(PATH_LOOKUP_GAVS, "guava13Temp");
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
        final String repo = System.getenv("DA_hosted_repo");
        Assume.assumeTrue(repo != null);
        File jsonRequestFile = getJsonRequestFile(PATH_SCM, "keycloak-1.6.0.Final");
        String json = FileUtils.readFileToString(jsonRequestFile, ENCODING);
        json = json.replace("${DA-hosted-repo}", repo);

        Response response = createClientRequest(PATH_SCM).post(Entity.json(json));

        assertEquals(200, response.getStatus());
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
