package org.jboss.da.scm.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

import org.apache.commons.io.FileUtils;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.manager.BasicScmManager;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.svn.svnexe.SvnExeScmProvider;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.jboss.da.scm.api.SCMType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Facade, which simplifies operations with the SCM repositories
 *
 */
@ApplicationScoped
public class ScmFacade {

    private static final Logger logger = LoggerFactory.getLogger(ScmFacade.class);

    private final ScmManager scmManager;

    public ScmFacade() {
        scmManager = new BasicScmManager();

        // git is handled via the git command (see gitCliClone)
        // add new (non-git) providers here
        scmManager.setScmProvider(SCMType.SVN.toString(), new SvnExeScmProvider());
    }

    /**
     * Tries to do a shallow clone (clone only the requested revision) of the remote repository to the local directory.
     * First tries to do it using the git, otherwise falls back to ScmManager.
     * If it is not possible to do that, then it does the full clone.
     *
     * @param scmType Type of the repository
     * @param scmUrl URL to the repository
     * @param revision Revision of the repository, which should be cloned
     * @param cloneTo Directory, where the repository should be cloned
     * @throws ScmException Thrown if the clone of the repository fails
     */
    public void shallowCloneRepository(SCMType scmType, String scmUrl, String revision, File cloneTo)
            throws ScmException {
        if (!cloneTo.exists()) {
            cloneTo.mkdir();
        }

        if (scmType == SCMType.GIT) {
            if (!gitCliClone(scmType, scmUrl, revision, cloneTo)) {
                throw new ScmException(
                        "Cloning of git repository " + scmUrl + " at revision " + revision + " failed.");
            }
            return;
        }

        cloneRepository(scmType, scmUrl, revision, cloneTo);
    }

    /**
     * Process full clone of the remote repository to the local directory.
     *
     * @param scmType Type of the repository
     * @param scmUrl URL to the repository
     * @param revision Revision of the repository, which should be cloned
     * @param cloneTo Directory, where the repository should be cloned
     * @throws ScmException Thrown if the clone of the repository fails
     */
    public void cloneRepository(SCMType scmType, String scmUrl, String revision, File cloneTo) throws ScmException {
        ScmRepository repo = getScmRepository(scmType.getSCMUrl(scmUrl), scmManager);
        CheckOutScmResult checkOut = scmManager.checkOut(repo, new ScmFileSet(cloneTo), new ScmTag(revision));
        if (!checkOut.isSuccess()) {
            throw new ScmException("Repository was not cloned: " + checkOut.getProviderMessage());
        }
    }

    private ScmRepository getScmRepository(String scmUrl, ScmManager scmManager) throws ScmException {
        try {
            return scmManager.makeScmRepository(scmUrl);
        } catch (NoSuchScmProviderException ex) {
            throw new ScmException("Could not find a provider.", ex);
        } catch (ScmRepositoryException ex) {
            throw new ScmException("Error while connecting to the repository", ex);
        }
    }

    /**
     * Clones the repository using the git. A branch or tag is cloned shallowly.
     * Commit hash cannot be cloned shallowly and falls back to a full clone.
     * Returns true if the clone succeeded, false otherwise.
     *
     * @param scmType type of scmUrl
     * @param scmUrl link of repo to clone
     * @param revision specific revision to clone
     * @param cloneTo directory to clone the scmUrl
     * @return
     */
    private boolean gitCliClone(SCMType scmType, String scmUrl, String revision, File cloneTo) {
        if (revision == null || revision.isEmpty()) {
            return runGit(cloneTo, "clone", "--depth", "1", scmUrl, ".");
        }

        // works when the revision is a branch or tag name
        if (runGit(cloneTo, "clone", "--depth", "1", "--branch", revision, scmUrl, ".")) {
            return true;
        }

        cleanDirectory(cloneTo);

        // revision is most likely a commit hash (cannot be cloned shallowly)
        if (runGit(cloneTo, "clone", scmUrl, ".") && runGit(cloneTo, "checkout", revision)) {
            return true;
        }

        cleanDirectory(cloneTo);
        return false;
    }

    private boolean runGit(File workingDir, String... args) {
        List<String> command = new ArrayList<>(args.length + 1);
        command.add("git");
        Collections.addAll(command, args);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workingDir);
        pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        pb.redirectError(ProcessBuilder.Redirect.DISCARD);

        Map<String, String> env = pb.environment();

        // need to add those variables to tell git not to prompt us if repository does not exist
        env.put("GIT_ASKPASS", "/bin/echo"); // git <= 2.3
        env.put("GIT_TERMINAL_PROMPT", "0"); // git > 2.3

        try {
            return pb.start().waitFor() == 0;
        } catch (IOException ex) {
            logger.error("Could not run git {}", String.join(" ", args), ex);
            return false;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted while running git {}", String.join(" ", args), ex);
            return false;
        }
    }

    private void cleanDirectory(File directory) {
        try {
            FileUtils.cleanDirectory(directory);
        } catch (IOException ex) {
            logger.warn("Could not clean directory {}", directory, ex);
        }
    }
}
