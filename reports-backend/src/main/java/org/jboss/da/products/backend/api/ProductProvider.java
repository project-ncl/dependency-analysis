package org.jboss.da.products.backend.api;

import org.jboss.da.listings.model.ProductSupportStatus;
import org.jboss.da.model.rest.GA;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public interface ProductProvider {

    // Products getters
    Future<Set<Product>> getAllProducts();

    Future<Set<Product>> getProductsByName(String name);

    Future<Set<Product>> getProductsByStatus(ProductSupportStatus status);

    // Artifacts getters
    Future<Set<Artifact>> getArtifacts(Product product);

    Future<Set<ProductArtifacts>> getArtifacts(GA ga);

    Future<Set<ProductArtifacts>> getArtifacts(GA ga, ProductSupportStatus status);

    // Versions getters
    Future<Map<Product, Set<String>>> getVersions(GA ga);

}
