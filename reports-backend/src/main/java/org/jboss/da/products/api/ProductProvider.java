package org.jboss.da.products.api;

import org.jboss.da.listings.model.ProductSupportStatus;
import org.jboss.da.model.rest.GA;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * Product Provider is used for providing information about products and their artifacts.
 * Each product is defined by its name and version and can have differen level of support.
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public interface ProductProvider {

    // Products getters
    /**
     * Returns all known products.
     * @return Set of all products.
     */
    Future<Set<Product>> getAllProducts();

    /**
     * Get all products with given name.
     * @param name Name of the products.
     * @return Set of all products with given name.
     */
    Future<Set<Product>> getProductsByName(String name);

    /**
     * Get all products with given support status.
     * @param status Support status of the products.
     * @return Set of all products with given support status.
     */
    Future<Set<Product>> getProductsByStatus(ProductSupportStatus status);

    // Artifacts getters
    /**
     * Get all artifacts in a given product.
     * @param product Product containing the artifacts.
     * @return Set of all artifacts.
     */
    Future<Set<Artifact>> getArtifacts(Product product);

    /**
     * Get all artifacts with given GroupId and ArtifactId and their products.
     * @param ga GroupID and ArtifactId of the artifacts.
     * @return Set of products and their artifacts.
     */
    Future<Set<ProductArtifacts>> getArtifacts(GA ga);

    /**
     * Get all artifacts with given GroupId and ArtifactId and their products. Use given
     * repository instead of the default one (apply only for Repository Providers).
     * @param ga GroupID and ArtifactId of the artifacts.
     * @param repository The repository to use insteady of the default one.
     * @return Set of products and their artifacts versions.
     */
    Future<Set<ProductArtifacts>> getArtifactsFromRepository(GA ga, String repository);

    /**
     * Get all artifacts with given GroupId and ArtifactId and their products, limited to products
     * with given support status..
     * @param ga GroupID and ArtifactId of the artifacts.
     * @param status Support status of the products.
     * @return Set of products and their artifacts.
     */
    Future<Set<ProductArtifacts>> getArtifacts(GA ga, ProductSupportStatus status);

    // Versions getters
    /**
     * Get all artifacts versions with given GroupId and ArtifactId and their products.
     * @param ga GroupID and ArtifactId of the artifacts.
     * @return Set of products and their artifacts versions.
     */
    Future<Map<Product, Set<String>>> getVersions(GA ga);

}
