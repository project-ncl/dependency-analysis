package org.jboss.da.listings.api.dao;

import org.jboss.da.listings.api.model.GA;

import java.util.List;
import java.util.Optional;

public interface GADAO extends GenericDAO<GA> {

    /**
     * Finds GA with specific groupId and artifactId if doesn't exist creates it
     * @param groupId
     * @param artifactId
     * @return GA
     */
    GA findOrCreate(String groupId, String ArtifactId);

    /**
     * Finds GA with specific groupId and artifactId
     * @param groupId
     * @param artifactId
     * @return Optional of GA
     */
    Optional<GA> findGA(String groupId, String artifactId);

    /**
     * Finds all GA
     * @return list of GAs
     */
    List<GA> findAll();
}
