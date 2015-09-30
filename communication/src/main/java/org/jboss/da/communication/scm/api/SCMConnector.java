package org.jboss.da.communication.scm.api;

import java.util.Optional;
import org.apache.maven.scm.ScmException;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.communication.pom.model.MavenProject;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
public interface SCMConnector {

    /**
     * Finds dependency trees of specific revision on scm url
     *
     * @param scmUrl
     * @param revision
     * @param pomPath
     * @return Optional of dependency tree of revision
     * @throws PomAnalysisException When there is problem with the pom analysis
     * @throws ScmException When checking out the repository failed
     */
    Optional<GAVDependencyTree> getDependencyTreeOfRevision(String scmUrl, String revision,
            String pomPath) throws ScmException, PomAnalysisException;

    Optional<MavenProject> getPom(String scmUrl, String revision, String pomPath)
            throws ScmException;
}
