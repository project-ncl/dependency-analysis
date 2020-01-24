package org.jboss.da.communication.cartographer.api;

import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.aprox.FindGAVDependencyException;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;
import org.jboss.da.model.rest.GAV;

public interface CartographerConnector {

    /**
     * Finds dependency trees of specific GAV.
     *
     * @param gav GAV
     * @return Dependency tree of the GAV
     * @throws CommunicationException When there is problem with communication.
     * @throws FindGAVDependencyException if the GAV cannot be analyzed
     */
    GAVDependencyTree getDependencyTreeOfGAV(GAV gav) throws CommunicationException, FindGAVDependencyException;
}
