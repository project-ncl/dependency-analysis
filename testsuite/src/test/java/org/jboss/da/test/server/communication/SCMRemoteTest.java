package org.jboss.da.test.server.communication;

import java.util.Optional;
import javax.inject.Inject;
import org.apache.maven.scm.ScmException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;
import org.jboss.da.communication.model.GAV;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.communication.scm.api.SCMConnector;
import org.jboss.da.test.ArquillianDeploymentFactory;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;

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
        return new ArquillianDeploymentFactory().createDeployment();
    }

    @Test
    public void testGetDependencyTreeOfRevision() throws ScmException, PomAnalysisException {
        String scmUrl = "https://github.com/project-ncl/dependency-analysis.git";

        // commit id for version 0.3
        String revision = "f34f4e1e";

        // normal case
        Optional<GAVDependencyTree> tree = scmConnector.getDependencyTreeOfRevision(scmUrl,
                revision, "");
        assertTrue(tree.isPresent());

        assertTrue(tree.get().getGav().equals(new GAV("org.jboss.da", "parent", "0.3.0")));
        assertTrue(tree.get().getDependencies().isEmpty());

        // with a slash in the pomPath
        Optional<GAVDependencyTree> treeWithSlash = scmConnector.getDependencyTreeOfRevision(
                scmUrl, revision, "/");
        assertTrue(treeWithSlash.isPresent());

        assertTrue(treeWithSlash.get().getGav().equals(new GAV("org.jboss.da", "parent", "0.3.0")));
        assertTrue(treeWithSlash.get().getDependencies().isEmpty());

        // with application in the pomPath
        Optional<GAVDependencyTree> treeApplication = scmConnector.getDependencyTreeOfRevision(
                scmUrl, revision, "application");
        assertTrue(treeApplication.isPresent());

        assertTrue(treeApplication.get().getGav()
                .equals(new GAV("org.jboss.da", "application", "0.3.0")));
        assertFalse(treeApplication.get().getDependencies().isEmpty());
    }

    @Test(expected = ScmException.class)
    public void testDependencyTreeOfRevisionWrongRevision() throws ScmException,
            PomAnalysisException {
        String scmUrl = "https://github.com/project-ncl/does_not_exist.git";
        Optional<GAVDependencyTree> tree = scmConnector.getDependencyTreeOfRevision(scmUrl, "", "");
    }

    @Test(expected = ScmException.class)
    public void testDependencyTreeOfRevisionWrongRevision2() throws ScmException,
            PomAnalysisException {
        String scmUrl = "https://github.com/project-ncl/dependency-analysis.git";
        String revision = "doesnotexist";
        Optional<GAVDependencyTree> tree = scmConnector.getDependencyTreeOfRevision(scmUrl,
                revision, "");
    }
}
