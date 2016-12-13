package org.jboss.da.communication.aprox.api;

import org.jboss.da.common.CommunicationException;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.jboss.da.communication.pom.model.MavenProject;
import org.jboss.da.model.rest.GA;
import org.jboss.da.model.rest.GAV;

public interface AproxConnector {

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

    Optional<InputStream> getPomStream(GAV gav) throws CommunicationException;

    /**
     * Finds out if a particular gav is present in the public repository listed
     * by Aprox
     * @param gav
     * @return boolean
     * @throws CommunicationException if we can't connect to the aprox server
     */
    boolean doesGAVExistInPublicRepo(GAV gav) throws CommunicationException;
}
