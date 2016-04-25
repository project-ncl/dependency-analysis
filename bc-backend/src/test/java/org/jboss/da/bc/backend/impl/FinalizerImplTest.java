package org.jboss.da.bc.backend.impl;

import org.jboss.da.bc.model.backend.ProjectDetail;
import org.jboss.da.bc.model.backend.ProjectHiearchy;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pnc.api.PNCConnector;
import org.jboss.da.communication.pnc.api.PNCRequestException;
import org.jboss.da.communication.pnc.model.BuildConfiguration;
import org.jboss.da.communication.pnc.model.BuildConfigurationCreate;
import org.jboss.da.model.rest.GAV;
import org.junit.Assert;
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
import java.util.Set;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
@RunWith(MockitoJUnitRunner.class)
public class FinalizerImplTest {

    @Mock
    private PNCConnector pnc;

    @InjectMocks
    private FinalizerImpl finalizer;

    private static int projectCounter = 0;

    private static final ProjectHiearchy ROOT_1 = new ProjectHiearchy(getProject(), true);
    static {
        ROOT_1.setSelected(true);

        ProjectDetail p = ROOT_1.getProject();
        p.setCloneRepo(false);

    }

    /**
     * This test case test option a) of FinalizerImpl.create method: Returns set containing single
     * integer, when the hiearchy object is selected
     */
    @Test
    public void testCreateAllSelected() throws CommunicationException, PNCRequestException {
        when(pnc.createBuildConfiguration(Matchers.any(BuildConfigurationCreate.class))).then(
                new BCCAnswer());
        ProjectHiearchy root = getHiearchy(true);
        root.setDependencies(new HashSet<>(Arrays.asList(getHiearchy(true), getHiearchy(true))));

        Set<Integer> ret = finalizer.create(root, new HashSet<>());
        Assert.assertEquals(1, ret.size());
    }

    /**
     * This test case test option b) of FinalizerImpl.create method: Returns set containing multiple
     * integers, when the hiearchy object is not selected AND it has selected dependencies
     */
    public void testCreateRootNotSelected() throws CommunicationException, PNCRequestException {
        when(pnc.createBuildConfiguration(Matchers.any(BuildConfigurationCreate.class))).then(
                new BCCAnswer());
        ProjectHiearchy root = getHiearchy(false);
        root.setDependencies(new HashSet<>(Arrays.asList(getHiearchy(true), getHiearchy(true))));

        Set<Integer> ret = finalizer.create(root, new HashSet<>());
        Assert.assertEquals(2, ret.size());
    }

    /**
     * This test case test option c) of FinalizerImpl.create method: Returns set containing NO
     * integer, when the hiearchy object is not selected AND it has NO selected dependencies
     */
    public void testCreateNothingSelected() throws CommunicationException, PNCRequestException {
        when(pnc.createBuildConfiguration(Matchers.any(BuildConfigurationCreate.class))).then(
                new BCCAnswer());
        ProjectHiearchy root = getHiearchy(false);
        root.setDependencies(new HashSet<>(Arrays.asList(getHiearchy(true), getHiearchy(true))));

        Set<Integer> ret = finalizer.create(root, new HashSet<>());
        Assert.assertTrue(ret.isEmpty());
    }

    private static ProjectHiearchy getHiearchy(boolean selected) {
        return new ProjectHiearchy(getProject(), selected);
    }

    private static ProjectDetail getProject() {
        projectCounter++;
        ProjectDetail ret = new ProjectDetail(new GAV("org.test", "test-" + projectCounter, "0."
                + projectCounter + ".0"));
        ret.setCloneRepo(false);
        ret.setEnvironmentId(projectCounter);
        ret.setProjectId(projectCounter);
        ret.setName("Project " + projectCounter);
        return ret;
    }

    private static class BCCAnswer implements Answer<BuildConfiguration> {

        private int counter;

        @Override
        public BuildConfiguration answer(InvocationOnMock invocation) throws Throwable {
            BuildConfiguration ret = new BuildConfiguration();
            ret.setId(counter++);
            return ret;
        }

    }

}
