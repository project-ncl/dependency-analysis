package org.jboss.da.rest.spi;

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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Path("/listings/blacklist")
@Api(value = "blacklist")
public interface BlackList {

    String GAV_JSON = "JSON object with keys 'groupId', 'artifactId', and 'version'";

    @POST
    @Path(value = "/gav")
    @Consumes(value = MediaType.APPLICATION_JSON)
    @Produces(value = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Add an artifact to the blacklist", response = SuccessResponse.class)
    Response addBlackArtifact(@ApiParam(value = GAV_JSON) RestArtifact artifact);

    @GET
    @Produces(value = MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Get all artifacts in the blacklist",
            responseContainer = "List",
            response = RestArtifact.class)
    Collection<RestArtifact> getAllBlackArtifacts();

    @GET
    @Path(value = "/ga")
    @Produces(value = MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Get artifacts in the blacklist with given groupid and artifactid",
            responseContainer = "List",
            response = RestArtifact.class)
    Collection<RestArtifact> getBlackArtifacts(
            @QueryParam(value = "groupid") String groupId,
            @QueryParam(value = "artifactid") String artifactId);

    @GET
    @Path(value = "/gav")
    @Produces(value = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Check if an artifact is in the blacklist", response = ContainsResponse.class)
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 404,
                            message = "Artifact is not in the blacklist",
                            response = ContainsResponse.class),
                    @ApiResponse(code = 400, message = "All parameters are required", response = ErrorMessage.class) })
    Response isBlackArtifactPresent(
            @QueryParam(value = "groupid") String groupId,
            @QueryParam(value = "artifactid") String artifactId,
            @QueryParam(value = "version") String version);

    @DELETE
    @Path(value = "/gav")
    @Consumes(value = MediaType.APPLICATION_JSON)
    @Produces(value = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Remove an artifact from the blacklist", response = SuccessResponse.class)
    SuccessResponse removeBlackArtifact(@ApiParam(value = GAV_JSON) RestArtifact artifact);

}
