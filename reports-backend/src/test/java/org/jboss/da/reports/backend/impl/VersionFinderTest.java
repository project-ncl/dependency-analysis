package org.jboss.da.reports.backend.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import org.jboss.da.common.version.VersionParser;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.aprox.api.AproxConnector;
import org.jboss.da.model.rest.GA;
import org.jboss.da.model.rest.GAV;
import org.jboss.da.reports.api.VersionLookupResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class VersionFinderTest {

    @Mock
    private AproxConnector aproxConnector;

    @Spy
    private final VersionParser osgiParser = new VersionParser();

    @InjectMocks
    @Spy
    private VersionFinderImpl versionFinder;

    private static final String NO_BUILT_VERSION = "1.1.3";

    private static final String NO_BUILT_VERSION_2 = "1.0.20";

    private static final String BUILT_VERSION = "1.1.4";

    private static final String BUILT_VERSION_RH = BUILT_VERSION + "-redhat-20";

    private static final String BUILT_VERSION_2 = "1.1.4.Final";

    private static final String BUILT_VERSION_2_RH = BUILT_VERSION_2 + "-redhat-10";

    private static final String MULTI_BUILT_VERSION = "1.1.5";

    private static final String MULTI_BUILT_VERSION_RH1 = MULTI_BUILT_VERSION + ".redhat-5";

    private static final String MULTI_BUILT_VERSION_RH2 = MULTI_BUILT_VERSION + ".redhat-3";

    private static final String MULTI_BUILT_VERSION_RH_BEST = MULTI_BUILT_VERSION + ".redhat-18";

    private static final String MULTI_BUILT_VERSION_RH4 = MULTI_BUILT_VERSION + ".redhat-16";

    private static final String OTHER_RH_VERSION_1 = "1.0.0.redhat-1";

    private static final String OTHER_RH_VERSION_2 = "1.0.0.redhat-18";

    private static final String OTHER_RH_VERSION_3 = "1.1.1.redhat-15";

    private static final String NON_OSGI_VERSION = "1.3";

    private static final String NON_OSGI_VERSION_RHT = "1.3.redhat-4";

    private static final String NON_OSGI_VERSION_2 = "1.3-Final";

    private static final String NON_OSGI_VERSION_2_RHT = "1.3.0.Final-redhat-7";

    private static final GA REQUESTED_GA = new GA("org.hibernate", "hibernate-core");

    private static final GAV SOME_GAV = new GAV(REQUESTED_GA, "0.0.1");

    private static final GAV NO_BUILT_GAV = new GAV(REQUESTED_GA, NO_BUILT_VERSION);

    private static final GAV BUILT_GAV = new GAV(REQUESTED_GA, BUILT_VERSION);

    private static final GAV BUILT_GAV_2 = new GAV(REQUESTED_GA, BUILT_VERSION_2);

    private static final GAV MULTI_BUILT_GAV = new GAV(REQUESTED_GA, MULTI_BUILT_VERSION);

    private static final GAV NON_OSGI_GAV = new GAV(REQUESTED_GA, NON_OSGI_VERSION);

    private static final GAV NON_OSGI_GAV_2 = new GAV(REQUESTED_GA, NON_OSGI_VERSION_2);

    private static final List<String> All_VERSIONS = Arrays.asList(OTHER_RH_VERSION_1,
            OTHER_RH_VERSION_2, NO_BUILT_VERSION_2, NO_BUILT_VERSION, OTHER_RH_VERSION_3,
            MULTI_BUILT_VERSION_RH2, BUILT_VERSION_RH, MULTI_BUILT_VERSION_RH1, BUILT_VERSION_2_RH,
            MULTI_BUILT_VERSION_RH_BEST, MULTI_BUILT_VERSION_RH4, NON_OSGI_VERSION,
            NON_OSGI_VERSION_RHT, BUILT_VERSION_2, NON_OSGI_VERSION_2, NON_OSGI_VERSION_2_RHT);

    private static final List<String> BUILT_VERSIONS = Arrays.asList(OTHER_RH_VERSION_1,
            OTHER_RH_VERSION_2, OTHER_RH_VERSION_3, MULTI_BUILT_VERSION_RH2, BUILT_VERSION_RH,
            MULTI_BUILT_VERSION_RH1, BUILT_VERSION_2_RH, MULTI_BUILT_VERSION_RH_BEST,
            MULTI_BUILT_VERSION_RH4, NON_OSGI_VERSION_RHT, NON_OSGI_VERSION_2_RHT);

    private void prepare(List<String> versions) throws CommunicationException {
        when(aproxConnector.getVersionsOfGA(REQUESTED_GA)).thenReturn(versions);
    }

    @Test
    public void lookupBuiltVersionsNonExistingGAVTest() throws CommunicationException {
        prepare(Collections.EMPTY_LIST);
        VersionLookupResult lookupResult = versionFinder.lookupBuiltVersions(SOME_GAV);
        assertTrue(lookupResult.getAvailableVersions().isEmpty());
        assertFalse(lookupResult.getBestMatchVersion().isPresent());
    }

    @Test
    public void lookupBuiltVersionsBuiltGAVTest() throws CommunicationException {
        prepare(All_VERSIONS);
        VersionLookupResult lookupResult = versionFinder.lookupBuiltVersions(BUILT_GAV);
        assertNotNull(lookupResult);
        assertEquals(BUILT_VERSION_RH, lookupResult.getBestMatchVersion().get());
        assertEquals(BUILT_VERSIONS.size(), lookupResult.getAvailableVersions().size());
        assertTrue(lookupResult.getAvailableVersions().containsAll(BUILT_VERSIONS));
    }

    @Test
    public void testVersionsForNonExistingGAV() throws CommunicationException {
        prepare(Collections.EMPTY_LIST);
        List<String> versions = versionFinder.getBuiltVersionsFor(SOME_GAV);
        assertTrue(versions.isEmpty());
    }

    @Test
    public void testVersionsForGAV() throws CommunicationException {
        prepare(All_VERSIONS);
        List<String> versions = versionFinder.getBuiltVersionsFor(SOME_GAV);
        assertNotNull(versions);
        assertEquals(BUILT_VERSIONS.size(), versions.size());
        assertTrue(versions.containsAll(BUILT_VERSIONS));
        assertFalse(versions.contains(NO_BUILT_VERSION));
        assertFalse(versions.contains(NO_BUILT_VERSION_2));
        assertFalse(versions.contains(BUILT_VERSION));
        assertFalse(versions.contains(MULTI_BUILT_VERSION));
        assertFalse(versions.contains(NON_OSGI_VERSION));
    }

    @Test
    public void getBestMatchVersionForNonExistingGAV() throws CommunicationException {
        prepare(Collections.EMPTY_LIST);
        Optional<String> bmv = versionFinder.getBestMatchVersionFor(SOME_GAV);
        assertFalse(bmv.isPresent());
    }

    @Test
    public void getBestMatchVersionForNotBuiltGAV() throws CommunicationException {
        prepare(All_VERSIONS);
        Optional<String> bmv = versionFinder.getBestMatchVersionFor(NO_BUILT_GAV);
        assertFalse(bmv.isPresent());
    }

    @Test
    public void getBestMatchVersionForBuiltGAV() throws CommunicationException {
        String bmv;
        prepare(All_VERSIONS);

        bmv = versionFinder.getBestMatchVersionFor(BUILT_GAV).get();
        assertNotNull(bmv);
        assertEquals(BUILT_VERSION_RH, bmv);

        bmv = versionFinder.getBestMatchVersionFor(BUILT_GAV_2).get();
        assertNotNull(bmv);
        assertEquals(BUILT_VERSION_2_RH, bmv);
    }

    @Test
    public void getBestMatchVersionForMultipleBuiltGAV() throws CommunicationException {
        prepare(All_VERSIONS);
        String bmv = versionFinder.getBestMatchVersionFor(MULTI_BUILT_GAV).get();
        assertNotNull(bmv);
        assertEquals(MULTI_BUILT_VERSION_RH_BEST, bmv);
    }

    @Test
    public void getBestMatchVersionForNoOSGIGAV() throws CommunicationException {
        String bmv;
        prepare(All_VERSIONS);

        bmv = versionFinder.getBestMatchVersionFor(NON_OSGI_GAV).get();
        assertNotNull(bmv);
        assertEquals(NON_OSGI_VERSION_RHT, bmv);

        bmv = versionFinder.getBestMatchVersionFor(NON_OSGI_GAV_2).get();
        assertNotNull(bmv);
        assertEquals(NON_OSGI_VERSION_2_RHT, bmv);
    }

}
