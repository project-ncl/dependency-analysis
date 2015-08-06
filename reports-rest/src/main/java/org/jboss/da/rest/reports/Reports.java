package org.jboss.da.rest.reports;

import org.jboss.da.communication.CommunicationException;
import org.jboss.da.communication.model.GAV;
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

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

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
    @ApiOperation(value = "Lookup built versions for the list of provided GAVs")
    public List<LookupReport> lookupGav(List<GAV> gavRequest) throws CommunicationException {
        List<LookupReport> reportsList = new ArrayList<>();
        for (GAV gav : gavRequest) {
            reportsList.add(toLookupReport(gav));
        }
        return reportsList;
    }

    private LookupReport toLookupReport(GAV gav) throws CommunicationException {
        return new LookupReport(gav, versionFinder.getBestMatchVersionFor(gav));
    }
}
