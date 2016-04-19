package org.jboss.da.communication.scm.api;

import java.util.List;
import java.util.Optional;

import org.apache.maven.scm.ScmException;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.communication.pom.model.MavenProject;
import org.jboss.da.model.rest.GA;
import org.jboss.da.model.rest.GAV;

import java.util.Map;
import java.util.Set;

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
     * @param repositories
     * @return Dependency tree of revision
     * @throws PomAnalysisException When there is problem with the pom analysis
     * @throws ScmException When checking out the repository failed
     */
    GAVDependencyTree getDependencyTreeOfRevision(String scmUrl, String revision, String pomPath,
            List<String> repositories) throws ScmException, PomAnalysisException;

    /**
     * Finds first level dependencies for each (transitive) module of given pom.
     *
     * @param scmUrl
     * @param revision
     * @param pomPath path to the pom for the analysis.
     * @param repositories optional repositories to take into account when analyzing poms.
     * @return Mapping from module GA to set of dependencies GAVs.
     * @throws PomAnalysisException When there is problem with the pom analysis
     * @throws ScmException When checking out the repository failed
     */
    Map<GA, Set<GAV>> getDependenciesOfModules(String scmUrl, String revision, String pomPath,
            List<String> repositories) throws ScmException, PomAnalysisException;

    /**
     * Finds dependency trees of specific revision on scm url
     *
     * @param scmUrl
     * @param revision
     * @param gav
     * @return Dependency tree of revision
     * @throws PomAnalysisException When there is problem with the pom analysis
     * @throws ScmException When checking out the repository failed
     */
    GAVDependencyTree getDependencyTreeOfRevision(String scmUrl, String revision, GAV gav)
            throws ScmException, PomAnalysisException;

    /**
     * Finds toplevel dependency of specific revision on scm url
     *
     * @param scmUrl
     * @param revision
     * @param gav
     * @return Dependencies of revision
     * @throws PomAnalysisException When there is problem with the pom analysis
     * @throws ScmException When checking out the repository failed
     */
    Set<GAV> getToplevelDependencyOfRevision(String scmUrl, String revision, GAV gav)
            throws ScmException, PomAnalysisException;

    /**
     * Returns true when GAV is in given repository.
     *
     * @param scmUrl
     * @param revision
     * @param gav
     * @return True when GAV is present in given repository
     * @throws ScmException When checking out the repository failed
     */
    boolean isGAVInRepository(String scmUrl, String revision, GAV gav) throws ScmException;

    Optional<MavenProject> getPom(String scmUrl, String revision, String pomPath)
            throws ScmException;

    public Optional<MavenProject> getPom(String scmUrl, String scmRevision, GAV gav)
            throws ScmException;
}
