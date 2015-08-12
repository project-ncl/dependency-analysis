package org.jboss.da.rest.listings;

import org.jboss.da.listings.api.service.BlackArtifactService;
import org.jboss.da.listings.api.service.WhiteArtifactService;
import org.jboss.da.rest.listings.model.ContainsResponse;
import org.jboss.da.rest.listings.model.RestArtifact;
import org.jboss.da.rest.listings.model.SuccessResponse;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

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
        List<RestArtifact> artifacts = new ArrayList<RestArtifact>();
        artifacts.addAll(convert.toRestArtifacts(whiteService.getAll()));
        return artifacts;
    }

    @GET
    @Path("/whitelist/gav")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Check if an artifact is in the whitelist",
            response = ContainsResponse.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response successfully generated") })
    public ContainsResponse isWhiteArtifactPresent(@QueryParam("groupid") String groupId,
            @QueryParam("artifactid") String artifactId, @QueryParam("version") String version) {
        ContainsResponse response = new ContainsResponse();
        response.setContains(whiteService.isArtifactPresent(groupId, artifactId, version));
        return response;
    }

    @POST
    @Path("/whitelist/gav")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Add an artifact to the whitelist", response = SuccessResponse.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response successfully generated") })
    public SuccessResponse addWhiteArtifact(
            @ApiParam(value = "JSON object with keys 'groupId', 'artifactId', and 'version'") RestArtifact artifact) {
        SuccessResponse response = new SuccessResponse();
        response.setSuccess(whiteService.addArtifact(artifact.getGroupId(),
                artifact.getArtifactId(), artifact.getVersion()));
        return response;
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
        List<RestArtifact> artifacts = new ArrayList<RestArtifact>();
        artifacts.addAll(convert.toRestArtifacts(blackService.getAll()));
        return artifacts;
    }

    @GET
    @Path("/blacklist/gav")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Check if an artifact is in the blacklist",
            response = ContainsResponse.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response successfully generated") })
    public ContainsResponse isBlackArtifactPresent(@QueryParam("groupid") String groupId,
            @QueryParam("artifactid") String artifactId, @QueryParam("version") String version) {
        ContainsResponse response = new ContainsResponse();
        response.setContains(blackService.isArtifactPresent(groupId, artifactId, version));
        return response;
    }

    @POST
    @Path("/blacklist/gav")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Add an artifact to the blacklist", response = SuccessResponse.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response successfully generated") })
    public SuccessResponse addBlackArtifact(
            @ApiParam(value = "JSON object with keys 'groupId', 'artifactId', and 'version'") RestArtifact artifact) {
        SuccessResponse response = new SuccessResponse();
        response.setSuccess(blackService.addArtifact(artifact.getGroupId(),
                artifact.getArtifactId(), artifact.getVersion()));
        return response;
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
