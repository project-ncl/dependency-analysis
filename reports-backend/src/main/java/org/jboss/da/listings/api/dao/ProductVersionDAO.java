package org.jboss.da.listings.api.dao;

import org.jboss.da.listings.api.model.ProductVersionArtifactRelationship;
import org.jboss.da.listings.api.model.ProductVersion;
import org.jboss.da.listings.model.ProductSupportStatus;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public interface ProductVersionDAO extends GenericDAO<ProductVersion> {

    /**
     * Change support status of ProductVersion with specific product name and version
     * 
     * @param name
     * @param version
     * @param newStatus
     * @return True if change was successful otherwise false
     * @throws NoSuchElementException
     */
    boolean changeProductVersionStatus(String name, String version, ProductSupportStatus newStatus);

    /**
     * Finds ProductVersion with specific product name and version
     * 
     * @param name
     * @param version
     * @return Optional of productVersion or empty
     */
    Optional<ProductVersion> findProductVersion(String name, String version);

    /**
     * Finds ProductVersions with specific product name
     * 
     * @param name
     * @return List of productVersions
     */
    List<ProductVersion> findProductVersionsWithProduct(String name);

    /**
     * Finds all ProductVersions
     * 
     * @return List of productVersions
     */
    List<ProductVersion> findAll();

    /**
     * Find ProductsVersions that contains Artifact with specific groupId, artifactId and version
     * 
     * @param groupId
     * @param artifactId
     * @param version
     * @return List of productVersions
     */
    List<ProductVersion> findProductVersionsWithArtifact(String groupId, String artifactId,
            String version, boolean preciseVersion);

    /**
     * Finds ProductVersions with specific product id, product name, version or support status. All parameters are optional.
     * 
     * @param product id optional
     * @param product name optional
     * @param version optional
     * @param status optional
     * @return List of productVersions with specified parameters
     */
    List<ProductVersion> findProductVersions(Long id, String name, String version,
            ProductSupportStatus status);

    /**
     * Finds Artifacts of ProductVersions with specific support status
     * 
     * @param status
     * @return List of products with their artifacts
     */
    List<ProductVersion> findProductVersionsWithArtifactsByStatus(ProductSupportStatus status);

    /**
     * Find ProductVersions that contains Artifact with specific groupId, artifactId and version
     *  
     * @param groupId
     * @param artifactId
     * @param version
     * @return List of products with artifacts
     */
    List<ProductVersionArtifactRelationship> findProductVersionsWithArtifactByGAV(String groupId,
            String artifactId, String version);

    /**
     *  Find ProductVersions with specific status that contains Artifact with specific groupId, artifactId
     * 
     * @param groupId
     * @param artifactId
     * @param status
     * @return
     */
    List<ProductVersionArtifactRelationship> findProductVersionsWithArtifactsByGAStatus(
            String groupId, String artifactId, Optional<ProductSupportStatus> status);
}
