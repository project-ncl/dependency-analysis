package org.jboss.da.bc.backend.impl;

import org.jboss.da.bc.model.backend.ProjectDetail;
import org.jboss.da.bc.model.backend.ProjectHiearchy;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pnc.api.PNCAuthConnector;
import org.jboss.da.communication.pnc.api.PNCConnectorProvider;
import org.jboss.da.communication.pnc.api.PNCRequestException;
import org.jboss.da.communication.pnc.impl.PNCConnectorProviderImpl;
import org.jboss.da.communication.pnc.model.BuildConfiguration;
import org.jboss.da.model.rest.GAV;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
@RunWith(MockitoJUnitRunner.class)
public class FinalizerImplTest {

    @Mock
    private PNCAuthConnector pnc;

    @Mock
    private PNCConnectorProvider pncProvider;

    @InjectMocks
    private FinalizerImpl finalizer;

    private static int projectCounter = 0;

    private static final ProjectHiearchy ROOT_1 = new ProjectHiearchy(getProject(), true);
    static {
        ROOT_1.setSelected(true);

        ProjectDetail p = ROOT_1.getProject();

    }

    @Before
    public void before() throws PNCRequestException, CommunicationException {
        when(pncProvider.getConnector()).thenReturn(pnc);
        when(pncProvider.getAuthConnector(Matchers.anyString())).thenReturn(pnc);
    }

    /**
     * This test case test option a) of FinalizerImpl.createDeps method: Returns set containing
     * single integer, when the hiearchy object is selected
     */
    @Test
    public void testCreateAllSelected() throws CommunicationException, PNCRequestException {
        when(pnc.getBuildConfiguration(Matchers.anyString())).then(new BCCAnswer());
        ProjectHiearchy root = getHiearchy(true);
        root.setDependencies(new HashSet<>(Arrays.asList(getHiearchy(true), getHiearchy(true))));

        Set<Integer> ret = finalizer.createDeps(root, new HashSet<>());
        Assert.assertEquals(1, ret.size());
    }

    /**
     * This test case test option b) of FinalizerImpl.createDeps method: Returns set containing
     * multiple integers, when the hiearchy object is not selected AND it has selected dependencies
     */
    public void testCreateRootNotSelected() throws CommunicationException, PNCRequestException {
        when(pnc.getBuildConfiguration(Matchers.anyString())).then(new BCCAnswer());
        ProjectHiearchy root = getHiearchy(false);
        root.setDependencies(new HashSet<>(Arrays.asList(getHiearchy(true), getHiearchy(true))));

        Set<Integer> ret = finalizer.createDeps(root, new HashSet<>());
        Assert.assertEquals(2, ret.size());
    }

    /**
     * This test case test option c) of FinalizerImpl.create method: Returns set containing NO
     * integer, when the hiearchy object is not selected AND it has NO selected dependencies
     */
    public void testCreateNothingSelected() throws CommunicationException, PNCRequestException {
        when(pnc.getBuildConfiguration(Matchers.anyString())).then(new BCCAnswer());
        ProjectHiearchy root = getHiearchy(false);
        root.setDependencies(new HashSet<>(Arrays.asList(getHiearchy(true), getHiearchy(true))));

        Set<Integer> ret = finalizer.createDeps(root, new HashSet<>());
        Assert.assertTrue(ret.isEmpty());
    }

    private static ProjectHiearchy getHiearchy(boolean selected) {
        return new ProjectHiearchy(getProject(), selected);
    }

    private static ProjectDetail getProject() {
        projectCounter++;
        ProjectDetail ret = new ProjectDetail(new GAV("org.test", "test-" + projectCounter, "0."
                + projectCounter + ".0"));
        ret.setEnvironmentId(projectCounter);
        ret.setProjectId(projectCounter);
        ret.setName("Project " + projectCounter);
        return ret;
    }

    private static class BCCAnswer implements Answer<Optional<BuildConfiguration>> {

        private int counter;

        @Override
        public Optional<BuildConfiguration> answer(InvocationOnMock invocation) throws Throwable {
            BuildConfiguration ret = new BuildConfiguration();
            ret.setId(counter++);
            return Optional.of(ret);
        }

    }

}
