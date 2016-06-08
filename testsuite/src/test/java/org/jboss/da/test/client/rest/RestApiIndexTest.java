package org.jboss.da.test.client.rest;

import org.apache.http.entity.ContentType;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Test;

import static org.apache.http.entity.ContentType.TEXT_HTML;
import static org.jboss.da.common.Constants.COMMIT_HASH;
import static org.jboss.da.common.Constants.DA_VERSION;
import static org.jboss.da.common.Constants.REST_API_VERSION_BC;
import static org.jboss.da.common.Constants.REST_API_VERSION_REPORTS;
import static org.junit.Assert.assertEquals;

public class RestApiIndexTest extends AbstractRestReportsTest {

    @Test
    public void testIndexHtml() throws Exception {
        String path = "/";
        ContentType contentType = TEXT_HTML;
        ClientRequest request = new ClientRequest(restApiURL + path);
        request.header("Content-Type", contentType);

        ClientResponse<String> response = request.get(String.class);

        assertEquals(getExpectedResponse(), response.getEntity(String.class));
    }

    private String getExpectedResponse() {
        return "<h1>Dependency analysis service REST API</h1>"
                + "\n"
                + "<ul><li><strong>DA Version:</strong> "
                + DA_VERSION
                + "</li>"
                + "\n"
                + "<ul><li><strong>Commit Hash:</strong> "
                + COMMIT_HASH
                + "</li>"
                + "\n"
                + "<li><strong>BC REST API Version:</strong> "
                + REST_API_VERSION_BC
                + "</li>"
                + "\n"
                + "<li><strong>Reports REST API Version:</strong> "
                + REST_API_VERSION_REPORTS
                + "</li>"
                + "\n"
                + "<li><a href=\"../doc\">Swagger documentation</a></li>"
                + "\n"
                + "<li><strong>REST proposal documentation:</strong> <a href=\"https://docs.engineering.redhat.com/display/JP/REST+endpoints+proposal\">https://docs.engineering.redhat.com/display/JP/REST+endpoints+proposal</a></li></ul>";
    }

}
