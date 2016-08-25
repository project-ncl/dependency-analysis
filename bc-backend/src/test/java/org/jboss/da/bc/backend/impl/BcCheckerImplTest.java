package org.jboss.da.bc.backend.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.jboss.da.communication.pnc.api.PNCConnector;
import org.jboss.da.communication.pnc.model.BuildConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BcCheckerImplTest {

    @Mock
    private PNCConnector pncConnector;

    @InjectMocks
    @Spy
    private BcCheckerImpl bcChecker;

    private static final String SCM_URL = "http://test.com";

    private static final String SCM_REVISION = "tag-test";

    private static final BuildConfiguration BC1 = createBc(1, "BC1", "mvn clean deploy", SCM_URL,
            SCM_REVISION);

    private static final BuildConfiguration BC2 = createBc(2, "BC2", "mvn clean install", SCM_URL,
            SCM_REVISION);

    @Test
    public void testLookupBcOneResult() throws Exception {
        when(pncConnector.getBuildConfigurations(SCM_URL, SCM_REVISION)).thenReturn(getBcList(BC1));

        List<BuildConfiguration> results = bcChecker.lookupBcByScm(SCM_URL, SCM_REVISION);
        List<Integer> resultIds = bcChecker.lookupBcIdsByScm(SCM_URL, SCM_REVISION);

        assertEquals(BC1, results.get(0));
        assertEquals((Integer) BC1.getId(), resultIds.get(0));
    }

    @Test
    public void testLookupBcMoreResults() throws Exception {
        when(pncConnector.getBuildConfigurations(SCM_URL, SCM_REVISION)).thenReturn(
                getBcList(BC2, BC1));
        List<BuildConfiguration> results = bcChecker.lookupBcByScm(SCM_URL, SCM_REVISION);
        List<Integer> resultIds = bcChecker.lookupBcIdsByScm(SCM_URL, SCM_REVISION);

        assertTrue(results.contains(BC2));
        assertTrue(results.contains(BC1));

        assertTrue(resultIds.contains(BC1.getId()));
        assertTrue(resultIds.contains(BC2.getId()));
    }

    @Test
    public void testLookupBcNoResult() throws Exception {
        when(pncConnector.getBuildConfigurations(SCM_URL, SCM_REVISION)).thenReturn(getBcList());
        List<BuildConfiguration> results = bcChecker.lookupBcByScm(SCM_URL, SCM_REVISION);
        List<Integer> resultIds = bcChecker.lookupBcIdsByScm(SCM_URL, SCM_REVISION);

        assertTrue(results.isEmpty());
        assertTrue(resultIds.isEmpty());
    }

    private static BuildConfiguration createBc(int id, String name, String buildScript,
            String scmRepoUrl, String scmRevision) {
        BuildConfiguration bc = new BuildConfiguration();
        bc.setId(id);
        bc.setName(name);
        bc.setBuildScript(buildScript);
        bc.setSCMLocation(scmRepoUrl, scmRevision);
        return bc;
    }

    private ArrayList<BuildConfiguration> getBcList(BuildConfiguration... bcs) {
        ArrayList<BuildConfiguration> bcList = new ArrayList<>();
        for (BuildConfiguration bc : bcs)
            bcList.add(bc);
        return bcList;
    }
}
