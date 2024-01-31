package org.jboss.da.listings.api.dao;

import org.jboss.da.listings.api.model.GA;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface GADAO extends GenericDAO<GA> {

    /**
     * Finds GA with specific groupId and artifactId if doesn't exist creates it
     * 
     * @param groupId
     * @param artifactId
     * @return GA
     */
    GA findOrCreate(String groupId, String artifactId);

    /**
     * Finds GA with specific groupId and artifactId
     * 
     * @param groupId
     * @param artifactId
     * @return Optional of GA
     */
    Optional<GA> findGA(String groupId, String artifactId);

    /**
     * Finds GA with specific groupId and artifactId from the provided set.
     *
     * @param gas Set of group ids and artifact ids to search.
     * @return List of found GAss.
     */
    Set<GA> findGAs(Set<org.jboss.da.model.rest.GA> gas);

    /**
     * Finds all GA
     * 
     * @return list of GAs
     */
    List<GA> findAll();
}
