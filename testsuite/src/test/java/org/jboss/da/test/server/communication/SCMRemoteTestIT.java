package org.jboss.da.test.server.communication;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.apache.maven.scm.ScmException;
import org.jboss.da.communication.indy.model.GAVDependencyTree;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.communication.scm.api.SCMConnector;
import org.jboss.da.model.rest.GAV;
import org.jboss.da.test.server.AbstractServerTest;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@QuarkusTest
public class SCMRemoteTestIT extends AbstractServerTest {

    @Inject
    SCMConnector scmConnector;

    @Test
    public void testGetDependencyTreeOfRevision() throws ScmException, PomAnalysisException {
        String scmUrl = "https://github.com/project-ncl/dependency-analysis.git";

        // commit id for version 0.3
        String revision = "f34f4e1e";

        // normal case
        GAVDependencyTree tree = scmConnector
                .getDependencyTreeOfRevision(scmUrl, revision, "", Collections.emptyList());

        assertEquals(tree.getGav(), new GAV("org.jboss.da", "parent", "0.3.0"));
        assertTrue(tree.getDependencies().isEmpty());

        // with a slash in the pomPath
        GAVDependencyTree treeWithSlash = scmConnector
                .getDependencyTreeOfRevision(scmUrl, revision, "/", Collections.emptyList());

        assertEquals(treeWithSlash.getGav(), new GAV("org.jboss.da", "parent", "0.3.0"));
        assertTrue(treeWithSlash.getDependencies().isEmpty());

        // with application in the pomPath
        GAVDependencyTree treeApplication = scmConnector
                .getDependencyTreeOfRevision(scmUrl, revision, "application", Collections.emptyList());

        assertEquals(treeApplication.getGav(), new GAV("org.jboss.da", "application", "0.3.0"));
        assertFalse(treeApplication.getDependencies().isEmpty());
    }

    @Test
    public void testDependencyTreeOfRevisionWrongRevision() {
        String scmUrl = "https://github.com/project-ncl/does_not_exist.git";
        assertThrows(
                ScmException.class,
                () -> scmConnector.getDependencyTreeOfRevision(scmUrl, "", "", Collections.emptyList()));
    }

    @Test
    public void testDependencyTreeOfRevisionWrongRevision2() {
        String scmUrl = "https://github.com/project-ncl/dependency-analysis.git";
        String revision = "doesnotexist";
        assertThrows(
                ScmException.class,
                () -> scmConnector.getDependencyTreeOfRevision(scmUrl, revision, "", Collections.emptyList()));
    }
}
