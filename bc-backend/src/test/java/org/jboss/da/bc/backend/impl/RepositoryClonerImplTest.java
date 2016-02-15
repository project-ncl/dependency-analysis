package org.jboss.da.bc.backend.impl;

import org.jboss.da.scm.api.SCMType;
import org.junit.Test;

public class RepositoryClonerImplTest {

    private final RepositoryClonerImpl clonner = new RepositoryClonerImpl();

    @Test(expected = UnsupportedOperationException.class)
    public void testCloneNonGitRepository() throws Exception {
        clonner.cloneRepository("", "", SCMType.SVN, "");
    }

}
