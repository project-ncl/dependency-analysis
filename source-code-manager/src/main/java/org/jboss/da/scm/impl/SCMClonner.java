package org.jboss.da.scm.impl;

import org.jboss.da.scm.api.SCMType;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.manager.BasicScmManager;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.git.jgit.JGitScmProvider;
import org.apache.maven.scm.provider.svn.svnexe.SvnExeScmProvider;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.slf4j.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * This code is mostly based from the SCM code in pnc-local, written by Ahmed Abu Lawi (@ahmedlawi92)
 */
@ApplicationScoped
public class SCMClonner {

    @Inject
    private Logger logger;

    private ScmManager scmManager;

    public SCMClonner() {
        scmManager = new BasicScmManager();

        // Add new providers here
        scmManager.setScmProvider(SCMType.GIT.toString(), new JGitScmProvider());
        scmManager.setScmProvider(SCMType.SVN.toString(), new SvnExeScmProvider());
    }

    public void cloneRepository(SCMType scmType, String scmUrl, String revision, File cloneTo)
            throws ScmException {

        if (!cloneTo.exists()) {
            cloneTo.mkdir();
        }

        if (shallowClone(scmType, scmUrl, revision, cloneTo)) {
            return;
        }
        // if we can't shallow clone, then do the full git clone
        ScmRepository repo = getScmRepository(scmType.getSCMUrl(scmUrl), scmManager);
        CheckOutScmResult checkOut = scmManager.checkOut(repo, new ScmFileSet(cloneTo), new ScmTag(
                revision));
        if (!checkOut.isSuccess()) {
            throw new ScmException("Repository was not clonned: " + checkOut.getProviderMessage());
        }
    }

    private ScmRepository getScmRepository(String scmUrl, ScmManager scmManager)
            throws ScmException {
        try {
            return scmManager.makeScmRepository(scmUrl);
        } catch (NoSuchScmProviderException ex) {
            throw new ScmException("Could not find a provider.", ex);
        } catch (ScmRepositoryException ex) {
            throw new ScmException("Error while connecting to the repository", ex);
        }
    }

    /**
     * If we were able to shallow clone, this function will return true.
     * false otherwise
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
                pb = new ProcessBuilder("git", "clone", "--depth", "1", "--branch", revision,
                        scmUrl, ".");
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
