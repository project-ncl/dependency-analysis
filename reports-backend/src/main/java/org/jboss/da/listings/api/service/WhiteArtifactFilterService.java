package org.jboss.da.listings.api.service;

import java.util.List;
import java.util.Optional;

import org.jboss.da.listings.api.model.ProductVersion;
import org.jboss.da.listings.api.model.ProductVersionArtifactRelationship;
import org.jboss.da.listings.model.ProductSupportStatus;

/**
 * 
 * Provides access to white artifacts by applying the required filtering - taking into consideration artifacts listed against
 * ProductVersions, white list and black list.
 * 
 * @author fkujikis
 *
 */
public interface WhiteArtifactFilterService {

    /**
     * Remove all blacklisted artifacts from the provided list of ProductVersion instances.
     * 
     * @return List of ProductVersion instances such that for each instance, instance.getWhiteArtifacts() contains only
     *         whitelisted artifacts, with the blacklisted artifacts filtered out.
     */
    public List<ProductVersion> toProductsContainingOnlyWhiteArtifacts(List<ProductVersion> products);

    /**
     * Remove all blacklisted artifacts from the provided list of ProductVersionArtifactRelationship instances.
     * 
     * @return List of ProductVersionArtifactRelationship instances such that for each instance,
     *         instance.getProductVersion().getWhiteArtifacts() contains only whitelisted artifacts, with the blacklisted
     *         artifacts filtered out.
     */
    public List<ProductVersionArtifactRelationship> toProductRelsContainingOnlyWhiteArtifacts(
            List<ProductVersionArtifactRelationship> products);

    /**
     * Get ProductVersion with specific product name and version, such that it contains only white artifacts.
     * 
     * @param name
     * @param version
     * @return Optional of productVersion or empty
     */
    Optional<ProductVersion> getProductVersionWithWhiteArtifacts(String name, String version);

    /**
     * Get all ProductVersions containing only White artifacts.
     * 
     * @return List of productVersions with white artifacts
     */
    List<ProductVersion> getAllWithWhiteArtifacts();

    /**
     * Finds Artifacts of ProductVersions with specific support status, only containing white artifacts
     * 
     * @param status
     * @return List of productVersions with their artifacts
     */
    List<ProductVersion> getProductVersionsWithWhiteArtifactsByStatus(ProductSupportStatus status);

    /**
     * Find ProductVersions that contains Artifact with specific groupId, artifactId and version such that it contains only
     * white artifacts.
     * 
     * @param groupId
     * @param artifactId
     * @param version
     * @return List of productVersions with artifacts
     */
    List<ProductVersionArtifactRelationship> getProductVersionsWithWhiteArtifactsByGAV(String groupId, String artifactId,
            String version);

    /**
     * Find ProductVersions with specific status that contains Artifact with specific groupId, artifactId such that it contains
     * only white artifacts.
     * 
     * @param groupId
     * @param artifactId
     * @param status
     * @return
     */
    List<ProductVersionArtifactRelationship> getProductVersionsWithWhiteArtifactsByGAStatus(String groupId, String artifactId,
            ProductSupportStatus status);

}