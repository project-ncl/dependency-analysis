package org.jboss.da.communication.aprox.api;

import java.util.List;

import org.jboss.da.communication.aprox.model.GA;
import org.jboss.da.communication.aprox.model.GAV;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;

public interface AproxConnector {

    /**
     * Finds dependency trees of specific revision on scm url
     * 
     * @param scmUrl
     * @param revision
     * @param version
     * @return dependency tree of revision
     */
    GAVDependencyTree getDependencyTreeOfRevision(String scmUrl, String revision, String version);

    /**
     * Finds dependency trees of specific GAV
     * 
     * @param gav
     * @return dependency tree of GAV
     */
    GAVDependencyTree getDependencyTreeOfGAV(GAV gav);

    /**
     * Finds available versions of specific groupId artifactId
     * 
     * @param ga
     * @return list of versions
     */
    List<String> getVersionsOfGA(GA ga);
}
