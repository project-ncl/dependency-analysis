package org.jboss.da.rest.api;

import java.util.Collection;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.da.listings.model.rest.ContainsResponse;
import org.jboss.da.listings.model.rest.RestArtifact;
import org.jboss.da.listings.model.rest.SuccessResponse;
import org.jboss.da.model.rest.ErrorMessage;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Path("/listings/blacklist")
@Tag(name = "blocklist")
public interface BlackList {

    String GAV_JSON = "JSON object with keys 'groupId', 'artifactId', and 'version'";

    @POST
    @Path(value = "/gav")
    @Consumes(value = MediaType.APPLICATION_JSON)
    @Produces(value = MediaType.APPLICATION_JSON)
    @Operation(summary = "Add an artifact to the blocklist")
    @APIResponse(content = @Content(schema = @Schema(implementation = SuccessResponse.class)))
    Response addBlackArtifact(@Parameter(description = GAV_JSON) RestArtifact artifact);

    @GET
    @Produces(value = MediaType.APPLICATION_JSON)
    @Operation(summary = "Get all artifacts in the blocklist")
    @APIResponse(content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = RestArtifact.class)))
    Collection<RestArtifact> getAllBlackArtifacts();

    @GET
    @Path(value = "/ga")
    @Produces(value = MediaType.APPLICATION_JSON)
    @Operation(summary = "Get artifacts in the blocklist with given groupid and artifactid")
    @APIResponse(content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = RestArtifact.class)))
    Collection<RestArtifact> getBlackArtifacts(
            @QueryParam(value = "groupid") String groupId,
            @QueryParam(value = "artifactid") String artifactId);

    @GET
    @Path(value = "/gav")
    @Produces(value = MediaType.APPLICATION_JSON)
    @Operation(summary = "Check if an artifact is in the blocklist")
    @APIResponse(content = @Content(schema = @Schema(implementation = ContainsResponse.class)))
    @APIResponse(
            responseCode = "404",
            description = "Artifact is not in the blocklist",
            content = @Content(schema = @Schema(implementation = ContainsResponse.class)))
    @APIResponse(
            responseCode = "400",
            description = "All parameters are required",
            content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
    Response isBlackArtifactPresent(
            @QueryParam(value = "groupid") String groupId,
            @QueryParam(value = "artifactid") String artifactId,
            @QueryParam(value = "version") String version);

    @DELETE
    @Path(value = "/gav")
    @Consumes(value = MediaType.APPLICATION_JSON)
    @Produces(value = MediaType.APPLICATION_JSON)
    @Operation(summary = "Remove an artifact from the blocklist")
    @APIResponse(content = @Content(schema = @Schema(implementation = SuccessResponse.class)))
    SuccessResponse removeBlackArtifact(@Parameter(description = GAV_JSON) RestArtifact artifact);

}
