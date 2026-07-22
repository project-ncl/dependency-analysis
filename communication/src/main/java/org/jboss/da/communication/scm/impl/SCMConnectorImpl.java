package org.jboss.da.communication.scm.impl;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.communication.pom.api.PomAnalyzer;
import org.jboss.da.communication.pom.model.MavenProject;
import org.jboss.da.communication.repository.model.GAVDependencyTree;
import org.jboss.da.communication.scm.api.SCMConnector;
import org.jboss.da.model.rest.GA;
import org.jboss.da.model.rest.GAV;
import org.jboss.da.scm.api.SCM;
import org.jboss.da.scm.api.ScmException;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@ApplicationScoped
public class SCMConnectorImpl implements SCMConnector {

    @Inject
    SCM scmManager;

    @Inject
    PomAnalyzer pomAnalyzer;

    @Override
    public GAVDependencyTree getDependencyTreeOfRevision(String scmUrl, String revision, GAV gav)
            throws ScmException, PomAnalysisException {
        File tempDir = scmManager.cloneRepository(scmUrl, revision);

        GAVDependencyTree gavDependencyTree = pomAnalyzer.readRelationships(tempDir, gav);
        return gavDependencyTree;
    }

    @Override
    public Set<GAV> getToplevelDependencyOfRevision(String scmUrl, String revision, GAV gav)
            throws ScmException, PomAnalysisException {
        File tempDir = scmManager.cloneRepository(scmUrl, revision);

        return pomAnalyzer.getToplevelDependency(tempDir, gav);
    }

    @Override
    public GAVDependencyTree getDependencyTreeOfRevision(
            String scmUrl,
            String revision,
            String pomPath,
            List<String> repositories) throws ScmException, PomAnalysisException {
        File tempDir = scmManager.cloneRepository(scmUrl, revision);

        GAVDependencyTree gavDependencyTree = pomAnalyzer.readRelationships(tempDir, pomPath, repositories);

        return gavDependencyTree;
    }

    @Override
    public Set<GAV> getToplevelDependencyOfRevision(
            String scmUrl,
            String revision,
            String pomPath,
            List<String> repositories) throws ScmException, PomAnalysisException {
        File tempDir = scmManager.cloneRepository(scmUrl, revision);

        return pomAnalyzer.getToplevelDependency(tempDir, pomPath, repositories);
    }

    @Override
    public Optional<MavenProject> getPom(String scmUrl, String revision, String pomPath) throws ScmException {
        if (!pomPath.endsWith("pom.xml")) {
            pomPath += "/pom.xml";
        }

        File tempDir = scmManager.cloneRepository(scmUrl, revision);

        return pomAnalyzer.readPom(new File(tempDir, pomPath));
    }

    @Override
    public boolean isGAVInRepository(String scmUrl, String revision, GAV gav) throws ScmException {
        File tempDir = scmManager.cloneRepository(scmUrl, revision);

        return pomAnalyzer.getPOMFileForGAV(tempDir, gav).isPresent();
    }

    @Override
    public Optional<MavenProject> getPom(String scmUrl, String revision, GAV gav) throws ScmException {
        File tempDir = scmManager.cloneRepository(scmUrl, revision);

        return pomAnalyzer.getPOMFileForGAV(tempDir, gav).flatMap(file -> pomAnalyzer.readPom(file));
    }

    @Override
    public Map<GA, Set<GAV>> getDependenciesOfModules(
            String scmUrl,
            String revision,
            String pomPath,
            List<String> repositories) throws ScmException, PomAnalysisException {
        File tempDir = scmManager.cloneRepository(scmUrl, revision);

        return pomAnalyzer.getDependenciesOfModules(tempDir, pomPath, repositories);
    }
}
