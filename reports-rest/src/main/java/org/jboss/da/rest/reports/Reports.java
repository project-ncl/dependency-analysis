package org.jboss.da.rest.reports;

import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.apache.maven.scm.ScmException;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
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
import org.slf4j.Logger;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

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
    @APIResponse(content = @Content(schema = @Schema(implementation = Report.class)))
    @WithSpan()
    public Response scmGenerator(
            @SpanAttribute(value = "request") @Parameter(description = "scm information") SCMReportRequest request)
            throws ScmException, PomAnalysisException, CommunicationException, ValidationException {
        return Response.ok().entity(facade.scmReport(request)).build();
    }

    @POST
    @Path("/scm-advanced")
    @Operation(summary = "Get dependency report for a project specified in a repository URL.")
    @APIResponse(content = @Content(schema = @Schema(implementation = AdvancedReport.class)))
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
    @APIResponse(content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = LookupReport.class)))
    @APIResponse(responseCode = "502", description = "Communication with remote repository failed")
    @Tag(name = "deprecated")
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
    @APIResponse(content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = NPMLookupReport.class)))
    @APIResponse(responseCode = "502", description = "Communication with remote repository failed")
    @Tag(name = "deprecated")
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
    @APIResponse(
            content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = NPMVersionsReport.class)))
    @APIResponse(responseCode = "502", description = "Communication with remote repository failed")
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
    @APIResponse(content = @Content(schema = @Schema(implementation = AlignReport.class)))
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
    @APIResponse(content = @Content(schema = @Schema(implementation = BuiltReport.class)))
    @WithSpan()
    public Response builtReport(@SpanAttribute(value = "request") BuiltReportRequest request)
            throws ScmException, PomAnalysisException, CommunicationException, ValidationException {
        return Response.status(Status.OK).entity(facade.builtReport(request)).build();
    }

}
