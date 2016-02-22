package org.jboss.da.rest.listings;

import org.jboss.da.listings.api.service.ArtifactService.SupportStatus;
import org.jboss.da.listings.api.service.BlackArtifactService;
import org.jboss.da.listings.api.service.ProductService;
import org.jboss.da.listings.api.service.ProductVersionService;
import org.jboss.da.listings.api.service.WLFiller;
import org.jboss.da.listings.api.service.WhiteArtifactService;
import org.jboss.da.listings.api.service.ArtifactService.ArtifactStatus;
import org.jboss.da.rest.listings.model.ContainsResponse;
import org.jboss.da.rest.listings.model.RestArtifact;
import org.jboss.da.rest.listings.model.RestProduct;
import org.jboss.da.rest.listings.model.RestProductArtifact;
import org.jboss.da.rest.listings.model.RestProductGAV;
import org.jboss.da.rest.listings.model.RestProductInput;
import org.jboss.da.rest.listings.model.SuccessResponse;
import org.jboss.da.rest.listings.model.WLFill;
import org.jboss.da.rest.model.ErrorMessage;

import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Collections;
import java.util.Optional;

import org.jboss.da.listings.api.model.BlackArtifact;
import org.jboss.da.listings.api.model.ProductVersion;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 *
 * @author Jozef Mrazek <jmrazek@redhat.com>
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
@Path("/listings")
@Api(value = "listings")
public class Artifacts {

    @Inject
    private RestConvert convert;

    @Inject
    private WLFiller filler;

    @Inject
    private WhiteArtifactService whiteService;

    @Inject
    private BlackArtifactService blackService;

    @Inject
    private ProductService productService;

    @Inject
    private ProductVersionService productVersionService;

    // //////////////////////////////////
    // Whitelist endpoints

