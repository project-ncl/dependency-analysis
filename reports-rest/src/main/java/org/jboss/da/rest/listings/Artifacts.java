package org.jboss.da.rest.listings;

import org.jboss.da.listings.api.service.BlackArtifactService;
import org.jboss.da.listings.api.service.WhiteArtifactService;
import org.jboss.da.listings.api.service.ArtifactService.STATUS;
import org.jboss.da.rest.listings.model.ContainsResponse;
import org.jboss.da.rest.listings.model.RestArtifact;
import org.jboss.da.rest.listings.model.SuccessResponse;
import org.jboss.da.rest.model.ErrorMessage;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import java.util.Collections;
import java.util.Optional;
import org.jboss.da.listings.api.model.BlackArtifact;
import org.jboss.da.listings.api.model.WhiteArtifact;

/**
 *
 * @author Jozef Mrazek <jmrazek@redhat.com>
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
@Path("/listings")
@Api(value = "/listings", description = "Listings of black/white listed artifacts")
public class Artifacts {

    @Inject
    private RestConvert convert;

    @Inject
    private WhiteArtifactService whiteService;

    @Inject
    private BlackArtifactService blackService;

    // //////////////////////////////////
    // Whitelist endpoints

    @GET
    @Path("/whitelist")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get all artifacts in the whitelist", responseContainer = "List",
            response = RestArtifact.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response successfully generated") })
    public Collection<RestArtifact> getAllWhiteArtifacts() {
        List<RestArtifact> artifacts = new ArrayList<>();
        artifacts.addAll(convert.toRestArtifacts(whiteService.getAll()));
        return artifacts;
    }

    @GET
    @Path("/whitelist/gav")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Check if an artifact is in the whitelist",
            response = ContainsResponse.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Artifact is in the whitelist"),
            @ApiResponse(code = 404, message = "Artifact is not in the whitelist") })
    public Response isWhiteArtifactPresent(@QueryParam("groupid") String groupId,
            @QueryParam("artifactid") String artifactId, @QueryParam("version") String version) {
        ContainsResponse response = new ContainsResponse();

        List<WhiteArtifact> artifacts = whiteService.getArtifacts(groupId, artifactId, version);
        response.setFound(convert.toRestArtifacts(artifacts));
        response.setContains(!artifacts.isEmpty());

        if (artifacts.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).entity(response).build();
        } else {
            return Response.ok(response).build();
        }
    }

    @POST
    @Path("/whitelist/gav")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Add an artifact to the whitelist", response = SuccessResponse.class)
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "Response successfully generated"),
                    @ApiResponse(
                            code = 400,
                            message = "Can't add artifact to whitelist, artifact is not in redhat version"),
                    @ApiResponse(code = 409,
                            message = "Can't add artifact to whitelist, artifact is blacklisted") })
    public Response addWhiteArtifact(
            @ApiParam(value = "JSON object with keys 'groupId', 'artifactId', and 'version'") RestArtifact artifact) {
        SuccessResponse response = new SuccessResponse();
        try {
            STATUS result = whiteService.addArtifact(artifact.getGroupId(),
                    artifact.getArtifactId(), artifact.getVersion());
            switch (result) {
                case ADDED:
                    response.setSuccess(true);
                    return Response.ok(response).build();
                case IS_BLACKLISTED:
                    response.setSuccess(false);
                    return Response
                            .status(Response.Status.CONFLICT)
                            .entity(new ErrorMessage(
                                    "Can't add artifact to whitelist, artifact is blacklisted"))
                            .build();
                case NOT_MODIFIED:
                    response.setSuccess(false);
                    return Response.ok(response).build();
                default:
                    response.setSuccess(false);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(new ErrorMessage("Unexpected server error occurred.")).build();
            }
        } catch (IllegalArgumentException ex) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage(
                            "Can't add artifact to whitelist, artifact is not in redhat version."))
                    .build();
        }
    }

    @DELETE
    @Path("/whitelist/gav")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Remove an artifact from the whitelist", response = SuccessResponse.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response successfully generated") })
    public SuccessResponse removeWhiteArtifact(
            @ApiParam(value = "JSON object with keys 'groupId', 'artifactId', and 'version'") RestArtifact artifact) {
        SuccessResponse response = new SuccessResponse();
        response.setSuccess(whiteService.removeArtifact(artifact.getGroupId(),
                artifact.getArtifactId(), artifact.getVersion()));
        return response;
    }

    // //////////////////////////////////
    // Blacklist endpoints

    @GET
    @Path("/blacklist")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get all artifacts in the blacklist", responseContainer = "List",
            response = RestArtifact.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response successfully generated") })
    public Collection<RestArtifact> getAllBlackArtifacts() {
        List<RestArtifact> artifacts = new ArrayList<>();
        artifacts.addAll(convert.toRestArtifacts(blackService.getAll()));
        return artifacts;
    }

    @GET
    @Path("/blacklist/gav")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Check if an artifact is in the blacklist",
            response = ContainsResponse.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Artifact is in the blacklist"),
            @ApiResponse(code = 404, message = "Artifact is not in the blacklist") })
    public Response isBlackArtifactPresent(@QueryParam("groupid") String groupId,
            @QueryParam("artifactid") String artifactId, @QueryParam("version") String version) {
        ContainsResponse response = new ContainsResponse();

        Optional<BlackArtifact> artifact = blackService.getArtifact(groupId, artifactId, version);
        List<BlackArtifact> artifacts = artifact
                .map(Collections::singletonList)
                .orElse(Collections.emptyList());

        response.setContains(artifact.isPresent());
        response.setFound(convert.toRestArtifacts(artifacts));

        if (artifact.isPresent()) {
            return Response.ok(response).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(response).build();
        }
    }

    @POST
    @Path("/blacklist/gav")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Add an artifact to the blacklist", response = SuccessResponse.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response successfully generated") })
    public Response addBlackArtifact(
            @ApiParam(value = "JSON object with keys 'groupId', 'artifactId', and 'version'") RestArtifact artifact) {
        SuccessResponse response = new SuccessResponse();
        STATUS result = blackService.addArtifact(artifact.getGroupId(), artifact.getArtifactId(),
                artifact.getVersion());
        switch (result) {
            case ADDED:
                response.setSuccess(true);
                return Response.ok(response).build();
            case WAS_WHITELISTED:
                response.setSuccess(true);
                response.setMessage("Artifact was moved from whitelist into blacklist");
                return Response.ok(response).build();
            case NOT_MODIFIED:
                response.setSuccess(false);
                return Response.ok(response).build();
            default:
                response.setSuccess(false);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(new ErrorMessage("Unexpected server error occurred.")).build();
        }
    }

    @DELETE
    @Path("/blacklist/gav")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Remove an artifact from the blacklist", response = SuccessResponse.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response successfully generated") })
    public SuccessResponse removeBlackArtifact(
            @ApiParam(value = "JSON object with keys 'groupId', 'artifactId', and 'version'") RestArtifact artifact) {
        SuccessResponse response = new SuccessResponse();
        response.setSuccess(blackService.removeArtifact(artifact.getGroupId(),
                artifact.getArtifactId(), artifact.getVersion()));
        return response;
    }
}
