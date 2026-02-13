package org.jboss.da.rest.listings;

import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jboss.da.listings.api.model.ProductVersion;
import org.jboss.da.listings.api.service.ArtifactService.ArtifactStatus;
import org.jboss.da.listings.api.service.BlackArtifactService;
import org.jboss.da.listings.api.service.ProductService;
import org.jboss.da.listings.api.service.ProductVersionService;
import org.jboss.da.listings.api.service.WLFiller;
import org.jboss.da.listings.api.service.WhiteArtifactFilterService;
import org.jboss.da.listings.api.service.WhiteArtifactService;
import org.jboss.da.listings.model.ProductSupportStatus;
import org.jboss.da.listings.model.rest.RestArtifact;
import org.jboss.da.listings.model.rest.RestProduct;
import org.jboss.da.listings.model.rest.RestProductArtifact;
import org.jboss.da.listings.model.rest.RestProductGAV;
import org.jboss.da.listings.model.rest.RestProductInput;
import org.jboss.da.listings.model.rest.SuccessResponse;
import org.jboss.da.listings.model.rest.WLFill;
import org.jboss.da.model.rest.ErrorMessage;
import org.jboss.da.validation.Validation;
import org.jboss.da.validation.ValidationException;

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

import java.util.Collection;
import java.util.Optional;

