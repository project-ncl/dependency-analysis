package org.jboss.da.communication.aprox.api;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.jboss.da.communication.pom.model.MavenProject;
import org.jboss.da.communication.repository.api.RepositoryException;
import org.jboss.da.model.rest.GA;
import org.jboss.da.model.rest.GAV;

public interface AproxConnector {

    /**
     * Finds available versions of specific groupId artifactId.
     * If the provided groupId artifactId is not found in repository, returns empty list.
     * 
     * @param ga
     * @return list of versions for given groupId artifactId in repository.
     * @throws RepositoryException When there is problem with communication.
     */
    List<String> getVersionsOfGA(GA ga) throws RepositoryException;

    /**
     * Finds available versions of specific groupId artifactId.
     * If the provided groupId artifactId is not found in repository, returns empty list.
     *
     * @param ga
     * @param repository Search versions in provided repository instead of default one.
     * @return list of versions for given groupId artifactId in repository.
     * @throws RepositoryException When there is problem with communication.
     */
    List<String> getVersionsOfGA(GA ga, String repository) throws RepositoryException;

    Optional<MavenProject> getPom(GAV gav) throws RepositoryException;

    Optional<InputStream> getPomStream(GAV gav) throws RepositoryException;

    /**
     * Finds out if a particular gav is present in the public repository listed
     * by Aprox
     * @param gav
     * @return boolean
     * @throws RepositoryException if we can't connect to the aprox server
     */
    boolean doesGAVExistInPublicRepo(GAV gav) throws RepositoryException;
}
