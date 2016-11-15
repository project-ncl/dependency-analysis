package org.jboss.da.bc.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jboss.da.bc.backend.api.POMInfoGenerator;
import org.jboss.da.bc.model.backend.ProjectDetail;
import org.jboss.da.bc.model.backend.ProjectHiearchy;
import org.jboss.da.communication.scm.api.SCMConnector;
import org.jboss.da.model.rest.GAV;
import org.jboss.da.reports.api.VersionLookupResult;
import org.jboss.da.reports.backend.api.VersionFinder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProjectHiearchyCreatorTest {

    private ProjectHiearchy topLevelHierarchy;

    @Mock
    private VersionFinder versionFinder;

    @Mock
    private POMInfoGenerator pomInfoGenerator;

    @Mock
    private SCMConnector scmConnector;

    @Spy
    @InjectMocks
    private ProjectHiearchyCreator projectHierarchyCreator;

    private Map<GAV, List<String>> mapExpectedDepsToGavs;

    /**
     * Test summary:
     * 
     * Use a mock version of VersionFinder to provide the available version lookup.
     * Ensure available versions for direct dependencies are present in the response. 
     */
    @Test
    public void testAvailableVersionsPresent() {
        Set<ProjectHiearchy> deps = projectHierarchyCreator.processDependencies(topLevelHierarchy,
                mapExpectedDepsToGavs.keySet());
        for (ProjectHiearchy ph : deps) {
            ProjectDetail pd = ph.getProject();
            GAV g = pd.getGav();
            assertEquals(mapExpectedDepsToGavs.get(g), pd.getAvailableVersions());
        }
    }

    // TEST SETUP BELOW

    @Before
    public void init() throws Exception {
        topLevelHierarchy = new ProjectHiearchy(new ProjectDetail(new GAV("org.jboss.da.bc.impl",
                "unittest", "1.0")), true);
        topLevelHierarchy.getProject().setExternalSCM("goo", "bar");

        initGavs();
        initMocks();
    }

    private void initMocks() throws Exception {

        for (GAV gav : mapExpectedDepsToGavs.keySet()) {

            // Mock VersionFinder: provide available versions for GAVs
            when(versionFinder.lookupBuiltVersions(gav)).thenReturn(
                    new VersionLookupResult(Optional.empty(), mapExpectedDepsToGavs.get(gav)));

            // Mock POMInfoGenerator: return empties as these are not critical
            when(pomInfoGenerator.getPomInfo(gav)).thenReturn(Optional.empty());

            when(
                    pomInfoGenerator.getPomInfo(Mockito.anyString(), Mockito.anyString(),
                            Mockito.any(GAV.class))).thenReturn(Optional.empty());
        }

        when(
                scmConnector.isGAVInRepository(Mockito.anyString(), Mockito.anyString(),
                        Mockito.any(GAV.class))).thenReturn(false);
    }

    @SuppressWarnings("serial")
    private void initGavs() {
        mapExpectedDepsToGavs = new HashMap<GAV, List<String>>();
        mapExpectedDepsToGavs.put(new GAV("org.jboss.da.bc.impl", "test-artifact-one", "1.5"),
                new ArrayList<String>() {

                    {
                        add("1.2.1.redhat-2");
                        add("1.1.2-redhat-1");
                    }
                });
        mapExpectedDepsToGavs.put(new GAV("org.jboss.da.bc.impl", "test-artifact-two", "2.2"),
                new ArrayList<String>() {

                    {
                        add("2.2.2-redhat-3");
                        add("2.1.1.redhat-1");
                    }
                });
        mapExpectedDepsToGavs.put(new GAV("org.jboss.da.bc.impl", "test-artifact-a", "3.7"),
                new ArrayList<String>() {

                    {
                        add("3.7.1-redhat-2");
                        add("3.5.1.redhat-1");
                        add("3.1.1-redhat-2");
                    }
                });
        mapExpectedDepsToGavs.put(new GAV("org.jboss.da.bc.impl", "test-artifact-b", "2.7"),
                new ArrayList<String>());
    }

}