    @GET
    @Path("/whitelist")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get all artifacts in the whitelist", responseContainer = "List",
            response = RestProductGAV.class)
    public Collection<RestProductGAV> getAllWhiteArtifacts() {
        return convert.toRestProductGAVList(productVersionService.getAll());
    }

    @POST
    @Path("/whitelist/fill/scm")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Fill artifacts from given git pom", response = SuccessResponse.class)
    public Response fillFromGitBom(
            @ApiParam(
                    value = "JSON object with keys 'scmUrl', 'revision', 'pomPath', list of 'repositories' and 'productId'") WLFill wlFill) {
        SuccessResponse response = new SuccessResponse();
        switch (filler.fillWhitelistFromPom(wlFill.getScmUrl(), wlFill.getRevision(),
                wlFill.getPomPath(), wlFill.getRepositories(), wlFill.getProductId())) {
            case PRODUCT_NOT_FOUND:
                response.setSuccess(false);
                response.setMessage("Product with this id not found");
                return Response.status(Status.NOT_FOUND).entity(response).build();
            case FILLED:
                response.setSuccess(true);
                return Response.ok().entity(response).build();
            case ANALYSER_ERROR:
                response.setSuccess(false);
                response.setMessage("Error while analysing pom file");
                return Response.status(Status.BAD_REQUEST).entity(response).build();
        }
        return Response.status(Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorMessage("There was some internal error")).build();
    }

    @POST
    @Path("/whitelist/fill/gav")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Fill artifacts from given maven pom gav",
            response = SuccessResponse.class)
    public Response fillFromGAVBom(
            @ApiParam(
                    value = "JSON object with keys 'groupId', 'artifactId', 'version' and 'productId'") RestProductArtifact a) {
        SuccessResponse response = new SuccessResponse();
        switch (filler.fillWhitelistFromGAV(a.getGroupId(), a.getArtifactId(), a.getVersion(),
                a.getProductId())) {
            case PRODUCT_NOT_FOUND:
                response.setSuccess(false);
                response.setMessage("Product with this id not found");
                return Response.status(Status.NOT_FOUND).entity(response).build();
            case FILLED:
                response.setSuccess(true);
                return Response.ok().entity(response).build();
            case ANALYSER_ERROR:
                response.setSuccess(false);
                response.setMessage("Error while analysing pom file");
                return Response.status(Status.BAD_REQUEST).entity(response).build();
        }
        return Response.status(Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorMessage("There was some internal error")).build();

    }

    @POST
    @Path("/whitelist/gav")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Add an artifact to the whitelist", response = SuccessResponse.class)
    @ApiResponses(value = { @ApiResponse(code = 409,
            message = "Can't add artifact to whitelist, artifact is blacklisted",
            response = ErrorMessage.class) })
    public Response addWhiteArtifact(
            @ApiParam(
                    value = "JSON object with keys 'groupId', 'artifactId', 'version' and 'productId'") RestProductArtifact artifact) {
        SuccessResponse response = new SuccessResponse();
        try {
            ArtifactStatus result = whiteService.addArtifact(artifact.getGroupId(),
                    artifact.getArtifactId(), artifact.getVersion(), artifact.getProductId());
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
    public SuccessResponse removeWhiteArtifact(
            @ApiParam(value = "JSON object with keys 'groupId', 'artifactId', and 'version'") RestArtifact artifact) {
        SuccessResponse response = new SuccessResponse();
        response.setSuccess(whiteService.removeArtifact(artifact.getGroupId(),
                artifact.getArtifactId(), artifact.getVersion()));
        return response;
    }

    @DELETE
    @Path("/whitelist/gavproduct")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Remove an artifact from the product", response = SuccessResponse.class)
    public SuccessResponse removeWhiteArtifactFromProduct(
            @ApiParam(value = "JSON object with keys 'groupId', 'artifactId', and 'version'") RestProductArtifact artifact) {
        SuccessResponse response = new SuccessResponse();
        response.setSuccess(whiteService.removeArtifractFromProductVersion(artifact.getGroupId(),
                artifact.getArtifactId(), artifact.getVersion(), artifact.getProductId()));
        return response;
    }

    // //////////////////////////////
    // whitelist products endpoints

    @POST
    @Path("/whitelist/product")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Add a product to the whitelist", response = SuccessResponse.class)
    @ApiResponses(value = { @ApiResponse(code = 400,
            message = "Name and version parameters are required", response = ErrorMessage.class) })
    public Response addProduct(
            @ApiParam(value = "JSON object with keys 'name', 'version' and optional 'status'") RestProductInput product) {
        SuccessResponse response = new SuccessResponse();
        if (product.getName().isEmpty() || product.getVersion().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage("Name and version parameters are required")).build();
        }
        if (product.getSupportStatus() == null) {
            product.setSupportStatus(SupportStatus.SUPPORTED);
        }
        response.setSuccess(productService.addProduct(product.getName(), product.getVersion(),
                product.getSupportStatus()));
        return Response.ok(response).build();
    }

    @DELETE
    @Path("/whitelist/product")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Remove a product from the whitelist", response = SuccessResponse.class)
    @ApiResponses(value = { @ApiResponse(code = 404, message = "Product not found",
            response = ErrorMessage.class) })
    public Response removeProduct(
            @ApiParam(value = "JSON object with keys 'name'and 'version'") RestProductInput product) {
        SuccessResponse response = new SuccessResponse();
        try {
            response.setSuccess(productService.removeProduct(product.getName(),
                    product.getVersion()));
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorMessage("Product not found")).build();
        }
        return Response.ok(response).build();
    }

    @PUT
    @Path("/whitelist/product")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Change support status of product in whitelist",
            response = SuccessResponse.class)
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Product not found", response = ErrorMessage.class),
            @ApiResponse(code = 400, message = "All parameters are required",
                    response = ErrorMessage.class) })
    public Response changeProductStatus(@ApiParam(
            value = "JSON object with keys 'name', 'version' and 'status'") RestProductInput product) {
        SuccessResponse response = new SuccessResponse();
        if (product.getSupportStatus() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage("All parameters are required")).build();
        }
        if (!productService.changeProductStatus(product.getName(), product.getVersion(),
                product.getSupportStatus())) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorMessage("Product not found")).build();
        }
        response.setSuccess(true);
        return Response.ok(response).build();
    }

    @GET
    @Path("/whitelist/products")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get all products from the whitelist", responseContainer = "List",
            response = RestProduct.class)
    public Collection<RestProduct> getProducts() {
        return convert.toRestProductList(productVersionService.getAll());
    }

    @GET
    @Path("/whitelist/product")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get product from the whitelist", responseContainer = "List",
            response = RestProduct.class)
    public Collection<RestProduct> getProduct(@QueryParam("id") Long id,
            @QueryParam("name") String name, @QueryParam("version") String version,
            @QueryParam("supportStatus") SupportStatus supportStatus) {
        return convert.toRestProductList(productVersionService.getProductVersions(id, name,
                version, supportStatus));
    }

    // //////////////////////////////
    // whitelist search endpoints

    @GET
    @Path("/whitelist/artifacts/product")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get all artifacts of product from the whitelist",
            responseContainer = "List", response = RestProductGAV.class)
    public Response artifactsOfProduct(@QueryParam("name") String name,
            @QueryParam("version") String version) {
        Optional<ProductVersion> pv = productVersionService.getProductVersion(name, version);
        if (pv.isPresent()) {
            return Response.ok(convert.toRestProductGAV(pv.get())).build();
        }
        return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorMessage("Product not found")).build();

    }

    @GET
    @Path("/whitelist/artifacts/gav")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get all artifacts with specified GAV from the whitelist",
            responseContainer = "List", response = RestProductGAV.class)
    public Response productsWithArtifactGAV(@QueryParam("groupid") String groupId,
            @QueryParam("artifactid") String artifactId, @QueryParam("version") String version) {

        return Response.ok(
                convert.fromRelationshipToRestProductGAVList(productVersionService
                        .getProductVersionsWithArtifactByGAV(groupId, artifactId, version)))
                .build();
    }

    @GET
    @Path("/whitelist/artifacts/status")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get all artifacts with specified status from the whitelist",
            responseContainer = "List", response = RestProductGAV.class)
    public Response productsWithArtifactStatus(@QueryParam("status") SupportStatus status) {

        return Response.ok(
                convert.toRestProductGAVList(productVersionService
                        .getProductVersionsWithArtifactsByStatus(status))).build();
    }

    @GET
    @Path("/whitelist/artifacts/gastatus")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get all artifacts with specified GA and status from the whitelist",
            responseContainer = "List", response = RestProductGAV.class)
    public Response productsWithArtifactGAAndStatus(@QueryParam("groupid") String groupId,
            @QueryParam("artifactid") String artifactId, @QueryParam("status") SupportStatus status) {

        return Response.ok(
                convert.fromRelationshipToRestProductGAVList(productVersionService
                        .getProductVersionsWithArtifactsByGAStatus(groupId, artifactId, status)))
                .build();
    }

    // //////////////////////////////////
    // Blacklist endpoints

    @GET
    @Path("/blacklist")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get all artifacts in the blacklist", responseContainer = "List",
            response = RestArtifact.class)
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
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Artifact is not in the blacklist",
                    response = ContainsResponse.class),
            @ApiResponse(code = 400, message = "All parameters are required",
                    response = ErrorMessage.class) })
    public Response isBlackArtifactPresent(@QueryParam("groupid") String groupId,
            @QueryParam("artifactid") String artifactId, @QueryParam("version") String version) {
        if (groupId == null || artifactId == null || version == null)
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage("All parameters are required")).build();
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
    public Response addBlackArtifact(
            @ApiParam(value = "JSON object with keys 'groupId', 'artifactId', and 'version'") RestArtifact artifact) {
        SuccessResponse response = new SuccessResponse();
        ArtifactStatus result = blackService.addArtifact(artifact.getGroupId(),
                artifact.getArtifactId(), artifact.getVersion());
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
    public SuccessResponse removeBlackArtifact(
            @ApiParam(value = "JSON object with keys 'groupId', 'artifactId', and 'version'") RestArtifact artifact) {
        SuccessResponse response = new SuccessResponse();
        response.setSuccess(blackService.removeArtifact(artifact.getGroupId(),
                artifact.getArtifactId(), artifact.getVersion()));
        return response;
    }
}
