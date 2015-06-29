package org.jboss.da.listings.api.dao;

import java.util.List;

import org.jboss.da.listings.api.model.Artifact;

/**
 * 
 * @author Jozef Mrazek <jmrazek@redhat.com>
 * 
 */
public interface ArtifactDAO<T extends Artifact> extends GenericDAO<T> {

    /**
     * Finds artifact by given group id, artifact id and version. When not found returns null.
     * 
     * @param groupId Group id of desired artifact.
     * @param artifactId Artifact id of desired artifact.
     * @param version Version of desired artifact.s
     * @return Found artifact or null when not found.
     */
    T findArtifactByGAV(String groupId, String artifactId, String version);

    /**
     * Finds and return all artifacts.
     * 
     * @return List of found artifacts.
     */
    List<T> findAll();

}
