package org.jboss.da.products.api;

import org.jboss.da.listings.model.ProductSupportStatus;
import org.jboss.pnc.api.dependencyanalyzer.dto.Version;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Product Provider is used for providing information about products and their artifacts. Each product is defined by its
 * name and version and can have differen level of support.
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public interface ProductProvider {

    // Products getters
    /**
     * Returns all known products.
     *
     * @return Set of all products.
     */
    CompletableFuture<Set<Product>> getAllProducts();

    /**
     * Get all products with given name.
     *
     * @param name Name of the products.
     * @return Set of all products with given name.
     */
    CompletableFuture<Set<Product>> getProductsByName(String name);

    /**
     * Get all products with given support status.
     *
     * @param status Support status of the products.
     * @return Set of all products with given support status.
     */
    CompletableFuture<Set<Product>> getProductsByStatus(ProductSupportStatus status);

    // Artifacts getters
    /**
     * Get all artifacts in a given product.
     *
     * @param product Product containing the artifacts.
     * @return Set of all artifacts.
     */
    CompletableFuture<Set<Artifact>> getArtifacts(Product product);

    /**
     * Get all artifacts with the same name and type as given artifact and their products.
     *
     * @param artifact artifact which name and type will be used for searching.
     * @return Set of products and their artifacts.
     */
    CompletableFuture<Set<ProductArtifacts>> getArtifacts(Artifact artifact);

    /**
     * Get all artifacts with the same name and type as given artifact and their products, limited to * products with
     * given support status..
     *
     * @param artifact artifact which name and type will be used for searching.
     * @param status Support status of the products.
     * @return Set of products and their artifacts.
     */
    CompletableFuture<Set<ProductArtifacts>> getArtifacts(Artifact artifact, ProductSupportStatus status);

    // Versions getters
    /**
     * Get all artifacts versions with the same name and type as given artifact and their products.
     *
     * @param artifact artifact which name and type will be used for searching.
     * @return Set of products and their artifacts versions.
     */
    CompletableFuture<Map<Product, Set<Version>>> getVersions(Artifact artifact);

    /**
     * Get all artifacts versions with the same name and type as given artifact regardless if they are from product or
     * not.
     *
     * @param artifact artifact which name and type will be used for searching.
     * @return Set of artifacts versions.
     */
    CompletableFuture<Set<Version>> getAllVersions(Artifact artifact);
}
