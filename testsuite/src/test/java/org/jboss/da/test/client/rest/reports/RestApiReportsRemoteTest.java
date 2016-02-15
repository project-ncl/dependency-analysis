package org.jboss.da.test.client.rest.reports;

import static org.junit.Assert.assertEquals;

import org.apache.commons.io.FileUtils;
import org.jboss.da.test.client.AbstractRestReportsTest;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Test;

import java.io.File;

public class RestApiReportsRemoteTest extends AbstractRestReportsTest {

    private static final String ENCODING = "utf-8";

    private static final String PATH_REPORTS_GAV = "/reports/gav";

    private static final String PATH_REPORTS_ALIGN = "/reports/align";

    private static final String PATH_LOOKUP_GAVS = "/reports/lookup/gavs";

    private static final String PATH_SCM = "/reports/scm";

    @Test
    public void testGavReportBasic() throws Exception {
        ClientResponse<String> response = assertResponseForRequest(PATH_REPORTS_GAV, "guava18");
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
        ClientResponse<String> response = assertResponseForRequest(PATH_LOOKUP_GAVS, "guava13");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGavLookupList() throws Exception {
        ClientResponse<String> response = assertResponseForRequest(PATH_LOOKUP_GAVS, "guava13List");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testScmReportBasic() throws Exception {
        ClientResponse<String> response = assertResponseForRequest(PATH_SCM, "dependency-analysis");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testAlignReportBasic() throws Exception {
        ClientResponse<String> response = assertResponseForRequest(PATH_REPORTS_ALIGN,
                "dependency-analysis");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testScmOptionalRepository() throws Exception {
        File jsonRequestFile = getJsonRequestFile(PATH_SCM, "keycloak-1.6.0.Final");
        ClientRequest request = createClientRequest(PATH_SCM,
                FileUtils.readFileToString(jsonRequestFile, ENCODING));
        ClientResponse<String> response = request.post(String.class);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testReportWithoutDependencies() throws Exception {
        ClientResponse<String> responseWith = assertResponseForRequest(PATH_REPORTS_GAV,
                "withDependencies");
        assertEquals(200, responseWith.getStatus());
        ClientResponse<String> responseWithout = assertResponseForRequest(PATH_REPORTS_GAV,
                "withoutDependencies");
        assertEquals(200, responseWithout.getStatus());
    }

}
