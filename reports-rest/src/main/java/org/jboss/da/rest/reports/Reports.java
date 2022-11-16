package org.jboss.da.rest.reports;

import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.maven.scm.ScmException;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pom.PomAnalysisException;
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
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.util.List;

/**
 * Main end point for the reports
 *
 * @author Dustin Kut Moy Cheung
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 */
@Path("/reports")
@Tag(name = "reports")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class Reports {

    @Inject
    private Logger log;

    @Inject
    private ReportsFacade facade;

    @POST
    @Path("/scm")
    @Operation(summary = "Get dependency report for a project specified in a repository URL.")
    @ApiResponse(content = @Content(schema = @Schema(implementation = Report.class)))
    @TimedMetric
    @WithSpan()
    public Response scmGenerator(
            @SpanAttribute(value = "request") @Parameter(description = "scm information") SCMReportRequest request)
            throws ScmException, PomAnalysisException, CommunicationException, ValidationException {
        return Response.ok().entity(facade.scmReport(request)).build();
    }

    @POST
    @Path("/scm-advanced")
    @Operation(summary = "Get dependency report for a project specified in a repository URL.")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AdvancedReport.class)))
    @TimedMetric
    @WithSpan()
    public Response advancedScmGenerator(
            @SpanAttribute(value = "request") @Parameter(description = "scm information") SCMReportRequest request)
            throws ScmException, PomAnalysisException, CommunicationException, ValidationException {
        return Response.ok().entity(facade.advancedScmReport(request)).build();

    }

    @POST
    @Path("/lookup/gavs")
    @Operation(
            summary = "Lookup built versions for the list of provided GAVs.",
            deprecated = true,
            description = "DEPRECATED: use /lookup/maven endpoint instead.")
    @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = LookupReport.class))))
    @ApiResponse(responseCode = "502", description = "Communication with remote repository failed")
    @Tag(name = "deprecated")
    @TimedMetric
    @Valid
    @WithSpan()
    public Response lookupGav(
            @SpanAttribute(value = "gavRequest") @Parameter(
                    description = "JSON list of objects with keys 'groupId', 'artifactId', and 'version'") LookupGAVsRequest gavRequest)
            throws CommunicationException {
        log.info("Incoming request to /lookup/gavs. Payload: " + gavRequest.toString());
        List<LookupReport> lookupReportList = facade.gavsReport(gavRequest);
        log.info("Request to /lookup/gavs completed successfully. Payload: " + gavRequest.toString());
        return Response.status(Status.OK).entity(lookupReportList).build();
    }

    @POST
    @Path("/lookup/npm")
    @Operation(
            summary = "Lookup built versions for the list of provided NPM artifacts.",
            deprecated = true,
            description = "DEPRECATED: use /lookup/npm endpoint instead.")
    @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = NPMLookupReport.class))))
    @ApiResponse(responseCode = "502", description = "Communication with remote repository failed")
    @Tag(name = "deprecated")
    @TimedMetric
    @Valid
    @WithSpan()
    public Response lookupNPM(
            @SpanAttribute(value = "request") @Parameter(
                    description = "JSON object with list of package names") LookupNPMRequest request)
            throws CommunicationException {
        log.info("Incoming request to /lookup/npm. Payload: " + request.toString());
        List<NPMLookupReport> lookupReportList = facade.lookupReport(request);
        log.info("Request to /lookup/npm completed successfully. Payload: " + request.toString());
        return Response.status(Status.OK).entity(lookupReportList).build();
    }

    @POST
    @Path("/versions/npm")
    @Operation(summary = "Lookup and filter versions for the list of provided NPM artifacts.")
    @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = NPMVersionsReport.class))))
    @ApiResponse(responseCode = "502", description = "Communication with remote repository failed")
    @TimedMetric
    @Valid
    @WithSpan()
    public Response versionsNPM(
            @SpanAttribute(value = "request") @Parameter(
                    description = "JSON object with list of package names") VersionsNPMRequest request)
            throws CommunicationException {
        log.info("Incoming request to /versions/npm. Payload: " + request.toString());
        List<NPMVersionsReport> versionsReportList = facade.versionReport(request);
        log.info("Request to /versions/npm completed successfully. Payload: " + request.toString());
        return Response.status(Status.OK).entity(versionsReportList).build();
    }

    @POST
    @Path("/align")
    @Operation(summary = "Get alignment report for project specified in a repository URL.")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AlignReport.class)))
    @TimedMetric
    @WithSpan()
    public Response alignReport(@SpanAttribute(value = "request") AlignReportRequest request)
            throws ScmException, PomAnalysisException, CommunicationException, ValidationException {
        return Response.status(Status.OK).entity(facade.alignReport(request)).build();
    }

    @POST
    @Path("/built")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get built artifacts for project specified in a repository URL.")
    @ApiResponse(content = @Content(schema = @Schema(implementation = BuiltReport.class)))
    @TimedMetric
    @WithSpan()
    public Response builtReport(@SpanAttribute(value = "request") BuiltReportRequest request)
            throws ScmException, PomAnalysisException, CommunicationException, ValidationException {
        return Response.status(Status.OK).entity(facade.builtReport(request)).build();
    }

}
