package org.jboss.da.reports.rest;

import org.jboss.da.reports.api.GAV;
import org.jboss.da.reports.api.LookupReport;
import org.jboss.da.reports.rest.model.GAVRequest;
import org.jboss.da.reports.rest.model.Report;
import org.jboss.da.reports.rest.model.SCMRequest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/generator")
public interface Generator {

    @POST
    @Path("scm")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Report scmGenerator(SCMRequest scmRequest);

    @POST
    @Path("gav")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Report gavGenerator(GAVRequest gavRequest);

    @POST
    @Path("lookups/gav")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<LookupReport> lookupGenerator(List<GAV> gavs);

}