/**
 *
 * @author Jozef Mrazek &lt;jmrazek@redhat.com&gt;
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
@Path("/listings")
@Tag(name = "deprecated")
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

    @Inject
    private WhiteArtifactFilterService whiteArtifactFilterService;

    @Inject
    private Validation validation;

    // //////////////////////////////////
    // Whitelist endpoints

    @GET
    @Path("/whitelist")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(deprecated = true)
    @WithSpan()
    public Collection<RestProductGAV> getAllWhiteArtifacts() {
        return convert.toRestProductGAVList(whiteArtifactFilterService.getAllWithWhiteArtifacts());
    }

    @POST
    @Path("/whitelist/fill/scm")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(deprecated = true)
    @WithSpan()
    public Response fillFromGitBom(@SpanAttribute(value = "wlFill") WLFill wlFill) {

        SuccessResponse response = new SuccessResponse();
        try {
            validation.validation(wlFill, "Filling product from GIT POM failed");
        } catch (ValidationException e) {
            return e.getResponse();
        }
        WLFiller.WLStatus result = filler.fillWhitelistFromPom(
                wlFill.getScmUrl(),
                wlFill.getRevision(),
                wlFill.getPomPath(),
                wlFill.getRepositories(),
                wlFill.getProductId());
        switch (result) {
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
            default:
                return Response.status(Status.INTERNAL_SERVER_ERROR)
                        .entity(
                                new ErrorMessage(
                                        ErrorMessage.ErrorType.UNEXPECTED_SERVER_ERR,
                                        "Unexpected server error occurred",
                                        "Result was " + result))
                        .build();
        }
    }

    @POST
    @Path("/whitelist/fill/gav")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(deprecated = true)
    @WithSpan()
    public Response fillFromGAVBom(@SpanAttribute(value = "a") RestProductArtifact a) {
        SuccessResponse response = new SuccessResponse();
        WLFiller.WLStatus result = filler
                .fillWhitelistFromGAV(a.getGroupId(), a.getArtifactId(), a.getVersion(), a.getProductId());
        switch (result) {
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
            case POM_NOT_FOUND:
                response.setSuccess(false);
                response.setMessage("Could not found pom file in repository");
                return Response.status(Status.NOT_FOUND).entity(response).build();
            default:
                return Response.status(Status.INTERNAL_SERVER_ERROR)
                        .entity(
                                new ErrorMessage(
                                        ErrorMessage.ErrorType.UNEXPECTED_SERVER_ERR,
                                        "Unexpected server error occurred",
                                        "Result was " + result))
                        .build();
        }
    }

    @POST
    @Path("/whitelist/gav")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(deprecated = true)
    @WithSpan()
    public Response addWhiteArtifact(@SpanAttribute(value = "artifact") RestProductArtifact artifact) {
        SuccessResponse response = new SuccessResponse();
        try {
            ArtifactStatus result = whiteService.addArtifact(
                    artifact.getGroupId(),
                    artifact.getArtifactId(),
                    artifact.getVersion(),
                    artifact.getProductId());
            switch (result) {
                case ADDED:
                    response.setSuccess(true);
                    return Response.ok(response).build();
                case IS_BLACKLISTED:
                    return Response.status(Response.Status.CONFLICT)
                            .entity(
                                    new ErrorMessage(
                                            ErrorMessage.ErrorType.BLACKLIST,
                                            "Can't add artifact to whitelist, artifact is blacklisted",
                                            null))
                            .build();
                case NOT_MODIFIED:
                    response.setSuccess(false);
                    return Response.ok(response).build();
                default:
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(
                                    new ErrorMessage(
                                            ErrorMessage.ErrorType.UNEXPECTED_SERVER_ERR,
                                            "Unexpected server error occurred.",
                                            "Result was " + result))
                            .build();
            }
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        }
    }

    @DELETE
    @Path("/whitelist/gav")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(deprecated = true)
    @WithSpan()
    public SuccessResponse removeWhiteArtifact(@SpanAttribute(value = "artifact") RestArtifact artifact) {
        SuccessResponse response = new SuccessResponse();
        response.setSuccess(
                whiteService.removeArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion()));
        return response;
    }

    @DELETE
    @Path("/whitelist/gavproduct")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(deprecated = true)
    @WithSpan()
    public SuccessResponse removeWhiteArtifactFromProduct(
            @SpanAttribute(value = "artifact") RestProductArtifact artifact) {
        SuccessResponse response = new SuccessResponse();
        response.setSuccess(
                whiteService.removeArtifractFromProductVersion(
                        artifact.getGroupId(),
                        artifact.getArtifactId(),
                        artifact.getVersion(),
                        artifact.getProductId()));
        return response;
    }

    // //////////////////////////////
    // whitelist products endpoints

    @POST
    @Path("/whitelist/product")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(deprecated = true)
    @WithSpan()
    public Response addProduct(@SpanAttribute(value = "product") RestProductInput product) {
        SuccessResponse response = new SuccessResponse();
        if (product.getName().isEmpty() || product.getVersion().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(
                            new ErrorMessage(
                                    ErrorMessage.ErrorType.PARAMS_REQUIRED,
                                    "Name and version parameters are required",
                                    null))
                    .build();
        }
        if (product.getSupportStatus() == null) {
            product.setSupportStatus(ProductSupportStatus.SUPPORTED);
        }
        response.setSuccess(
                productService.addProduct(product.getName(), product.getVersion(), product.getSupportStatus()));
        Long id = productVersionService.getProductVersion(product.getName(), product.getVersion()).get().getId();
        response.setId(id);
        return Response.ok(response).build();
    }

    @DELETE
    @Path("/whitelist/product")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(deprecated = true)
    @WithSpan()
    public Response removeProduct(@SpanAttribute(value = "product") RestProductInput product) {
        SuccessResponse response = new SuccessResponse();
        try {
            response.setSuccess(productService.removeProduct(product.getName(), product.getVersion()));
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(
                            new ErrorMessage(
                                    ErrorMessage.ErrorType.PRODUCT_NOT_FOUND,
                                    "Product not found",
                                    e.getMessage()))
                    .build();
        }
        return Response.ok(response).build();
    }

    @PUT
    @Path("/whitelist/product")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(deprecated = true)
    @WithSpan()
    public Response changeProductStatus(@SpanAttribute(value = "product") RestProductInput product) {
        SuccessResponse response = new SuccessResponse();
        if (product.getSupportStatus() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(
                            new ErrorMessage(
                                    ErrorMessage.ErrorType.PARAMS_REQUIRED,
                                    "All parameters are required",
                                    "Parameter support status is required"))
                    .build();
        }
        if (!productService.changeProductStatus(product.getName(), product.getVersion(), product.getSupportStatus())) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorMessage(ErrorMessage.ErrorType.PRODUCT_NOT_FOUND, "Product not found", null))
                    .build();
        }
        response.setSuccess(true);
        return Response.ok(response).build();
    }

    @GET
    @Path("/whitelist/products")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(deprecated = true)
    @WithSpan()
    public Collection<RestProduct> getProducts() {
        return convert.toRestProductList(productVersionService.getAll());
    }

    @GET
    @Path("/whitelist/product")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(deprecated = true)
    @WithSpan()
    public Collection<RestProduct> getProduct(
            @SpanAttribute(value = "id") @QueryParam("id") Long id,
            @SpanAttribute(value = "name") @QueryParam("name") String name,
            @SpanAttribute(value = "version") @QueryParam("version") String version,
            @SpanAttribute(value = "supportStatus") @QueryParam("supportStatus") ProductSupportStatus supportStatus) {
        return convert.toRestProductList(productVersionService.getProductVersions(id, name, version, supportStatus));
    }

    // //////////////////////////////
    // whitelist search endpoints

    @GET
    @Path("/whitelist/artifacts/product")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(deprecated = true)
    @WithSpan()
    public Response artifactsOfProduct(
            @SpanAttribute(value = "name") @QueryParam("name") String name,
            @SpanAttribute(value = "version") @QueryParam("version") String version) {
        Optional<ProductVersion> pv = whiteArtifactFilterService.getProductVersionWithWhiteArtifacts(name, version);
        if (pv.isPresent()) {
            return Response.ok(convert.toRestProductGAV(pv.get())).build();
        }
        return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorMessage(ErrorMessage.ErrorType.PRODUCT_NOT_FOUND, "Product not found", null))
                .build();

    }

    @GET
    @Path("/whitelist/artifacts/gav")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(deprecated = true)
    @WithSpan()
    public Response productsWithArtifactGAV(
            @SpanAttribute(value = "groupId") @QueryParam("groupid") String groupId,
            @SpanAttribute(value = "artifactId") @QueryParam("artifactid") String artifactId,
            @SpanAttribute(value = "version") @QueryParam("version") String version) {

        return Response
                .ok(
                        convert.fromRelationshipToRestProductGAVList(
                                whiteArtifactFilterService
                                        .getProductVersionsWithWhiteArtifactsByGAV(groupId, artifactId, version)))
                .build();
    }

    @GET
    @Path("/whitelist/artifacts/status")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(deprecated = true)
    @WithSpan()
    public Response productsWithArtifactStatus(
            @SpanAttribute(value = "status") @QueryParam("status") ProductSupportStatus status) {

        return Response
                .ok(
                        convert.toRestProductGAVList(
                                whiteArtifactFilterService.getProductVersionsWithWhiteArtifactsByStatus(status)))
                .build();
    }

    @GET
    @Path("/whitelist/artifacts/gastatus")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(deprecated = true)
    @WithSpan()
    public Response productsWithArtifactGAAndStatus(
            @SpanAttribute(value = "groupId") @QueryParam("groupid") String groupId,
            @SpanAttribute(value = "artifactId") @QueryParam("artifactid") String artifactId,
            @SpanAttribute(value = "status") @QueryParam("status") ProductSupportStatus status) {

        return Response
                .ok(
                        convert.fromRelationshipToRestProductGAVList(
                                whiteArtifactFilterService
                                        .getProductVersionsWithWhiteArtifactsByGAStatus(groupId, artifactId, status)))
                .build();
    }

}
