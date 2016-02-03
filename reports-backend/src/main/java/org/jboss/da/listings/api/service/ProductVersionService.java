package org.jboss.da.listings.api.service;

import org.jboss.da.listings.api.model.ProductVersion;
import org.jboss.da.listings.api.model.ProductVersionArtifactRelationship;
import org.jboss.da.listings.api.service.ArtifactService.SupportStatus;

import java.util.List;
import java.util.Optional;

public interface ProductVersionService {

    /**
     * Get ProductVersion with specific product name and version
     * 
     * @param name
     * @param version
     * @return Optional of productVersion or empty
     */
    Optional<ProductVersion> getProductVersion(String name, String version);

    /**
     * Get ProductVersion with specific id
     * 
     * @param id
     * @return Optional of productVersion or empty
     */
    Optional<ProductVersion> getProductVersion(long id);

    /**
     * Get all ProductVersions
     * 
     * @return List of productVersions
     */
    List<ProductVersion> getAll();

    /**
     * Get ProductVersions that contains Artifact with specific groupId, artifactId and version
     * 
     * @param groupId
     * @param artifactId
     * @param version
     * @return List of products
     */
    List<ProductVersion> getProductVersionsOfArtifact(String groupId, String artifactId,
            String version);

    /**
     * Finds ProductVersion with specific product id, product name, version or support status. All parameters are optional.
     * 
     * @param id optional
     * @param name optional
     * @param version optional
     * @param status optional
     * @return List of productVersions with specified parameters
     */
    List<ProductVersion> getProductVersions(Long id, String name, String version,
            SupportStatus status);

    /**
     * Finds Artifacts of ProductVersions with specific support status
     * 
     * @param status
     * @return List of productVersions with their artifacts
     */
    List<ProductVersion> getProductVersionsWithArtifactsByStatus(SupportStatus status);

    /**
     * Find ProductVersions that contains Artifact with specific groupId, artifactId and version
     *  
     * @param groupId
     * @param artifactId
     * @param version
     * @return List of productVersions with artifacts
     */
    List<ProductVersionArtifactRelationship> getProductVersionsWithArtifactByGAV(String groupId,
            String artifactId, String version);

    /**
     *  Find ProductVersions with specific status that contains Artifact with specific groupId, artifactId
     *
     * @param groupId
     * @param artifactId
     * @param status
     * @return
     */
    List<ProductVersionArtifactRelationship> getProductVersionsWithArtifactsByGAStatus(
            String groupId, String artifactId, SupportStatus status);

    /**
     *  Find ProductVersions with any status that contains Artifact with specific groupId, artifactId
     *
     * @param groupId
     * @param artifactId
     * @return
     */
    List<ProductVersionArtifactRelationship> getProductVersionsWithArtifactsByGA(String groupId,
            String artifactId);

}
