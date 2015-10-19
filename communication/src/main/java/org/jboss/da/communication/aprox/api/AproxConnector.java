package org.jboss.da.communication.aprox.api;

import org.jboss.da.communication.CommunicationException;
import org.jboss.da.communication.aprox.FindGAVDependencyException;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;
import org.jboss.da.communication.aprox.model.Repository;
import org.jboss.da.communication.model.GA;
import org.jboss.da.communication.model.GAV;

import java.util.List;
import java.util.Optional;

import org.jboss.da.communication.pom.model.MavenProject;

public interface AproxConnector {

    public enum RepositoryManipulationStatus {
        DONE, NAME_NOT_EXIST, NAME_EXIST_DIFFERENT_URL;
    };

    /**
     * Finds dependency trees of specific GAV
     * 
     * @param gav
     * @return Optional of dependency tree of GAV
     * @throws CommunicationException When there is problem with communication.
     *         FindGAVDependencyException if the GAV cannot be analyzed
     */
    Optional<GAVDependencyTree> getDependencyTreeOfGAV(GAV gav) throws CommunicationException,
            FindGAVDependencyException;

    /**
     * Finds available versions of specific groupId artifactId.
     * If the provided groupId artifactId is not found in repository, returns empty list.
     * 
     * @param ga
     * @return list of versions for given groupId artifactId in repository.
     * @throws CommunicationException When there is problem with communication.
     */
    List<String> getVersionsOfGA(GA ga) throws CommunicationException;

    Optional<MavenProject> getPom(GAV gav) throws CommunicationException;

    RepositoryManipulationStatus addRepositoryToGroup(Repository repository)
            throws CommunicationException;

    RepositoryManipulationStatus removeRepositoryFromGroup(Repository repository)
            throws CommunicationException;

    List<Repository> getAllRepositoriesFromGroup() throws CommunicationException;

    /**
     * Finds out if a particular gav is present in the public repository listed
     * by Aprox
     * @param gav
     * @return boolean
     * @throws CommunicationException if we can't connect to the aprox server
     */
    boolean doesGAVExistInPublicRepo(GAV gav) throws CommunicationException;
}
