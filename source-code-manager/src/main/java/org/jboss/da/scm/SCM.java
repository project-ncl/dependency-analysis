package org.jboss.da.scm;

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

/**
 * This code is mostly based from the SCM code in pnc-local, written by Ahmed Abu Lawi (@ahmedlawi92)
 */
@ApplicationScoped
public class SCM {

    private ScmManager scmManager;

    public SCM() {
        scmManager = new BasicScmManager();

        // Add new providers here
        scmManager.setScmProvider(SCMType.GIT.toString(), new JGitScmProvider());
        scmManager.setScmProvider(SCMType.SVN.toString(), new SvnExeScmProvider());
    }

    public boolean cloneRepository(SCMType scmType, String scmUrl, String revision, String cloneTo)
            throws ScmException {

        File buildDir = new File(cloneTo);
        if (!buildDir.exists()) {
            buildDir.mkdir();
        }

        ScmRepository repo = getScmRepository(
                String.format("scm:%s:%s", scmType.toString(), scmUrl), scmManager);
        return scmManager.checkOut(repo, new ScmFileSet(buildDir), new ScmTag(revision))
                .isSuccess();
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
