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

/**
 *
 * @author Jozef Mrazek <jmrazek@redhat.com>
 *
 */
@Path("/listings")
@Api(value = "/listings", description = "Listings of black and white artifacts")
public class Artifacts {

    @Inject
    private RestConvert convert;

    @Inject
    private WhiteArtifactService whiteService;

    @Inject
    private BlackArtifactService blackService;

    @POST
    @Path("/white")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value="Add White Artifact")
    public SuccessResponse addWhiteArtifact(RestArtifact artifact) {
        SuccessResponse response = new SuccessResponse();
        response.setSuccess(whiteService.addArtifact(artifact.getGroupId(),
                artifact.getArtifactId(),
                artifact.getVersion()));
        return response;
    }

    @POST
    @Path("/black")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value="Add Black Artifact")
    public SuccessResponse addBlackArtifact(RestArtifact artifact) {
        SuccessResponse response = new SuccessResponse();
        response.setSuccess(blackService.addArtifact(artifact.getGroupId(),
                artifact.getArtifactId(),
                artifact.getVersion()));
        return response;
    }

    @GET
    @Path("/white")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Test if artifact is a white artifact")
    public ContainsResponse isWhiteArtifactPresent(@QueryParam("groupid") String groupId,
                   @QueryParam("artifactid") String artifactId,
                   @QueryParam("version") String version) {
        ContainsResponse response = new ContainsResponse();
        response.setContains(whiteService.isArtifactPresent(groupId, artifactId, version));
        return response;
    }

    @GET
    @Path("/black")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Test if artifact is a black artifact")
    public ContainsResponse isBlackArtifactPresent(@QueryParam("groupid") String groupId,
                   @QueryParam("artifactid") String artifactId,
                   @QueryParam("version") String version) {
        ContainsResponse response = new ContainsResponse();
        response.setContains(blackService.isArtifactPresent(groupId, artifactId, version));
        return response;
    }

    @GET
    @Path("/whitelist")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get all the white artifacts")
    public Collection<RestArtifact> getAllWhiteArtifacts() {
        List<RestArtifact> artifacts = new ArrayList<RestArtifact>();
        artifacts.addAll(convert.toRestArtifacts(whiteService.getAll()));
        return artifacts;
    }

    @GET
    @Path("/blacklist")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get all the black artifacts")
    public Collection<RestArtifact> getAllBlackArtifacts() {
        List<RestArtifact> artifacts = new ArrayList<RestArtifact>();
        artifacts.addAll(convert.toRestArtifacts(blackService.getAll()));
        return artifacts;
    }

    @DELETE
    @Path("/white")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Remove white artifact")
    public SuccessResponse removeWhiteArtifact(RestArtifact artifact) {
        SuccessResponse response = new SuccessResponse();
        response.setSuccess(whiteService.removeArtifact(artifact.getGroupId(),
                artifact.getArtifactId(),
                artifact.getVersion()));
        return response;
    }

    @DELETE
    @Path("/black")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Remove black artifact")
    public SuccessResponse removeBlackArtifact(RestArtifact artifact) {
        SuccessResponse response = new SuccessResponse();
        response.setSuccess(blackService.removeArtifact(artifact.getGroupId(),
                artifact.getArtifactId(),
                artifact.getVersion()));
        return response;
    }
}
