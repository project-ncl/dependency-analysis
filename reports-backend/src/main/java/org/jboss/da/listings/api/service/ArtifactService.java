package org.jboss.da.listings.api.service;

import java.util.List;

import org.jboss.da.listings.api.model.Artifact;

/**
 * 
 * @author Jozef Mrazek <jmrazek@redhat.com>
 *
 */
public interface ArtifactService<T extends Artifact> {

    /**
     * Add artifact to list.
     * 
     * @param groupId
     * @param artifactId
     * @param version
     */
    void addArtifact(String groupId, String artifactId, String version);

    /**
     * Finds artifact by given group id, artifact id and version. When not found returns null.
     * 
     * @param groupId
     * @param artifactId
     * @param version
     * @return
     */
    T getArtifact(String groupId, String artifactId, String version);

    /**
     * Finds if list contains artifact with specific groupId, artifactId and verison.
     * 
     * @param groupId
     * @param artifactId
     * @param version
     * @return True or false if list contains artifact.
     */
    boolean isArtifactPresent(String groupId, String artifactId, String version);

    /**
     * Finds and return all artifacts.
     * 
     * @return List of found artifacts.
     */
    List<T> getAll();

}
