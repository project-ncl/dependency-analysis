package org.jboss.da.scm.impl;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.manager.BasicScmManager;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.git.jgit.JGitScmProvider;
import org.apache.maven.scm.provider.svn.svnexe.SvnExeScmProvider;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.jboss.da.scm.api.SCMType;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Facade, which simplifies operations with the SCM repositories
 * 
 */
@ApplicationScoped
public class ScmFacade {

    @Inject
    private Logger logger;

    private final ScmManager scmManager;

    public ScmFacade() {
        scmManager = new BasicScmManager();

        // Add new providers here
        scmManager.setScmProvider(SCMType.GIT.toString(), new JGitScmProvider());
        scmManager.setScmProvider(SCMType.SVN.toString(), new SvnExeScmProvider());
    }

    /**
     * Tries to do a shallow clone (clone only the requested revision) of the remote repository to the local directory. If it is not
     * possible to do that, then it does the full clone.
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

        if (shallowClone(scmType, scmUrl, revision, cloneTo)) {
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
        // if we can't shallow clone, then do the full git clone
        ScmRepository repo = getScmRepository(scmType.getSCMUrl(scmUrl), scmManager);
        CheckOutScmResult checkOut = scmManager.checkOut(repo, new ScmFileSet(cloneTo), new ScmTag(revision));
        if (!checkOut.isSuccess()) {
            throw new ScmException("Repository was not clonned: " + checkOut.getProviderMessage());
        }
    }

    /**
     * Stages selected local files in the SCM repository and pushes them to the remote repository.
     * 
     * @param scmType Type of the repository
     * @param scmUrl URL to the repository
     * @param baseDir Directory of the local repository
     * @param files Files, which should be pushed to the remote
     * @param commitMessage Commit message
     * @throws ScmException Thrown if the operation with the repository fails
     */
    public void commitAndPush(SCMType scmType, String scmUrl, File baseDir, List<File> files, String commitMessage)
            throws ScmException {
        ScmRepository repo = getScmRepository(scmType.getSCMUrl(scmUrl), scmManager);
        ScmFileSet scmFileSet = new ScmFileSet(baseDir, files);
        AddScmResult addResult = scmManager.add(repo, scmFileSet);

        if (!addResult.isSuccess())
            throw new ScmException(
                    "The manager wasn't able to ADD these files " + scmFileSet.toString() + " to the repository "
                            + repo);

        CheckInScmResult pushResult = scmManager.checkIn(repo, scmFileSet, commitMessage);

        if (!pushResult.isSuccess())
            throw new ScmException(
                    "The manager wasn't able to PUSH these files " + scmFileSet.toString() + " to the repository "
                            + repo);
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
     * If we were able to shallow clone, this function will return true. false otherwise
     *
     * @param scmType type of scmUrl
     * @param scmUrl link of repo to clone
     * @param revision specific revision to clone
     * @param cloneTo directory to clone the scmUrl
     * @return
     */
    private boolean shallowClone(SCMType scmType, String scmUrl, String revision, File cloneTo) {
        // we only support git for shallow cloning
        if (!scmType.equals(SCMType.GIT)) {
            return false;
        }

        try {
            ProcessBuilder pb;
            if (revision == null || revision.isEmpty()) {
                pb = new ProcessBuilder("git", "clone", "--depth", "1", scmUrl, ".");
            } else {
                pb = new ProcessBuilder("git", "clone", "--depth", "1", "--branch", revision, scmUrl, ".");
            }
            pb.directory(cloneTo);

            Map<String, String> env = pb.environment();

            // need to add those variables to tell git not to prompt us if repository does not exist
            env.put("GIT_ASKPASS", "/bin/echo"); // git <= 2.3
            env.put("GIT_TERMINAL_PROMPT", "0"); // git > 2.3

            Process p = pb.start();
            int status = p.waitFor();
            return status == 0;
        } catch (IOException | InterruptedException ex) {
            logger.error("Could not shallow clone", ex);
            return false;
        }
    }
}
