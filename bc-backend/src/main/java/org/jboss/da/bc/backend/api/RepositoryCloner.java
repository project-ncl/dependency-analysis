package org.jboss.da.bc.backend.api;

import org.jboss.da.common.CommunicationException;
import org.jboss.da.scm.api.SCMType;

/**
 * 
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
public interface RepositoryCloner {

    /**
     * Clones the repository to the internal systems. Source repository has to be available without authentication
     * 
     * @param url URL to the source repository
     * @param revision Revision, which will be cloned
     * @param scmType Type of the repository. Currently is supported only GIT
     * @param repositoryName Name of the new repository
     * @return URL to the new repository
     * @throws CommunicationException Thrown if the cloning process failed
     */
    String cloneRepository(String url, String revision, SCMType scmType, String repositoryName)
            throws CommunicationException;

}
