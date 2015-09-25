package org.jboss.da.bc.backend.impl;

import org.jboss.da.scm.SCMType;
import org.junit.Test;

public class RepositoryClonerImplTest {

    private RepositoryClonerImpl clonner = new RepositoryClonerImpl();

    @Test(expected = UnsupportedOperationException.class)
    public void testCloneNonGitRepository() throws Exception {
        clonner.cloneRepository("", "", SCMType.SVN, "");
    }

    @Test
    public void testCloneGitRepository() throws Exception {
        String url = clonner.cloneRepository("https://github.com/project-ncl/dependency-analysis",
                "master", SCMType.GIT, "TEST_DA3");
        System.out.println("\n\n" + url + "\n\n");
    }

}
