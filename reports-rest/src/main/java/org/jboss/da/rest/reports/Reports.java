package org.jboss.da.rest.reports;

import org.apache.maven.scm.ScmException;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.aprox.FindGAVDependencyException;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.model.rest.ErrorMessage;
import org.jboss.da.reports.model.rest.AdvancedReport;
import org.jboss.da.reports.model.rest.AlignReport;
import org.jboss.da.reports.model.rest.AlignReportRequest;
import org.jboss.da.reports.model.rest.BuiltReport;
import org.jboss.da.reports.model.rest.BuiltReportRequest;
import org.jboss.da.reports.model.rest.GAVRequest;
import org.jboss.da.reports.model.rest.LookupGAVsRequest;
import org.jboss.da.reports.model.rest.LookupReport;
import org.jboss.da.reports.model.rest.Report;
import org.jboss.da.reports.model.rest.SCMReportRequest;
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

import java.util.NoSuchElementException;

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
    private ReportsFacade facade;

    @POST
    @Path("/scm")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get dependency report for a project specified in a repository URL",
            response = Report.class)
    public Response scmGenerator(@ApiParam(value = "scm information") SCMReportRequest request) {
        try {
            return Response.ok().entity(facade.scmReport(request)).build();
        } catch (NoSuchElementException e) {
            return Response
                    .status(Status.NOT_FOUND)
                    .entity(new ErrorMessage(ErrorMessage.eType.NO_RELATIONSHIP_FOUND,
                            "No relationship found")).build();
        } catch (ScmException e) {
            log.error("Exception thrown in scm endpoint", e);
            return Response
                    .status(Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorMessage(ErrorMessage.eType.SCM_ENDPOINT,
                            "Exception thrown in scm endpoint", e.getMessage())).build();
        } catch (PomAnalysisException e) {
            log.error("Exception thrown during POM analysis", e);
            return Response
                    .status(Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorMessage(ErrorMessage.eType.POM_ANALYSIS,
                            "Exception thrown during POM analysis", e.getMessage())).build();
        } catch (IllegalArgumentException e) {
            log.error("Illegal arguments exception", e);
            return Response
                    .status(Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorMessage(ErrorMessage.eType.ILLEGAL_ARGUMENTS,
                            "Illegal arguments exception", e.getMessage())).build();
        } catch (CommunicationException e) {
            log.error("Exception during communication", e);
            return Response
                    .status(Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorMessage(ErrorMessage.eType.COMMUNICATION_FAIL,
                            "Exception during communication", e.getMessage())).build();
        } catch (ValidationException e) {
            return e.getResponse();
        }
    }

    @POST
    @Path("/scm-advanced")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get dependency report for a project specified in a repository URL",
            response = AdvancedReport.class)
    public Response advancedScmGenerator(
            @ApiParam(value = "scm information") SCMReportRequest request) {
        try {
            return Response.ok().entity(facade.advancedScmReport(request)).build();
        } catch (NoSuchElementException e) {
            return Response
                    .status(Status.NOT_FOUND)
                    .entity(new ErrorMessage(ErrorMessage.eType.NO_RELATIONSHIP_FOUND,
                            "No relationship found")).build();
        } catch (ScmException e) {
            log.error("Exception thrown in scm endpoint", e);
            return Response
                    .status(Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorMessage(ErrorMessage.eType.SCM_ENDPOINT,
                            "Exception thrown in scm endpoint", e.getMessage())).build();
        } catch (PomAnalysisException e) {
            log.error("Exception thrown during POM analysis", e);
            return Response
                    .status(Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorMessage(ErrorMessage.eType.POM_ANALYSIS,
                            "Exception thrown during POM analysis", e.getMessage())).build();
        } catch (IllegalArgumentException e) {
            log.error("Illegal arguments exception", e);
            return Response
                    .status(Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorMessage(ErrorMessage.eType.ILLEGAL_ARGUMENTS,
                            "Illegal arguments exception", e.getMessage())).build();
        } catch (CommunicationException e) {
            log.error("Exception during communication", e);
            return Response
                    .status(Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorMessage(ErrorMessage.eType.COMMUNICATION_FAIL,
                            "Exception during communication", e.getMessage())).build();
        } catch (ValidationException e) {
            return e.getResponse();
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
    public Response gavGenerator(
            @ApiParam(value = "JSON Object with keys 'groupId', 'artifactId', and 'version'") GAVRequest gavRequest) {
        try {
            return Response.ok().entity(facade.gavReport(gavRequest)).build();
        } catch (CommunicationException ex) {
            log.error("Communication with remote repository failed", ex);
            return Response
                    .status(502)
                    .entity(new ErrorMessage(ErrorMessage.eType.COMMUNICATION_FAIL,
                            "Communication with remote repository failed", ex.getMessage()))
                    .build();
        } catch (FindGAVDependencyException ex) {
            log.error("Could not find gav in AProx", ex);
            return Response
                    .status(Status.NOT_FOUND)
                    .entity(new ErrorMessage(ErrorMessage.eType.GA_NOT_FOUND,
                            "Requested GA was not found", ex.getMessage())).build();
        } catch (IllegalArgumentException e) {
            return Response
                    .status(Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorMessage(ErrorMessage.eType.ILLEGAL_ARGUMENTS,
                            "Illegal arguments exception", e.getMessage())).build();
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
    public Response lookupGav(
            @ApiParam(
                    value = "JSON list of objects with keys 'groupId', 'artifactId', and 'version'") LookupGAVsRequest gavRequest) {
        try {
            return Response.status(Status.OK).entity(facade.gavsReport(gavRequest)).build();
        } catch (CommunicationException e) {
            return Response
                    .status(502)
                    .entity(new ErrorMessage(ErrorMessage.eType.COMMUNICATION_FAIL,
                            "Communication with remote repository failed")).build();
        } catch (IllegalArgumentException e) {
            return Response
                    .status(Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorMessage(ErrorMessage.eType.ILLEGAL_ARGUMENTS,
                            "Illegal arguments exception", e.getMessage())).build();
        }
    }

    @POST
    @Path("/align")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get alignment report for project specified in a repository URL.",
            response = AlignReport.class)
    public Response alignReport(AlignReportRequest request) {
        try {
            return Response.status(Status.OK).entity(facade.alignReport(request)).build();
        } catch (ScmException e) {
            log.error("Exception thrown in scm endpoint", e);
            return Response
                    .status(Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorMessage(ErrorMessage.eType.SCM_ENDPOINT,
                            "Exception thrown in scm endpoint", e.getMessage())).build();
        } catch (PomAnalysisException e) {
            log.error("Exception thrown during POM analysis", e);
            return Response
                    .status(Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorMessage(ErrorMessage.eType.POM_ANALYSIS,
                            "Exception thrown during POM analysis", e.getMessage())).build();
        } catch (ValidationException e) {
            return e.getResponse();
        }
    }

    @POST
    @Path("/built")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get builded artifacts for project specified in a repository URL.",
            response = BuiltReport.class)
    public Response builtReport(BuiltReportRequest request) {
        try {
            return Response.status(Status.OK).entity(facade.builtReport(request)).build();
        } catch (ScmException e) {
            log.error("Exception thrown in SCM analysis", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorMessage(ErrorMessage.eType.SCM_ENDPOINT, e.getMessage()))
                    .build();
        } catch (PomAnalysisException e) {
            log.error("Exception thrown in POM analysis", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorMessage(ErrorMessage.eType.POM_ANALYSIS, e.getMessage()))
                    .build();
        } catch (CommunicationException e) {
            log.error("Communication with remote repository failed", e);
            return Response
                    .status(Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorMessage(ErrorMessage.eType.COMMUNICATION_FAIL, e.getMessage()))
                    .build();
        } catch (ValidationException e) {
            return e.getResponse();
        }
    }
}
