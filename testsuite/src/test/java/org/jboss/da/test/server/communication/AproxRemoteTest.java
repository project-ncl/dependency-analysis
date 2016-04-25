package org.jboss.da.test.server.communication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.aprox.FindGAVDependencyException;
import org.jboss.da.communication.aprox.api.AproxConnector;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;
import org.jboss.da.model.rest.GA;
import org.jboss.da.model.rest.GAV;
import org.jboss.da.test.ArquillianDeploymentFactory;
import org.jboss.da.test.ArquillianDeploymentFactory.DepType;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;

@RunWith(Arquillian.class)
public class AproxRemoteTest {

    @Inject
    private AproxConnector aproxConnector;

    @Deployment
    public static EnterpriseArchive createDeployment() {
        return new ArquillianDeploymentFactory().createDeployment(DepType.REPORTS);
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
    public void testGetCorrectDependencies() throws CommunicationException, FindGAVDependencyException {
        GAV gav = new GAV("xom", "xom", "1.2.5");
        GAVDependencyTree tree = aproxConnector.getDependencyTreeOfGAV(gav);

        Set<String> expectedDependencyGAV = new HashSet<>(
                Arrays.asList(new String[] {"xalan:xalan:2.7.0", "xerces:xercesImpl:2.8.0", "xml-apis:xml-apis:1.3.03"}));

        Set<String> receivedDependencyGAV = tree.getDependencies().stream()
                .map(f -> f.getGav().toString()).collect(Collectors.toSet());

        assertEquals(expectedDependencyGAV, receivedDependencyGAV);
    }

    @Test(expected = FindGAVDependencyException.class)
    public void testNoGAVInRepository() throws CommunicationException, FindGAVDependencyException {
        GAV gav = new GAV("do", "not-exist", "1.0");
        aproxConnector.getDependencyTreeOfGAV(gav);
    }

    @Test
    public void noDependenciesForGAV() throws CommunicationException, FindGAVDependencyException {
        GAV gav = new GAV("org.scala-lang", "scala-library", "2.11.7");
        GAVDependencyTree reply = aproxConnector.getDependencyTreeOfGAV(gav);
        assertTrue(reply.getDependencies().isEmpty());
    }

    @Test
    public void findIfGAVInPublicRepo() throws CommunicationException {
        GAV not_exist = new GAV("do", "not-exist", "2.0");
        assertFalse(aproxConnector.doesGAVExistInPublicRepo(not_exist));

        GAV exist = new GAV("xom", "xom", "1.2.5");
        assertTrue(aproxConnector.doesGAVExistInPublicRepo(exist));
    }

}
