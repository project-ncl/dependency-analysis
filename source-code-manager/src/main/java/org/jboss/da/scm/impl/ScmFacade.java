package org.jboss.da.scm.impl;

import java.io.File;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.da.scm.api.ScmException;
import org.jboss.da.scm.impl.git.GitCommands;
import org.jboss.da.scm.impl.git.GitException;

/**
 * Facade, which simplifies operations with the SCM repositories
 */
@ApplicationScoped
public class ScmFacade {

    @Inject
    GitCommands gitCommands;

    /**
     * Clones the remote repository at the given revision to the local directory.
     * The revision is cloned shallowly if possible, otherwise the whole repository is cloned.
     *
     * @param scmUrl URL to the repository
     * @param revision Revision of the repository, which should be cloned
     * @param cloneTo Directory, where the repository should be cloned
     * @throws ScmException Thrown if the clone of the repository fails
     */
    public void cloneRepository(String scmUrl, String revision, File cloneTo) throws ScmException {
        if (!cloneTo.exists()) {
            cloneTo.mkdir();
        }

        try {
            gitCommands.cloneRepository(scmUrl, revision, cloneTo);
        } catch (GitException ex) {
            throw new ScmException(
                    "Cloning of git repository " + scmUrl + " at revision " + revision + " failed.",
                    ex);
        }
    }
}