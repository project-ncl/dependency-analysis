package org.jboss.da.reports.rest;

import org.jboss.da.reports.rest.model.GAVRequest;
import org.jboss.da.reports.rest.model.Report;
import org.jboss.da.reports.rest.model.SCMRequest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/generator")
public class Generator {

    @POST
    @Path("scm")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Report scmGenerator(SCMRequest scmRequest) {
        // TODO: placeholder for now
        return null;
    }

    @POST
    @Path("gav")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Report gavGenerator(GAVRequest gavRequest) {
        // TODO: placeholder for now
        return null;
    }
}
