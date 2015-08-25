package org.jboss.da.rest.reports;

import org.apache.commons.lang.NotImplementedException;
import org.jboss.da.communication.CommunicationException;
import org.jboss.da.communication.model.GAV;
import org.jboss.da.listings.api.service.BlackArtifactService;
import org.jboss.da.listings.api.service.WhiteArtifactService;
import org.jboss.da.reports.api.ArtifactReport;
import org.jboss.da.reports.api.ReportsGenerator;
import org.jboss.da.reports.api.VersionLookupResult;
import org.jboss.da.reports.backend.api.VersionFinder;
import org.jboss.da.rest.reports.model.LookupReport;
import org.jboss.da.rest.reports.model.Report;
import org.jboss.da.rest.reports.model.SCMRequest;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import java.lang.invoke.MethodHandles;
import org.jboss.da.rest.model.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Inject
    private VersionFinder versionFinder;

    @Inject
    private ReportsGenerator reportsGenerator;

    @Inject
    private WhiteArtifactService whiteArtifactService;

    @Inject
    private BlackArtifactService blackArtifactService;

    @POST
    @Path("/scm")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get dependency report for a project specified in a repository URL",
            response = Report.class, hidden = true)
    // TODO unhide when the method will be implemented
    public Report scmGenerator(SCMRequest scmRequest) {
        throw new NotImplementedException();
    }

    @POST
    @Path("/gav")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Get dependency report for a GAV " // TODO change when dependencies will be implemented
                    + "(Currently the dependencies and dependency_versions_satisfied don't contains useful values)",
            response = ArtifactReport.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Report was successfully generated"),
            @ApiResponse(code = 404, message = "Requested GA was not found"),
            @ApiResponse(code = 502, message = "Communication with remote repository failed") })
    public Response gavGenerator(@ApiParam(
            value = "JSON Object with keys 'groupId', 'artifactId', and 'version'") GAV gavRequest) {
        try {
            ArtifactReport artifactReport = reportsGenerator.getReport(gavRequest);
            if (artifactReport == null)
                return Response.status(Status.NOT_FOUND)
                        .entity(new ErrorMessage("Requested GA was not found")).build();
            else
                return Response.ok().entity(toReport(artifactReport)).build();
        } catch (CommunicationException ex) {
            return Response.status(502).entity(ex).build();
        }
    }

    @POST
    @Path("/lookup/gav")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Lookup built versions for the list of provided GAVs",
            responseContainer = "List", response = LookupReport.class)
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "Lookup report was successfully generated"),
                    @ApiResponse(
                            code = 206,
                            message = "Lookup report was generated, but at least one of the requested GAs was not found."),
                    @ApiResponse(code = 502,
                            message = "Communication with remote repository failed") })
    public Response lookupGav(
            @ApiParam(
                    value = "JSON list of objects with keys 'groupId', 'artifactId', and 'version'") List<GAV> gavRequest) {

        List<LookupReport> reportsList = new ArrayList<>();
        Status responseStatus = Status.OK;
        for (GAV gav : gavRequest) {
            try {
                VersionLookupResult lookupResult = versionFinder.lookupBuiltVersions(gav);
                LookupReport lookupReport = toLookupReport(gav, lookupResult);
                reportsList.add(lookupReport);
                if (lookupResult == null) {
                    responseStatus = Status.PARTIAL_CONTENT;
                }
            } catch (CommunicationException ex) {
                log.error("Communication with remote repository failed", ex);
                return Response.status(502)
                        .entity(new ErrorMessage("Communication with remote repository failed"))
                        .build();
            }
        }

        return Response.status(responseStatus).entity(reportsList).build();
    }

    private Report toReport(ArtifactReport report) {
        return new Report(report.getGav().getGroupId(), report.getGav().getArtifactId(), report
                .getGav().getVersion(), new ArrayList<>(report.getAvailableVersions()),
                report.getBestMatchVersion(), false, new ArrayList<>(),
                // TODO change when dependencies will be implemented
                report.isBlacklisted(), report.isWhiteListed(), report.getNotBuiltDependencies());
    }

    private LookupReport toLookupReport(GAV gav, VersionLookupResult lookupResult)
            throws CommunicationException {
        if (lookupResult == null)
            return new LookupReport(gav, null, Collections.<String> emptyList(),
                    isBlacklisted(gav), isWhitelisted(gav));
        else
            return new LookupReport(gav, lookupResult.getBestMatchVersion(),
                    lookupResult.getAvailableVersions(), isBlacklisted(gav), isWhitelisted(gav));
    }

    private boolean isBlacklisted(GAV gav) {
        return blackArtifactService.isArtifactPresent(gav.getGroupId(), gav.getArtifactId(),
                gav.getVersion());
    }

    private boolean isWhitelisted(GAV gav) {
        return whiteArtifactService.isArtifactPresent(gav.getGroupId(), gav.getArtifactId(),
                gav.getVersion());
    }

}
