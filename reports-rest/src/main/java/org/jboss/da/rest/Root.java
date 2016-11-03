package org.jboss.da.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static org.jboss.da.common.Constants.REST_API_VERSION_BC;
import static org.jboss.da.common.Constants.REST_API_VERSION_REPORTS;
import static org.jboss.da.common.Constants.DA_VERSION;
import static org.jboss.da.common.Constants.COMMIT_HASH;

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
