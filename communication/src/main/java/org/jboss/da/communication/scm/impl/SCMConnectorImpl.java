package org.jboss.da.communication.scm.impl;

import java.io.File;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.maven.scm.ScmException;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.communication.pom.api.PomAnalyzer;
import org.jboss.da.communication.pom.model.MavenProject;
import org.jboss.da.communication.scm.api.SCMConnector;
import org.jboss.da.model.rest.GA;
import org.jboss.da.model.rest.GAV;
import org.jboss.da.scm.api.SCM;
import org.jboss.da.scm.api.SCMType;

import java.util.Map;
import java.util.Set;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
@ApplicationScoped
public class SCMConnectorImpl implements SCMConnector {

    @Inject
    private SCM scmManager;

    @Inject
    private PomAnalyzer pomAnalyzer;

    @Override
    public GAVDependencyTree getDependencyTreeOfRevision(String scmUrl, String revision, GAV gav)
            throws ScmException, PomAnalysisException {
        // git clone
        // TODO: hardcoded to git right now
        // TODO: enable the svn test if svn support is added
        File tempDir = scmManager.cloneRepository(SCMType.GIT, scmUrl, revision);

        GAVDependencyTree gavDependencyTree = pomAnalyzer.readRelationships(tempDir, gav);
        return gavDependencyTree;
    }

    @Override
    public Set<GAV> getToplevelDependencyOfRevision(String scmUrl, String revision, GAV gav)
            throws ScmException, PomAnalysisException {
        // git clone
        // TODO: hardcoded to git right now
        // TODO: enable the svn test if svn support is added
        File tempDir = scmManager.cloneRepository(SCMType.GIT, scmUrl, revision);

        return pomAnalyzer.getToplevelDepency(tempDir, gav);
    }

    @Override
    public GAVDependencyTree getDependencyTreeOfRevision(String scmUrl, String revision,
            String pomPath, List<String> repositories) throws ScmException, PomAnalysisException {
        // git clone
        // TODO: hardcoded to git right now
        // TODO: enable the svn test if svn support is added
        File tempDir = scmManager.cloneRepository(SCMType.GIT, scmUrl, revision);

        GAVDependencyTree gavDependencyTree = pomAnalyzer.readRelationships(tempDir, pomPath,
                repositories);

        return gavDependencyTree;
    }

    @Override
    public Optional<MavenProject> getPom(String scmUrl, String revision, String pomPath)
            throws ScmException {
        if (!pomPath.endsWith("pom.xml")) {
            pomPath += "/pom.xml";
        }

        // git clone
        // TODO: hardcoded to git right now
        File tempDir = scmManager.cloneRepository(SCMType.GIT, scmUrl, revision);

        return pomAnalyzer.readPom(new File(tempDir, pomPath));
    }

    @Override
    public boolean isGAVInRepository(String scmUrl, String revision, GAV gav) throws ScmException {
        // git clone
        // TODO: hardcoded to git right now
        File tempDir = scmManager.cloneRepository(SCMType.GIT, scmUrl, revision);

        return pomAnalyzer.getPOMFileForGAV(tempDir, gav).isPresent();
    }

    @Override
    public Optional<MavenProject> getPom(String scmUrl, String revision, GAV gav) throws ScmException {
        // git clone
        // TODO: hardcoded to git right now
        File tempDir = scmManager.cloneRepository(SCMType.GIT, scmUrl, revision);

        return pomAnalyzer.getPOMFileForGAV(tempDir, gav)
                .flatMap(file -> pomAnalyzer.readPom(file));
    }

    @Override
    public Map<GA, Set<GAV>> getDependenciesOfModules(String scmUrl, String revision,
            String pomPath, List<String> repositories) throws ScmException, PomAnalysisException {
        // git clone
        // TODO: hardcoded to git right now
        File tempDir = scmManager.cloneRepository(SCMType.GIT, scmUrl, revision);

        return pomAnalyzer.getDependenciesOfModules(tempDir, pomPath, repositories);
    }
}
