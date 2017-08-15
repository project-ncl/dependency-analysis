package org.jboss.da.test.server.communication;

import org.jboss.da.test.server.AbstractServerTest;
import static org.junit.Assert.*;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.da.communication.pnc.api.PNCConnectorProvider;
import org.jboss.da.communication.pnc.model.BuildConfiguration;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import java.util.List;

/**
 * Remote tests, which tests communication with PNC
 * 
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
@RunWith(Arquillian.class)
public class PncRemoteTest extends AbstractServerTest {

    @Inject
    private PNCConnectorProvider pncConnector;

    @Test
    @Ignore
    public void testGetBuildConfigurationByScmUrlAndRevision() throws Exception {
        List<BuildConfiguration> obtainedBcs = pncConnector.getConnector().getBuildConfigurations(
                "https://github.com/project-ncl/pnc.git", "*/v0.2");

        assertEquals(1, obtainedBcs.size());
        BuildConfiguration bc = obtainedBcs.get(0);
        testBcValues(bc);
    }

    @Test
    public void testGetBuildConfigurationByNotExistingScmUrlAndRevision() throws Exception {
        List<BuildConfiguration> obtainedBcs = pncConnector.getConnector().getBuildConfigurations(
                "https://not.existing.url", "not.existing.tag");

        assertEquals(0, obtainedBcs.size());
    }

    private void testBcValues(BuildConfiguration bc) {
        assertFalse(bc.getName().isEmpty());
        assertNotNull(bc.getRepositoryConfiguration());
        assertFalse(bc.getScmRevision().isEmpty());
        assertFalse(bc.getBuildScript().isEmpty());
    }
}
