package org.jboss.da.bc.backend.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.jboss.da.bc.backend.api.BcChecker;
import org.jboss.da.communication.pnc.api.PNCConnector;
import org.jboss.da.communication.pnc.model.BuildConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class BcCheckerImplTest {

    @Mock
    private PNCConnector pncConnector;

    @InjectMocks
    @Spy
    private BcCheckerImpl bcChecker;

    private String SCM_URL = "http://test.com";

    private String SCM_REVISION = "tag-test";

    private BuildConfiguration bc1 = createBc(1, "BC1", "mvn clean deploy", SCM_URL, SCM_REVISION);

    private BuildConfiguration bc2 = createBc(2, "BC2", "mvn clean install", SCM_URL, SCM_REVISION);

    @Test
    public void testLookupBcOneResult() throws Exception {
        when(pncConnector.getBuildConfigurations(SCM_URL, SCM_REVISION)).thenReturn(getBcList(bc1));
        Optional<BuildConfiguration> result = bcChecker.lookupBcByScm(SCM_URL, SCM_REVISION);

        assertEquals(bc1, result.get());
    }

    @Test
    public void testLookupBcMoreResults() throws Exception {
        when(pncConnector.getBuildConfigurations(SCM_URL, SCM_REVISION)).thenReturn(
                getBcList(bc2, bc1));
        Optional<BuildConfiguration> result = bcChecker.lookupBcByScm(SCM_URL, SCM_REVISION);

        assertEquals(bc2, result.get());
    }

    @Test
    public void testLookupBcNoResult() throws Exception {
        when(pncConnector.getBuildConfigurations(SCM_URL, SCM_REVISION)).thenReturn(getBcList());
        Optional<BuildConfiguration> result = bcChecker.lookupBcByScm(SCM_URL, SCM_REVISION);

        assertEquals(false, result.isPresent());
    }

    private BuildConfiguration createBc(int id, String name, String buildScript, String scmRepoUrl,
            String scmRevision) {
        BuildConfiguration bc = new BuildConfiguration();
        bc.setId(id);
        bc.setName(name);
        bc.setBuildScript(buildScript);
        bc.setScmRepoURL(scmRepoUrl);
        bc.setScmRevision(scmRevision);
        return bc;
    }

    private ArrayList<BuildConfiguration> getBcList(BuildConfiguration... bcs) {
        ArrayList<BuildConfiguration> bcList = new ArrayList<>();
        for (BuildConfiguration bc : bcs)
            bcList.add(bc);
        return bcList;
    }
}
