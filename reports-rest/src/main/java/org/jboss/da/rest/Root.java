package org.jboss.da.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static org.jboss.da.common.version.Constants.REST_API_VERSION;

/**
 *
 * @author Jozef Mrazek <jmrazek@redhat.com>
 *
 */
@Path("/")
public class Root {

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getDescription() {
        return "<h1>Dependency analyzer REST</h1>"
                + "\n"
                + "<ul><li><strong>Version:</strong> "
                + REST_API_VERSION
                + "</li>"
                + "\n"
                + "<li><a href=\"../../doc\">Swagger documentation</a></li>"
                + "\n"
                + "<li><strong>REST proposal documentation:</strong> <a href=\"https://docs.engineering.redhat.com/display/JP/REST+endpoints+proposal\">https://docs.engineering.redhat.com/display/JP/REST+endpoints+proposal</a></li></ul>";
    }

}
