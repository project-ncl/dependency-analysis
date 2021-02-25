package org.jboss.da.rest.reports;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.apache.maven.scm.ScmException;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.communication.repository.api.RepositoryException;
import org.jboss.da.model.rest.ErrorMessage;
import org.jboss.da.model.rest.ErrorMessage.ErrorType;
import org.jboss.da.reports.model.request.AlignReportRequest;
import org.jboss.da.reports.model.request.BuiltReportRequest;
import org.jboss.da.reports.model.request.LookupGAVsRequest;
import org.jboss.da.reports.model.request.LookupNPMRequest;
import org.jboss.da.reports.model.request.SCMReportRequest;
import org.jboss.da.reports.model.request.VersionsNPMRequest;
import org.jboss.da.reports.model.response.AdvancedReport;
import org.jboss.da.reports.model.response.AlignReport;
import org.jboss.da.reports.model.response.BuiltReport;
import org.jboss.da.reports.model.response.LookupReport;
import org.jboss.da.reports.model.response.NPMLookupReport;
import org.jboss.da.reports.model.response.NPMVersionsReport;
import org.jboss.da.reports.model.response.Report;
import org.jboss.da.rest.facade.ReportsFacade;
import org.jboss.da.validation.ValidationException;
import org.jboss.pnc.pncmetrics.rest.TimedMetric;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Main end point for the reports
 *
 * @author Dustin Kut Moy Cheung
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 */
@Path("/reports")
@Api(value = "reports")
public class Reports {

    @Inject
    private Logger log;

    @Inject
    private ReportsFacade facade;

    @POST
    @Path("/scm")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get dependency report for a project specified in a repository URL", response = Report.class)
    @TimedMetric
    public Response scmGenerator(@ApiParam(value = "scm information") SCMReportRequest request)
            throws ScmException, PomAnalysisException, CommunicationException, ValidationException {
        return Response.ok().entity(facade.scmReport(request)).build();
    }

    @POST
    @Path("/scm-advanced")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Get dependency report for a project specified in a repository URL",
            response = AdvancedReport.class)
    @TimedMetric
    public Response advancedScmGenerator(@ApiParam(value = "scm information") SCMReportRequest request)
            throws ScmException, PomAnalysisException, CommunicationException, ValidationException {
        return Response.ok().entity(facade.advancedScmReport(request)).build();

    }

    @POST
    @Path("/lookup/gavs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Lookup built versions for the list of provided GAVs",
            responseContainer = "List",
            response = LookupReport.class)
    @ApiResponses(value = { @ApiResponse(code = 502, message = "Communication with remote repository failed") })
    @TimedMetric
    public Response lookupGav(
            @ApiParam(
                    value = "JSON list of objects with keys 'groupId', 'artifactId', and 'version'") LookupGAVsRequest gavRequest)
            throws CommunicationException {
        log.info("Incoming request to /lookup/gavs. Payload: " + gavRequest.toString());
        List<LookupReport> lookupReportList = facade.gavsReport(gavRequest);
        log.info("Request to /lookup/gavs completed successfully. Payload: " + gavRequest.toString());
        return Response.status(Status.OK).entity(lookupReportList).build();
    }

    @POST
    @Path("/lookup/npm")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Lookup built versions for the list of provided NPM artifacts",
            responseContainer = "List",
            response = NPMLookupReport.class)
    @ApiResponses(value = { @ApiResponse(code = 502, message = "Communication with remote repository failed") })
    @TimedMetric
    public Response lookupNPM(@ApiParam(value = "JSON object with list of package names") LookupNPMRequest request)
            throws CommunicationException {
        log.info("Incoming request to /lookup/npm. Payload: " + request.toString());
        List<NPMLookupReport> lookupReportList = facade.lookupReport(request);
        log.info("Request to /lookup/npm completed successfully. Payload: " + request.toString());
        return Response.status(Status.OK).entity(lookupReportList).build();
    }

    @POST
    @Path("/versions/npm")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Lookup and filter versions for the list of provided NPM artifacts",
            responseContainer = "List",
            response = NPMVersionsReport.class)
    @ApiResponses(value = { @ApiResponse(code = 502, message = "Communication with remote repository failed") })
    @TimedMetric
    public Response versionsNPM(@ApiParam(value = "JSON object with list of package names") VersionsNPMRequest request)
            throws CommunicationException {
        log.info("Incoming request to /versions/npm. Payload: " + request.toString());
        List<NPMVersionsReport> versionsReportList = facade.versionReport(request);
        log.info("Request to /versions/npm completed successfully. Payload: " + request.toString());
        return Response.status(Status.OK).entity(versionsReportList).build();
    }

    @POST
    @Path("/align")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Get alignment report for project specified in a repository URL.",
            response = AlignReport.class)
    @TimedMetric
    public Response alignReport(AlignReportRequest request)
            throws ScmException, PomAnalysisException, CommunicationException, ValidationException {
        return Response.status(Status.OK).entity(facade.alignReport(request)).build();
    }

    @POST
    @Path("/built")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Get builded artifacts for project specified in a repository URL.",
            response = BuiltReport.class)
    @TimedMetric
    public Response builtReport(BuiltReportRequest request)
            throws ScmException, PomAnalysisException, CommunicationException, ValidationException {
        return Response.status(Status.OK).entity(facade.builtReport(request)).build();
    }

}
