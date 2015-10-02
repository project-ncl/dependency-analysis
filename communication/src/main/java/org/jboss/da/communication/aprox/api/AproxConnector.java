package org.jboss.da.communication.aprox.api;

import org.jboss.da.communication.CommunicationException;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;
import org.jboss.da.communication.model.GA;
import org.jboss.da.communication.model.GAV;

import java.util.List;
import java.util.Optional;
import org.jboss.da.communication.pom.model.MavenProject;

public interface AproxConnector {

    /**
     * Finds dependency trees of specific GAV
     * 
     * @param gav
     * @return Optional of dependency tree of GAV
     * @throws CommunicationException When there is problem with communication.
     */
    Optional<GAVDependencyTree> getDependencyTreeOfGAV(GAV gav) throws CommunicationException;

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
}
