package org.jboss.da.rest.reports;

import org.jboss.da.rest.reports.model.GAVRequest;
import org.jboss.da.rest.reports.model.Report;
import org.jboss.da.rest.reports.model.SCMRequest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
}
