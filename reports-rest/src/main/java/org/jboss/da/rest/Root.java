package org.jboss.da.rest;

import static org.jboss.da.common.Constants.COMMIT_HASH;
import static org.jboss.da.common.Constants.DA_VERSION;
import static org.jboss.da.common.Constants.REST_API_VERSION_BC;
import static org.jboss.da.common.Constants.REST_API_VERSION_REPORTS;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 *
 * @author Jozef Mrazek &lt;jmrazek@redhat.com&gt;
 *
 */
@Path("/")
public class Root {

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getDescription() {
        return String.format(
                """
                        <h1>Dependency analysis service REST API</h1>
                        <ul><li><strong>DA Version:</strong> %s</li>
                        <ul><li><strong>Commit Hash:</strong> %s</li>
                        <li><strong>BC REST API Version:</strong> %s</li>
                        <li><strong>Reports REST API Version:</strong> %s</li>
                        <li><a href="../../q/openapi">Swagger documentation</a></li>
                        <li><a href="../../q/health">Health Check URL</a></li>
                        <li><strong>REST proposal documentation:</strong> <a href="https://docs.engineering.redhat.com/display/JP/REST+endpoints+proposal">https://docs.engineering.redhat.com/display/JP/REST+endpoints+proposal</a></li></ul>""",
                DA_VERSION,
                COMMIT_HASH,
                REST_API_VERSION_BC,
                REST_API_VERSION_REPORTS);
    }

}
