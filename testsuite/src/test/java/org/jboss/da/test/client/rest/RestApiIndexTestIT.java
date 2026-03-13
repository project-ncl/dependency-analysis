package org.jboss.da.test.client.rest;

import static org.jboss.da.common.Constants.COMMIT_HASH;
import static org.jboss.da.common.Constants.DA_VERSION;
import static org.jboss.da.common.Constants.REST_API_VERSION_BC;
import static org.jboss.da.common.Constants.REST_API_VERSION_REPORTS;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(value = H2DatabaseTestResource.class, restrictToAnnotatedClass = true)
public class RestApiIndexTestIT extends AbstractRestReportsTest {

    @Test
    public void testIndexHtml() {
        String path = "/";
        Response response = createWebTarget(path).request().get();

        assertEquals(getExpectedResponse(), response.readEntity(String.class));
    }

    private String getExpectedResponse() {
        return String.format(
                """
                        <h1>Dependency analysis service REST API</h1>
                        <ul><li><strong>DA Version:</strong> %s</li>
                        <ul><li><strong>Commit Hash:</strong> %s</li>
                        <li><strong>BC REST API Version:</strong> %s</li>
                        <li><strong>Reports REST API Version:</strong> %s</li>
                        <li><a href="../../q/openapi">Swagger documentation</a></li>
                        <li><strong>REST proposal documentation:</strong> <a href="https://docs.engineering.redhat.com/display/JP/REST+endpoints+proposal">https://docs.engineering.redhat.com/display/JP/REST+endpoints+proposal</a></li></ul>""",
                DA_VERSION,
                COMMIT_HASH,
                REST_API_VERSION_BC,
                REST_API_VERSION_REPORTS);
    }
}
