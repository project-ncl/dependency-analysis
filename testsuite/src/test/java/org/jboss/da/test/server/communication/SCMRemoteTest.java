package org.jboss.da.test.server.communication;

import javax.inject.Inject;

import org.apache.maven.scm.ScmException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.communication.scm.api.SCMConnector;
import org.jboss.da.model.rest.GAV;
import org.jboss.da.test.ArquillianDeploymentFactory;
import org.jboss.da.test.ArquillianDeploymentFactory.DepType;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
@RunWith(Arquillian.class)
public class SCMRemoteTest {

    @Inject
    private SCMConnector scmConnector;

    @Deployment
    public static EnterpriseArchive createDeployment() {
        return new ArquillianDeploymentFactory().createDeployment(DepType.REPORTS);
    }

    @Test
    public void testGetDependencyTreeOfRevision() throws ScmException, PomAnalysisException {
        String scmUrl = "https://github.com/project-ncl/dependency-analysis.git";

        // commit id for version 0.3
        String revision = "f34f4e1e";

        // normal case
        GAVDependencyTree tree = scmConnector.getDependencyTreeOfRevision(scmUrl, revision, "",
                Collections.emptyList());

        assertTrue(tree.getGav().equals(new GAV("org.jboss.da", "parent", "0.3.0")));
        assertTrue(tree.getDependencies().isEmpty());

        // with a slash in the pomPath
        GAVDependencyTree treeWithSlash = scmConnector.getDependencyTreeOfRevision(scmUrl,
                revision, "/", Collections.emptyList());

        assertTrue(treeWithSlash.getGav().equals(new GAV("org.jboss.da", "parent", "0.3.0")));
        assertTrue(treeWithSlash.getDependencies().isEmpty());

        // with application in the pomPath
        GAVDependencyTree treeApplication = scmConnector.getDependencyTreeOfRevision(scmUrl,
                revision, "application", Collections.emptyList());

        assertTrue(treeApplication.getGav().equals(new GAV("org.jboss.da", "application", "0.3.0")));
        assertFalse(treeApplication.getDependencies().isEmpty());
    }

    @Test(expected = ScmException.class)
    public void testDependencyTreeOfRevisionWrongRevision() throws ScmException,
            PomAnalysisException {
        String scmUrl = "https://github.com/project-ncl/does_not_exist.git";
        scmConnector.getDependencyTreeOfRevision(scmUrl, "", "", Collections.emptyList());
    }

    @Test(expected = ScmException.class)
    public void testDependencyTreeOfRevisionWrongRevision2() throws ScmException,
            PomAnalysisException {
        String scmUrl = "https://github.com/project-ncl/dependency-analysis.git";
        String revision = "doesnotexist";
        scmConnector.getDependencyTreeOfRevision(scmUrl, revision, "", Collections.emptyList());
    }
}
