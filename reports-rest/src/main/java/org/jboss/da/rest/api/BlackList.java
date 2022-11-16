package org.jboss.da.rest.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jboss.da.listings.model.rest.ContainsResponse;
import org.jboss.da.listings.model.rest.RestArtifact;
import org.jboss.da.listings.model.rest.SuccessResponse;
import org.jboss.da.model.rest.ErrorMessage;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.Collection;

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
    @ApiResponse(content = @Content(schema = @Schema(implementation = SuccessResponse.class)))
    Response addBlackArtifact(@Parameter(description = GAV_JSON) RestArtifact artifact);

    @GET
    @Produces(value = MediaType.APPLICATION_JSON)
    @Operation(summary = "Get all artifacts in the blocklist")
    @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = RestArtifact.class))))
    Collection<RestArtifact> getAllBlackArtifacts();

    @GET
    @Path(value = "/ga")
    @Produces(value = MediaType.APPLICATION_JSON)
    @Operation(summary = "Get artifacts in the blocklist with given groupid and artifactid")
    @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = RestArtifact.class))))
    Collection<RestArtifact> getBlackArtifacts(
            @QueryParam(value = "groupid") String groupId,
            @QueryParam(value = "artifactid") String artifactId);

    @GET
    @Path(value = "/gav")
    @Produces(value = MediaType.APPLICATION_JSON)
    @Operation(summary = "Check if an artifact is in the blocklist")
    @ApiResponse(content = @Content(schema = @Schema(implementation = ContainsResponse.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Artifact is not in the blocklist",
            content = @Content(schema = @Schema(implementation = ContainsResponse.class)))
    @ApiResponse(
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
    @ApiResponse(content = @Content(schema = @Schema(implementation = SuccessResponse.class)))
    SuccessResponse removeBlackArtifact(@Parameter(description = GAV_JSON) RestArtifact artifact);

}
