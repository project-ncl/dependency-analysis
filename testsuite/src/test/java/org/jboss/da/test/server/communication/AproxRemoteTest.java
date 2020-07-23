package org.jboss.da.test.server.communication;

import org.jboss.da.test.server.AbstractServerTest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.aprox.FindGAVDependencyException;
import org.jboss.da.communication.aprox.api.AproxConnector;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;
import org.jboss.da.communication.cartographer.api.CartographerConnector;
import org.jboss.da.model.rest.GA;
import org.jboss.da.model.rest.GAV;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import org.junit.Before;

@RunWith(Arquillian.class)
public class AproxRemoteTest extends AbstractServerTest {

    @Inject
    private AproxConnector aproxConnector;

    @Inject
    private CartographerConnector cartographerConnector;

    @Before
    public void workaroundNoHttpResponseException() throws InterruptedException {
        Thread.sleep(2000);
    }

    @Test
    public void testGetVersionsOfGA() throws CommunicationException {
        GA ga = new GA("org.jboss.ballroom", "ballroom");
        List<String> ballroomTest = Arrays
                .asList(new String[] { "1.3.0.Final-redhat-1", "1.4.0.Final-redhat-1", "1.6.0.Final-redhat-1" });
        List<String> result = aproxConnector.getVersionsOfGA(ga);
        assertTrue(result.size() > 0);
        // future releases might make the size of result to be bigger
        assertTrue(ballroomTest.size() <= result.size());
        assertTrue(result.containsAll(ballroomTest));
    }

    @Test
    public void testGetVersionsOfNpm() throws CommunicationException {
        List<String> jqueryVersions = Arrays.asList(
                new String[] {
                        "1.11.0",
                        "1.11.0-beta3",
                        "1.11.0-rc1",
                        "1.11.1",
                        "1.11.1-beta1",
                        "1.11.1-rc1",
                        "1.11.1-rc2",
                        "1.11.2",
                        "1.11.3",
                        "1.12.0",
                        "1.12.1",
                        "1.12.2",
                        "1.12.3",
                        "1.12.4",
                        "1.5.1",
                        "1.5.1-redhat-1",
                        "1.6.2-redhat-1",
                        "1.6.2",
                        "1.6.3",
                        "1.7.2",
                        "1.7.3",
                        "1.8.2",
                        "1.8.3",
                        "1.9.1",
                        "2.1.0",
                        "2.1.0-beta2",
                        "2.1.0-beta3",
                        "2.1.0-rc1",
                        "2.1.1",
                        "2.1.1-beta1",
                        "2.1.1-rc1",
                        "2.1.1-rc2",
                        "2.1.2",
                        "2.1.3",
                        "2.1.4",
                        "2.2.0",
                        "2.2.1",
                        "2.2.2",
                        "2.2.3",
                        "2.2.4",
                        "3.0.0",
                        "3.0.0-alpha1",
                        "3.0.0-beta1",
                        "3.0.0-rc1",
                        "3.1.0",
                        "3.1.1",
                        "3.2.0",
                        "3.2.1",
                        "3.3.0",
                        "3.3.1" });
        List<String> result = aproxConnector.getVersionsOfNpm("jquery");
        assertEquals(jqueryVersions.size(), result.size());
        assertTrue(result.containsAll(jqueryVersions));
    }

    @Test
    public void testGetCorrectDependencies() throws CommunicationException, FindGAVDependencyException {
        GAV gav = new GAV("xom", "xom", "1.2.5");
        GAVDependencyTree tree = cartographerConnector.getDependencyTreeOfGAV(gav);

        Set<String> expectedDependencyGAV = new HashSet<>(
                Arrays.asList(
                        new String[] { "xalan:xalan:2.7.0", "xerces:xercesImpl:2.8.0", "xml-apis:xml-apis:1.3.03" }));

        Set<String> receivedDependencyGAV = tree.getDependencies()
                .stream()
                .map(f -> f.getGav().toString())
                .collect(Collectors.toSet());

        assertEquals(expectedDependencyGAV, receivedDependencyGAV);
    }

    @Test(expected = FindGAVDependencyException.class)
    public void testNoGAVInRepository() throws CommunicationException, FindGAVDependencyException {
        GAV gav = new GAV("do", "not-exist", "1.0");
        cartographerConnector.getDependencyTreeOfGAV(gav);
    }

    @Test
    public void noDependenciesForGAV() throws CommunicationException, FindGAVDependencyException {
        GAV gav = new GAV("org.scala-lang", "scala-library", "2.11.7");
        GAVDependencyTree reply = cartographerConnector.getDependencyTreeOfGAV(gav);
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
