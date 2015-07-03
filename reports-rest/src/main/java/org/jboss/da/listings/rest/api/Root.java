package org.jboss.da.listings.rest.api;

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
public interface Root {

    @GET
    @Produces(MediaType.TEXT_HTML)
    String getDescription();
}
