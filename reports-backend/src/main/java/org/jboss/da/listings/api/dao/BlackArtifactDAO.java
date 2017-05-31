package org.jboss.da.listings.api.dao;

import org.jboss.da.listings.api.model.BlackArtifact;

import java.util.List;

/**
 * 
 * @author Jozef Mrazek &lt;jmrazek@redhat.com&gt;
 *
 */
public interface BlackArtifactDAO extends ArtifactDAO<BlackArtifact> {

    /**
     * Finds artifacts with given group id and artifact id.
     *
     * @param groupId Group id of desired artifacts.
     * @param artifactId Artifact id of desired artifacts.
     * @return List of artifacts.
     */
    List<BlackArtifact> findArtifacts(String groupId, String artifactId);

}
