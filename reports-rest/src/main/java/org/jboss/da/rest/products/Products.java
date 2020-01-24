package org.jboss.da.rest.products;

import org.jboss.da.listings.api.model.ProductVersion;
import org.jboss.da.listings.api.service.ProductVersionService;
import org.jboss.da.model.rest.ErrorMessage;
import org.jboss.da.model.rest.GAV;
import org.jboss.da.products.api.ArtifactDiff;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.jboss.da.products.api.ProductsService;
import org.jboss.da.products.model.rest.GADiff;
import org.jboss.da.products.model.rest.ProductDiff;
import org.jboss.da.rest.listings.RestConvert;

import javax.ws.rs.core.Response;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.TreeSet;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Path("/products")
@Api(value = "products")
public class Products {

    @Inject
    private ProductsService products;

    @Inject
    private ProductVersionService productService;

    @Inject
    private RestConvert convert;

    @GET
    @Path("/diff")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get difference of two products", response = ProductDiff.class)
    public Response getProduct(@QueryParam("leftProduct") Long leftProduct, @QueryParam("rightProduct") Long rightProduct) {
        Optional<ProductVersion> left = productService.getProductVersion(leftProduct);
        if (!left.isPresent()) {
            return Response.status(NOT_FOUND).entity(new ErrorMessage(ErrorMessage.ErrorType.PRODUCT_NOT_FOUND,
                    "Product " + leftProduct + " doesn't exist.", null)).build();
        }
        Optional<ProductVersion> right = productService.getProductVersion(rightProduct);
        if (!right.isPresent()) {
            return Response.status(NOT_FOUND).entity(new ErrorMessage(ErrorMessage.ErrorType.PRODUCT_NOT_FOUND,
                    "Product " + rightProduct + " doesn't exist.", null)).build();
        }
        Set<ArtifactDiff> diff = products.difference(leftProduct, rightProduct);

        ProductDiff ret = new ProductDiff();
        ret.setLeftProduct(convert.toRestProduct(left.get()));
        ret.setRightProduct(convert.toRestProduct(right.get()));
        ret.setAdded(diff.stream().filter(ArtifactDiff::isAdded).map(d -> new GAV(d.getGa(), d.getRightVersion()))
                .collect(Collectors.toCollection(TreeSet::new)));
        ret.setRemoved(diff.stream().filter(ArtifactDiff::isRemoved).map(d -> new GAV(d.getGa(), d.getLeftVersion()))
                .collect(Collectors.toCollection(TreeSet::new)));
        ret.setChanged(diff.stream().filter(ArtifactDiff::isChanged)
                .map(d -> new GADiff(d.getGa(), d.getLeftVersion(), d.getRightVersion(), d.getDifference().toString()))
                .collect(Collectors.toCollection(TreeSet::new)));
        ret.setUnchanged(diff.stream().filter(ArtifactDiff::isUnchanged).map(d -> new GAV(d.getGa(), d.getLeftVersion()))
                .collect(Collectors.toCollection(TreeSet::new)));
        return Response.ok().entity(ret).build();
    }
}
