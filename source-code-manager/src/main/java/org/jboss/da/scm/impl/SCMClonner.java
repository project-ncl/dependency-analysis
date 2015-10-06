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
import javax.enterprise.context.ApplicationScoped;
import java.io.File;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;

/**
 * This code is mostly based from the SCM code in pnc-local, written by Ahmed Abu Lawi (@ahmedlawi92)
 */
@ApplicationScoped
public class SCMClonner {

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
}
