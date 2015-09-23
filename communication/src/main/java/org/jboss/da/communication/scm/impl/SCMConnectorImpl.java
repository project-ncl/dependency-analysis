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
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.communication.pom.PomAnalyzer;
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
    public Optional<GAVDependencyTree> getDependencyTreeOfRevision(String scmUrl, String revision,
            String pomPath) throws ScmException, PomAnalysisException {

        try {
            // git clone
            // TODO: hardcoded to git right now
            File tempDir = Files.createTempDirectory("cloned_repo").toFile();

            try {
                scmManager.cloneRepository(SCMType.GIT, scmUrl, revision, tempDir.toString());

                GAVDependencyTree gavDependencyTree = pomAnalyzer.readRelationships(tempDir,
                        new File(tempDir, pomPath));

                return Optional.ofNullable(gavDependencyTree);
            } finally {
                // cleanup
                FileUtils.deleteDirectory(tempDir);
            }
        } catch (IOException e) {
            throw new ScmException("Could not create temp directory for cloning the repository", e);
        }
    }

}
