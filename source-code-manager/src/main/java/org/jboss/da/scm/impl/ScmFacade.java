package org.jboss.da.scm.impl;

import java.io.File;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

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
import org.jboss.da.scm.impl.git.GitCommands;
import org.jboss.da.scm.impl.git.GitException;

/**
 * Facade, which simplifies operations with the SCM repositories
 */
@ApplicationScoped
public class ScmFacade {

    private final ScmManager scmManager;

    @Inject
    GitCommands gitCommands;

    public ScmFacade() {
        scmManager = new BasicScmManager();

        // git is handled via the git command (see GitCommands)
        // add new (non-git) providers here
        scmManager.setScmProvider(SCMType.SVN.toString(), new SvnExeScmProvider());
    }

    /**
     * Tries to do a shallow clone (clone only the requested revision) of the remote repository to the local directory.
     * If it is not possible to do that, then it does the full clone.
     * For a git repository, it tries to do it using the git tool, otherwise uses ScmManager.
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
            try {
                gitCommands.cloneRepository(scmUrl, revision, cloneTo);
            } catch (GitException ex) {
                throw new ScmException(
                        "Cloning of git repository " + scmUrl + " at revision " + revision + " failed.",
                        ex);
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
}
