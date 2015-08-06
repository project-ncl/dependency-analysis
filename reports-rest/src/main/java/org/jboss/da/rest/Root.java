package org.jboss.da.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
                + "<ul><li><strong>Version:</strong> 1.0.0</li>"
                + "<li><strong>Swagger documentation:</strong>/da/doc</li>"
                + "<li><strong>REST proposal documentation:</strong> https://docs.engineering.redhat.com/display/JP/REST+endpoints+proposal</li></ul>";
    }

}
