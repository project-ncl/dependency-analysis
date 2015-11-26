package org.jboss.da.listings.api.service;

import java.util.List;

import org.jboss.da.communication.model.GAV;
import org.jboss.da.listings.api.model.Artifact;

/**
 * 
 * @author Jozef Mrazek <jmrazek@redhat.com>
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
public interface ArtifactService<T extends Artifact> {

    public enum STATUS {
        ADDED, NOT_MODIFIED, IS_BLACKLISTED, WAS_WHITELISTED
    };

    /**
     * Add artifact to list.
     * 
     * @param groupId
     * @param artifactId
     * @param version
     * @return Status of addition.
     */
    STATUS addArtifact(String groupId, String artifactId, String version);

    /**
     * Checks if list contains artifact with specific groupId, artifactId and version.
     * All restrictions and conversions are applied like using getArtifact method of specific list.
     *
     * @param groupId
     * @param artifactId
     * @param version
     * @return True if list contains the artifact otherwise false.
     */
    boolean isArtifactPresent(String groupId, String artifactId, String version);

    /**
     * Checks if list contains artifact with specific GAV.
     * All restrictions and conversions are applied like using getArtifact method of specific list.
     * @param gav
     * @return True if list contains the artifact otherwise false.
     */
    boolean isArtifactPresent(GAV gav);

    /**
     * Finds and return all artifacts.
     * 
     * @return List of found artifacts.
     */
    List<T> getAll();

    /**
     * Remove artifact from list.
     * Removes only exact match of artifact.
     * 
     * @param groupId
     * @param artifactId
     * @param version
     * @return True if artifact was deleted.
     */
    boolean removeArtifact(String groupId, String artifactId, String version);

}
