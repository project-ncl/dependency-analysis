package org.jboss.da.rest.api;

import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.lookup.model.MavenLatestRequest;
import org.jboss.da.lookup.model.MavenLatestResult;
import org.jboss.da.lookup.model.MavenLookupRequest;
import org.jboss.da.lookup.model.MavenLookupResult;
import org.jboss.da.lookup.model.MavenVersionsRequest;
import org.jboss.da.lookup.model.MavenVersionsResult;
import org.jboss.da.lookup.model.NPMLookupRequest;
import org.jboss.da.lookup.model.NPMLookupResult;
import org.jboss.da.lookup.model.NPMVersionsRequest;
import org.jboss.da.lookup.model.NPMVersionsResult;
import org.jboss.da.reports.model.response.NPMVersionsReport;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Path("/lookup")
@Tag(name = "lookup")
@Consumes(value = MediaType.APPLICATION_JSON)
@Produces(value = MediaType.APPLICATION_JSON)
public interface Lookup {

    @POST
    @Path(value = "/maven")
    @Operation(summary = "Finds best matching versions for given Maven artifact coordinates (GAV).")
    @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = MavenLookupResult.class))))
    @WithSpan()
    Set<MavenLookupResult> lookupMaven(@SpanAttribute(value = "request") MavenLookupRequest request)
            throws CommunicationException;

    @POST
    @Path(value = "/maven/versions")
    @Operation(summary = "Lookup and filter available versions for the given Maven artifact coordinates (GAV).")
    @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = MavenLookupResult.class))))
    @WithSpan()
    Set<MavenVersionsResult> versionsMaven(@SpanAttribute(value = "request") MavenVersionsRequest request)
            throws CommunicationException;

    @POST
    @Path(value = "/maven/latest")
    @Operation(
            summary = "Finds latest matching versions for given Maven artifact coordinates (GAV), including bad versions.",
            description = "This endpoint is used for version increment so it will search all possible places and qualities of artifacts, including deleted and blocklisted artifacts.")
    @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = MavenLatestResult.class))))
    @WithSpan()
    Set<MavenLatestResult> lookupMaven(@SpanAttribute(value = "request") MavenLatestRequest request)
            throws CommunicationException;

    @POST
    @Path(value = "/npm")
    @Operation(summary = "Finds best matching versions for given NPM artifact coordinates (name, version).")
    @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = NPMLookupResult.class))))
    @WithSpan()
    Set<NPMLookupResult> lookupNPM(@SpanAttribute(value = "request") NPMLookupRequest request)
            throws CommunicationException;

    @POST
    @Path(value = "/npm/versions")
    @Operation(summary = "Lookup and filter available versions for the given NPM artifact coordinates (name, version).")
    @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = NPMVersionsResult.class))))
    @WithSpan()
    Set<NPMVersionsResult> versionsNPM(@SpanAttribute(value = "request") NPMVersionsRequest request)
            throws CommunicationException;

}
