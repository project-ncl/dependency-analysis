package org.jboss.da.communication.pnc;

import org.jboss.da.communication.repository.api.RepositoryException;
import org.jboss.da.model.rest.GA;

import java.util.List;

public interface PncConnector {

    /**
     * Finds available versions for given artifact (groupId:artifactId).
     *
     * @param ga Maven G:A
     * @return list of available versions for given groupId:artifactId in repository, never {@code null}
     * @throws RepositoryException When there is problem with communication.
     */
    List<String> getMavenVersions(GA ga, boolean temporaryBuild) throws RepositoryException;

    /**
     * Finds available versions for given npm package.
     *
     * @param packageName Name of the npm package
     * @return list of available versions for package in repository, never {@code null}
     * @throws RepositoryException When there is problem with communication.
     */
    List<String> getNpmVersions(String packageName, boolean temporaryBuild) throws RepositoryException;

}
