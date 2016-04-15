package org.jboss.da.test.server.communication;

import static org.junit.Assert.*;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.da.communication.pnc.api.PNCConnector;
import org.jboss.da.communication.pnc.model.BuildConfiguration;
import org.jboss.da.communication.pnc.model.BuildConfigurationCreate;
import org.jboss.da.test.ArquillianDeploymentFactory;
import org.jboss.da.test.ArquillianDeploymentFactory.DepType;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import java.util.List;
import java.util.UUID;

/**
 * Remote tests, which tests communication with PNC
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
@RunWith(Arquillian.class)
public class PncRemoteTest {

    @Inject
    private PNCConnector pncConnector;

    @Deployment
    public static EnterpriseArchive createDeployment() {
        return new ArquillianDeploymentFactory().createDeployment(DepType.BC);
    }

    @Test
    public void testGetBuildConfigurationByScmUrlAndRevision() throws Exception {
        List<BuildConfiguration> obtainedBcs = pncConnector.getBuildConfigurations(
                "https://github.com/project-ncl/pnc.git", "*/v0.2");

        assertEquals(1, obtainedBcs.size());
        BuildConfiguration bc = obtainedBcs.get(0);
        testBcValues(bc);
    }

    @Test
    public void testGetBuildConfigurationByNotExistingScmUrlAndRevision() throws Exception {
        List<BuildConfiguration> obtainedBcs = pncConnector.getBuildConfigurations(
                "https://not.existing.url", "not.existing.tag");

        assertEquals(0, obtainedBcs.size());
    }

    @Test
    public void testCreateRemoveBC() throws Exception {
        String bcName = "BCTestName-" + UUID.randomUUID().toString();
        String scmRepoUrl = "http://test-" + UUID.randomUUID().toString() + ".com";
        int environmentId = 1;
        int projectId = 1;

        BuildConfigurationCreate bc = new BuildConfigurationCreate();
        bc.setName(bcName);
        bc.setEnvironmentId(environmentId);
        bc.setProjectId(projectId);
        bc.setScmRepoURL(scmRepoUrl);
        bc.setProductVersionId(1);

        // Create a BC
        BuildConfiguration obtainedBc = pncConnector.createBuildConfiguration(bc);
        assertNotNull(obtainedBc);
        assertNotNull(obtainedBc.getId());
        assertEquals(bcName, obtainedBc.getName());
        assertEquals(environmentId, obtainedBc.getEnvironment().getId());
        assertEquals(projectId, obtainedBc.getProject().getId());
        assertTrue(testBcWithNameExists(bcName));

        // Delete created BC
        boolean obtainedEcode = pncConnector.deleteBuildConfiguration(obtainedBc);
        assertTrue(obtainedEcode);
        assertFalse(testBcWithNameExists(bcName));

    }

    private boolean testBcWithNameExists(String bcName) throws Exception {
        List<BuildConfiguration> allBcs = pncConnector.getBuildConfigurations();
        return allBcs.parallelStream()
                .map((bc) -> bc.getName().equals(bcName))
                .anyMatch(x -> x);

    }

    private void testBcValues(BuildConfiguration bc) {
        assertFalse(bc.getName().isEmpty());
        assertFalse(bc.getScmRepoURL().isEmpty());
        assertFalse(bc.getScmRevision().isEmpty());
        assertFalse(bc.getBuildScript().isEmpty());
    }
}
