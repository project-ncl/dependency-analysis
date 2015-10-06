package org.jboss.da.communication.scm.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.apache.maven.scm.ScmException;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;
import org.jboss.da.communication.model.GAV;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.communication.pom.api.PomAnalyzer;
import org.jboss.da.communication.pom.model.MavenProject;
import org.jboss.da.communication.scm.api.SCMConnector;
import org.jboss.da.scm.SCM;
import org.jboss.da.scm.SCMType;

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
        try {
            // git clone
            // TODO: hardcoded to git right now
            File tempDir = Files.createTempDirectory("cloned_repo").toFile();

            try {
                scmManager.cloneRepository(SCMType.GIT, scmUrl, revision, tempDir.toString());
                GAVDependencyTree gavDependencyTree = pomAnalyzer.readRelationships(tempDir, gav);
                return gavDependencyTree;
            } finally {
                // cleanup
                FileUtils.deleteDirectory(tempDir);
            }
        } catch (IOException e) {
            throw new ScmException("Could not create temp directory for cloning the repository", e);
        }
    }

    @Override
    public GAVDependencyTree getDependencyTreeOfRevision(String scmUrl, String revision,
            String pomPath) throws ScmException, PomAnalysisException {

        try {
            // git clone
            // TODO: hardcoded to git right now
            File tempDir = Files.createTempDirectory("cloned_repo").toFile();

            try {
                scmManager.cloneRepository(SCMType.GIT, scmUrl, revision, tempDir.toString());

                GAVDependencyTree gavDependencyTree = pomAnalyzer.readRelationships(tempDir,
                        new File(tempDir, pomPath));

                return gavDependencyTree;
            } finally {
                // cleanup
                FileUtils.deleteDirectory(tempDir);
            }
        } catch (IOException e) {
            throw new ScmException("Could not create temp directory for cloning the repository", e);
        }
    }

    @Override
    public Optional<MavenProject> getPom(String scmUrl, String revision, String pomPath)
            throws ScmException {
        if (!pomPath.endsWith("pom.xml")) {
            pomPath += "/pom.xml";
        }

        try {
            File tempDir = Files.createTempDirectory("cloned_repo").toFile();
            try {
                // git clone
                // TODO: hardcoded to git right now
                scmManager.cloneRepository(SCMType.GIT, scmUrl, revision, tempDir.toString());

                return pomAnalyzer.readPom(new File(tempDir, pomPath));
            } finally {
                // cleanup
                FileUtils.deleteDirectory(tempDir);
            }
        } catch (IOException ex) {
            throw new ScmException("Could not get pom file from SCM.", ex);
        }
    }

}
