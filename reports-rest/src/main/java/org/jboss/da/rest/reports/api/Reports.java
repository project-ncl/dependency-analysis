package org.jboss.da.rest.reports.api;

import org.jboss.da.rest.reports.api.model.GAVRequest;
import org.jboss.da.rest.reports.api.model.Report;
import org.jboss.da.rest.reports.api.model.SCMRequest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/reports")
public interface Reports {

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
    

}
