package org.jboss.da.reports.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/testme")
public class TestMe {
    @GET
    @Produces("text/plain")
    public String getSomething() {
        return "Hi Jack";
    }
}
