package org.jboss.da.communication.aprox.api;

import org.jboss.da.communication.CommunicationException;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;
import org.jboss.da.communication.model.GA;
import org.jboss.da.communication.model.GAV;

import java.util.List;

public interface AproxConnector {

    /**
     * Finds dependency trees of specific revision on scm url
     * 
     * @param scmUrl
     * @param revision
     * @param pomPath
     * @return dependency tree of revision
     * @throws CommunicationException When there is problem with communication.
     */
    GAVDependencyTree getDependencyTreeOfRevision(String scmUrl, String revision, String pomPath)
            throws CommunicationException;

    /**
     * Finds dependency trees of specific GAV
     * 
     * @param gav
     * @return dependency tree of GAV
     * @throws CommunicationException When there is problem with communication.
     */
    GAVDependencyTree getDependencyTreeOfGAV(GAV gav) throws CommunicationException;

    /**
     * Finds available versions of specific groupId artifactId
     * 
     * @param ga
     * @return list of versions
     * @throws CommunicationException When there is problem with communication.
     */
    List<String> getVersionsOfGA(GA ga) throws CommunicationException;
}
