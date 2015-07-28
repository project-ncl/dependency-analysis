package org.jboss.da.rest.reports;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.jboss.da.communication.aprox.model.GAV;
import org.jboss.da.reports.backend.api.VersionFinder;
import org.jboss.da.rest.reports.model.GAVRequest;
import org.jboss.da.rest.reports.model.LookupReport;
import org.jboss.da.rest.reports.model.Report;
import org.jboss.da.rest.reports.model.SCMRequest;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.List;

/**
 * Main end point for the reports
 * 
 * @author Dustin Kut Moy Cheung
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
@Path("/reports")
@Api(value = "/reports", description = "Get report of dependencies of projects")
public class Reports {

    @Inject
    private VersionFinder versionFinder;

    @POST
    @Path("/scm")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get dependency report for a project specified in a repository url")
    public Report scmGenerator(SCMRequest scmRequest) {
        // TODO Auto-generated method stub
        return null;
    }

    @POST
    @Path("/gav")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get dependency report for a GAV")
    public Report gavGenerator(GAVRequest gavRequest) {
        // TODO Auto-generated method stub
        return null;
    }

    @POST
    @Path("/lookup/gav")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get best matching version for the list of GAVs provided")
    public List<LookupReport> lookupGav(List<GAV> gavRequest) {
        List<LookupReport> reportsList = new ArrayList<>();
        gavRequest.forEach((gav) -> reportsList.add(toLookupReport(gav)));
        return reportsList;
    }

    private LookupReport toLookupReport(GAV gav) {
        return new LookupReport(gav, versionFinder.getBestMatchVersionFor(gav));
    }
}
