package org.jboss.da.rest.api;

import java.util.Set;

import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
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
    @APIResponse(
            content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = MavenLookupResult.class)))
    Set<MavenLookupResult> lookupMaven(@Valid MavenLookupRequest request) throws CommunicationException;

    @POST
    @Path(value = "/maven/versions")
    @Operation(summary = "Lookup and filter available versions for the given Maven artifact coordinates (GAV).")
    @APIResponse(
            content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = MavenLookupResult.class)))
    Set<MavenVersionsResult> versionsMaven(@Valid MavenVersionsRequest request) throws CommunicationException;

    @POST
    @Path(value = "/maven/latest")
    @Operation(
            summary = "Finds latest matching versions for given Maven artifact coordinates (GAV), including bad versions.",
            description = "This endpoint is used for version increment so it will search all possible places and qualities of artifacts, including deleted and blocklisted artifacts.")
    @APIResponse(
            content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = MavenLatestResult.class)))
    Set<MavenLatestResult> lookupMaven(@Valid MavenLatestRequest request) throws CommunicationException;

    @POST
    @Path(value = "/npm")
    @Operation(summary = "Finds best matching versions for given NPM artifact coordinates (name, version).")
    @APIResponse(content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = NPMLookupResult.class)))
    Set<NPMLookupResult> lookupNPM(@Valid NPMLookupRequest request) throws CommunicationException;

    @POST
    @Path(value = "/npm/versions")
    @Operation(summary = "Lookup and filter available versions for the given NPM artifact coordinates (name, version).")
    @APIResponse(
            content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = NPMVersionsResult.class)))
    Set<NPMVersionsResult> versionsNPM(@Valid NPMVersionsRequest request) throws CommunicationException;

}
