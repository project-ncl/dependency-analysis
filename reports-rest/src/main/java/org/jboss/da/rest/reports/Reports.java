package org.jboss.da.rest.reports;

import org.apache.maven.scm.ScmException;
import org.jboss.da.communication.aprox.FindGAVDependencyException;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.model.GAV;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.listings.api.model.ProductVersion;
import org.jboss.da.listings.api.service.BlackArtifactService;
import org.jboss.da.listings.api.service.WhiteArtifactService;
import org.jboss.da.reports.api.AdvancedArtifactReport;
import org.jboss.da.reports.api.ArtifactReport;
import org.jboss.da.reports.api.ReportsGenerator;
import org.jboss.da.reports.api.SCMLocator;
import org.jboss.da.reports.api.VersionLookupResult;
import org.jboss.da.reports.backend.api.VersionFinder;
import org.jboss.da.rest.listings.model.RestProductInput;
import org.jboss.da.rest.model.ErrorMessage;
import org.jboss.da.rest.reports.model.AdvancedReport;
import org.jboss.da.rest.reports.model.LookupReport;
import org.jboss.da.rest.reports.model.Report;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Main end point for the reports
 * 
 * @author Dustin Kut Moy Cheung
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
@Path("/reports")
@Api(value = "reports")
public class Reports {

    @Inject
    private Logger log;

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
            response = Report.class)
    public Response scmGenerator(@ApiParam(value = "scm information") SCMLocator scm) {

        try {

            Optional<ArtifactReport> artifactReport = reportsGenerator.getReportFromSCM(scm);

            return artifactReport
                    .map(x -> Response.ok().entity(toReport(x)).build())
                    .orElseGet(() -> Response.status(Status.NOT_FOUND)
                            .entity(new ErrorMessage("No relationship found")).build());

        } catch (ScmException|PomAnalysisException|IllegalArgumentException|CommunicationException e) {
            log.error("Exception thrown in scm endpoint", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e).build();
        }
    }

    @POST
    @Path("/scm-advanced")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get dependency report for a project specified in a repository URL",
            response = AdvancedReport.class)
    public Response advancedScmGenerator(@ApiParam(value = "scm information") SCMLocator scm) {

        try {

            Optional<AdvancedArtifactReport> advancedArtifactReport = reportsGenerator.getAdvancedReportFromSCM(scm);

            return advancedArtifactReport
                    .map(x -> Response.ok().entity(toAdvancedReport(x)).build())
                    .orElseGet(() -> Response.status(Status.NOT_FOUND)
                            .entity(new ErrorMessage("No relationship found")).build());

        } catch (ScmException|PomAnalysisException|IllegalArgumentException|CommunicationException e) {
            log.error("Exception thrown in scm endpoint", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e).build();
        }
    }

    @POST
    @Path("/gav")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get dependency report for a GAV ", response = Report.class)
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Requested GAV was not found in repository",
                    response = ErrorMessage.class),
            @ApiResponse(code = 502, message = "Communication with remote repository failed") })
    public Response gavGenerator(@ApiParam(
            value = "JSON Object with keys 'groupId', 'artifactId', and 'version'") GAV gavRequest) {
        try {
            ArtifactReport artifactReport = reportsGenerator.getReport(gavRequest);
            return Response.ok().entity(toReport(artifactReport)).build();
        } catch (CommunicationException ex) {
            log.error("Communication with remote repository failed", ex);
            return Response.status(502).entity(ex).build();
        } catch (FindGAVDependencyException ex) {
            log.error("Could not find gav in AProx", ex);
            return Response.status(Status.NOT_FOUND)
                    .entity(new ErrorMessage("Requested GA was not found")).build();
        }
    }

    @POST
    @Path("/lookup/gavs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Lookup built versions for the list of provided GAVs",
            responseContainer = "List", response = LookupReport.class)
    @ApiResponses(value = {
            @ApiResponse(code = 502, message = "Communication with remote repository failed") })
    public Response lookupGav(
            @ApiParam(
                    value = "JSON list of objects with keys 'groupId', 'artifactId', and 'version'") List<GAV> gavRequest) {
        List<LookupReport> reportsList = new ArrayList<>();

        boolean communicationSucceded = gavRequest.parallelStream().map((gav) -> {
            try {
                VersionLookupResult lookupResult = versionFinder.lookupBuiltVersions(gav);
                LookupReport lookupReport = toLookupReport(gav, lookupResult);
                reportsList.add(lookupReport);
                return true;
            } catch (CommunicationException ex) {
                log.error("Communication with remote repository failed", ex);
                return false;
            }
        }).allMatch(x -> {
            return x;
        });

        if (!communicationSucceded)
            return Response.status(502)
                    .entity(new ErrorMessage("Communication with remote repository failed"))
                    .build();
        else
            return Response.status(Status.OK).entity(reportsList).build();
    }

    private static Report toReport(ArtifactReport report) {
        List<Report> dependencies = report.getDependencies()
                .stream()
                .map(Reports::toReport)
                .collect(Collectors.toList());

        return new Report(report.getGav(), new ArrayList<>(report.getAvailableVersions()),
                report.getBestMatchVersion().orElse(null), report.isDependencyVersionSatisfied(),
                dependencies,
                report.isBlacklisted(), toWhitelisted(report.getWhitelisted()), report.getNotBuiltDependencies());
    }

    private static AdvancedReport toAdvancedReport(AdvancedArtifactReport advancedArtifactReport) {

        Report report = toReport(advancedArtifactReport.getArtifactReport());
        return new AdvancedReport(report, advancedArtifactReport.getBlacklistedArtifacts(),
                advancedArtifactReport.getWhitelistedArtifacts(),
                advancedArtifactReport.getCommunityGavsWithBestMatchVersions(),
                advancedArtifactReport.getCommunityGavsWithBuiltVersions(),
                advancedArtifactReport.getCommunityGavs());
    }

    private LookupReport toLookupReport(GAV gav, VersionLookupResult lookupResult) {
        return new LookupReport(gav, lookupResult.getBestMatchVersion().orElse(null),
                lookupResult.getAvailableVersions(), isBlacklisted(gav),
                toWhitelisted(getWhitelisted(gav)));
    }

    private static List<RestProductInput> toWhitelisted(List<ProductVersion> whitelisted) {
        throw new UnsupportedOperationException("Not supported yet."); // TODO
    }

    private List<ProductVersion> getWhitelisted(GAV gav) {
        throw new UnsupportedOperationException("Not supported yet.");// TODO
    }

    private boolean isBlacklisted(GAV gav) {
        return blackArtifactService.isArtifactPresent(gav.getGroupId(), gav.getArtifactId(),
                gav.getVersion());
    }

}
