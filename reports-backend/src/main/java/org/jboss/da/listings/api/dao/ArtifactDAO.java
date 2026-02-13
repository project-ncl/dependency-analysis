package org.jboss.da.listings.api.dao;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.jboss.da.listings.api.model.Artifact;
import org.jboss.da.listings.api.model.GA;

/**
 * 
 * @author Jozef Mrazek &lt;jmrazek@redhat.com&gt;
 * 
 */
public interface ArtifactDAO<T extends Artifact> extends GenericDAO<T> {

    /**
     * Finds artifact by given group id, artifact id and version.
     * 
     * @param groupId Group id of desired artifact.
     * @param artifactId Artifact id of desired artifact.
     * @param version Version of desired artifact.s
     * @return Optional of artifact or empty when not found.
     */
    Optional<T> findArtifact(String groupId, String artifactId, String version);

    List<T> findArtifact(Set<GA> gas);

    /**
     * Finds and return all artifacts.
     * 
     * @return List of found artifacts.
     */
    List<T> findAll();

}
