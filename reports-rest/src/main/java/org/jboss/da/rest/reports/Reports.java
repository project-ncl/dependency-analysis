package org.jboss.da.rest.reports;

import org.apache.maven.scm.ScmException;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.aprox.FindGAVDependencyException;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.communication.repository.api.RepositoryException;
import org.jboss.da.model.rest.ErrorMessage;
import org.jboss.da.model.rest.ErrorMessage.ErrorType;
import org.jboss.da.reports.model.response.AdvancedReport;
import org.jboss.da.reports.model.response.AlignReport;
import org.jboss.da.reports.model.request.AlignReportRequest;
import org.jboss.da.reports.model.response.BuiltReport;
import org.jboss.da.reports.model.request.BuiltReportRequest;
import org.jboss.da.reports.model.request.GAVRequest;
import org.jboss.da.reports.model.request.LookupGAVsRequest;
import org.jboss.da.reports.model.response.LookupReport;
import org.jboss.da.reports.model.response.Report;
import org.jboss.da.reports.model.request.SCMReportRequest;
import org.jboss.da.rest.facade.ReportsFacade;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.jboss.da.validation.ValidationException;
import org.jboss.pnc.pncmetrics.rest.TimedMetric;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Main end point for the reports
 *
 * @author Dustin Kut Moy Cheung
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
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
    @ApiOperation(value = "Get dependency report for a project specified in a repository URL",
            response = Report.class)
    @TimedMetric
    public Response scmGenerator(@ApiParam(value = "scm information") SCMReportRequest request) {
        try {
            return Response.ok().entity(facade.scmReport(request)).build();
        } catch (NoSuchElementException e) {
            return handleException("No relationship found", ErrorType.NO_RELATIONSHIP_FOUND,
                    Status.NOT_FOUND, e);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @POST
    @Path("/scm-advanced")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get dependency report for a project specified in a repository URL",
            response = AdvancedReport.class)
    @TimedMetric
    public Response advancedScmGenerator(
            @ApiParam(value = "scm information") SCMReportRequest request) {
        try {
            return Response.ok().entity(facade.advancedScmReport(request)).build();
        } catch (NoSuchElementException e) {
            return handleException("No relationship found", ErrorType.NO_RELATIONSHIP_FOUND,
                    Status.NOT_FOUND, e);
        } catch (Exception e) {
            return handleException(e);
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
    @TimedMetric
    public Response gavGenerator(
            @ApiParam(value = "JSON Object with keys 'groupId', 'artifactId', and 'version'") GAVRequest gavRequest) {
        try {
            return Response.ok().entity(facade.gavReport(gavRequest)).build();
        } catch (RepositoryException e) {
            return handleException("Communication with remote repository failed",
                    ErrorType.COMMUNICATION_FAIL, Status.BAD_GATEWAY, e);
        } catch (FindGAVDependencyException e) {
            return handleException("Requested GA was not found in artifact repository",
                    ErrorType.GA_NOT_FOUND, Status.NOT_FOUND, e);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @POST
    @Path("/lookup/gavs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Lookup built versions for the list of provided GAVs",
            responseContainer = "List", response = LookupReport.class)
    @ApiResponses(value = { @ApiResponse(code = 502,
            message = "Communication with remote repository failed") })
    @TimedMetric
    public Response lookupGav(
            @ApiParam(
                    value = "JSON list of objects with keys 'groupId', 'artifactId', and 'version'") LookupGAVsRequest gavRequest) {
        try {
            log.info("Incoming request to /lookup/gavs. Payload: " + gavRequest.toString());
            List<LookupReport> lookupReportList = facade.gavsReport(gavRequest);
            log.info("Request to /lookup/gavs completed successfully. Payload: "
                    + gavRequest.toString());
            return Response.status(Status.OK).entity(lookupReportList).build();
        } catch (RepositoryException e) {
            return handleException("Communication with remote repository failed",
                    ErrorType.COMMUNICATION_FAIL, Status.BAD_GATEWAY, e);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @POST
    @Path("/align")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get alignment report for project specified in a repository URL.",
            response = AlignReport.class)
    @TimedMetric
    public Response alignReport(AlignReportRequest request) {
        try {
            return Response.status(Status.OK).entity(facade.alignReport(request)).build();
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @POST
    @Path("/built")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get builded artifacts for project specified in a repository URL.",
            response = BuiltReport.class)
    @TimedMetric
    public Response builtReport(BuiltReportRequest request) {
        try {
            return Response.status(Status.OK).entity(facade.builtReport(request)).build();
        } catch (Exception e) {
            return handleException(e);
        }
    }

    private Response handleException(Exception e) {
        if (e instanceof ValidationException) { // order of tests is important
            return ((ValidationException) e).getResponse();
        } else if (e instanceof RepositoryException) {
            return handleException("Communication with remote repository failed",
                    ErrorType.COMMUNICATION_FAIL, e);
        } else if (e instanceof CommunicationException) {
            return handleException("Exception thrown when communicating with external service",
                    ErrorType.COMMUNICATION_FAIL, e);
        } else if (e instanceof PomAnalysisException) {
            return handleException("Exception thrown in POM analysis", ErrorType.POM_ANALYSIS, e);
        } else if (e instanceof ScmException) {
            return handleException("Exception thrown in SCM analysis", ErrorType.SCM_ENDPOINT, e);
        } else if (e instanceof IllegalArgumentException) {
            return handleException("Illegal arguments exception", ErrorType.ILLEGAL_ARGUMENTS, e);
        } else {
            return handleException("Unknown error while handling request",
                    ErrorType.UNEXPECTED_SERVER_ERR, e);
        }
    }

    private Response handleException(String message, ErrorType errorType, Exception e) {
        return handleException(message, errorType, Status.INTERNAL_SERVER_ERROR, e);
    }

    private Response handleException(String message, ErrorType errorType, Status status, Exception e) {
        log.error(message, e);
        return Response.status(status).entity(new ErrorMessage(errorType, message, e.getMessage()))
                .build();
    }
}
