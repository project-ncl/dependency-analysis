package org.jboss.da.test.server.communication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.maven.scm.ScmException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.da.communication.CommunicationException;
import org.jboss.da.communication.aprox.api.AproxConnector;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;
import org.jboss.da.communication.model.GA;
import org.jboss.da.communication.model.GAV;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.test.ArquillianDeploymentFactory;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import static org.junit.Assert.assertFalse;

@RunWith(Arquillian.class)
public class AproxRemoteTest {

    @Inject
    private AproxConnector aproxConnector;

    @Deployment
    public static EnterpriseArchive createDeployment() {
        return new ArquillianDeploymentFactory().createDeployment();
    }

    @Test
    public void testGetVersionsOfGA() throws CommunicationException {
        GA ga = new GA("org.jboss.ballroom", "ballroom");
        List<String> ballroomTest = Arrays.asList(new String[] { "1.3.0.Final-redhat-1",
                "1.4.0.Final-redhat-1", "1.6.0.Final-redhat-1" });
        List<String> result = aproxConnector.getVersionsOfGA(ga);
        assertTrue(result.size() > 0);
        // future releases might make the size of result to be bigger
        assertTrue(ballroomTest.size() <= result.size());
        assertTrue(result.containsAll(ballroomTest));
    }

    @Test
    public void testGetCorrectDependencies() throws CommunicationException {
        GAV gav = new GAV("xom", "xom", "1.2.5");
        Optional<GAVDependencyTree> tree = aproxConnector.getDependencyTreeOfGAV(gav);

        Set<String> expectedDependencyGAV = new HashSet<>(
                Arrays.asList(new String[] {"xalan:xalan:2.7.0", "xerces:xercesImpl:2.8.0", "xml-apis:xml-apis:1.3.03"}));

        Set<String> receivedDependencyGAV = tree.get().getDependencies().stream()
                .map(f -> f.getGav().toString()).collect(Collectors.toSet());

        assertEquals(expectedDependencyGAV, receivedDependencyGAV);
    }

    @Test
    public void testNoGAVInRepository() throws CommunicationException {
        GAV gav = new GAV("do", "not-exist", "1.0");
        Optional<GAVDependencyTree> tree = aproxConnector.getDependencyTreeOfGAV(gav);
        assertFalse(tree.isPresent());
    }

    @Test
    public void testGetDependencyTreeOfRevision() throws ScmException, PomAnalysisException {
        String scmUrl = "https://github.com/project-ncl/dependency-analysis.git";

        // commit id for version 0.3
        String revision = "f34f4e1e";

        // normal case
        Optional<GAVDependencyTree> tree = aproxConnector.getDependencyTreeOfRevision(scmUrl,
                revision, "");
        assertTrue(tree.isPresent());

        assertTrue(tree.get().getGav().equals(new GAV("org.jboss.da", "parent", "0.3.0")));
        assertTrue(tree.get().getDependencies().isEmpty());

        // with a slash in the pomPath
        Optional<GAVDependencyTree> treeWithSlash = aproxConnector.getDependencyTreeOfRevision(
                scmUrl, revision, "/");
        assertTrue(treeWithSlash.isPresent());

        assertTrue(treeWithSlash.get().getGav().equals(new GAV("org.jboss.da", "parent", "0.3.0")));
        assertTrue(treeWithSlash.get().getDependencies().isEmpty());

        // with application in the pomPath
        Optional<GAVDependencyTree> treeApplication = aproxConnector.getDependencyTreeOfRevision(
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
        Optional<GAVDependencyTree> tree = aproxConnector.getDependencyTreeOfRevision(scmUrl, "",
                "");
    }

    @Test(expected = ScmException.class)
    public void testDependencyTreeOfRevisionWrongRevision2() throws ScmException,
            PomAnalysisException {
        String scmUrl = "https://github.com/project-ncl/dependency-analysis.git";
        String revision = "doesnotexist";
        Optional<GAVDependencyTree> tree = aproxConnector.getDependencyTreeOfRevision(scmUrl,
                revision, "");
    }
}
