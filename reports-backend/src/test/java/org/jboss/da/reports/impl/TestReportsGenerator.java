package org.jboss.da.reports.impl;

import org.jboss.da.communication.CommunicationException;
import org.jboss.da.communication.aprox.api.AproxConnector;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;
import org.jboss.da.communication.model.GAV;
import org.jboss.da.listings.api.service.BlackArtifactService;
import org.jboss.da.listings.api.service.WhiteArtifactService;
import org.jboss.da.reports.api.ArtifactReport;
import org.jboss.da.reports.backend.api.VersionFinder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
@RunWith(MockitoJUnitRunner.class)
public class TestReportsGenerator {

    @Mock
    private AproxConnector aproxClient;

    @Mock
    private VersionFinder versionFinderImpl;

    @Mock
    private BlackArtifactService blackArtifactService;

    @Mock
    private WhiteArtifactService whiteArtifactService;

    @InjectMocks
    @Spy
    ReportsGeneratorImpl generator;

    private static final GAV daGAV = new GAV("org.jboss", "dependency-analysis", "1.0.1");

    private static final String version = "0.1.0";

    private static final GAV daCoreGAV = new GAV("org.jboss.da", "core", version);

    private static final List<String> daCoreVersionsNoBest = Arrays.asList("1.1.1.redhat-2",
            "1.2.3.redhat-1", "1.3.4.redhat-3", "1.3.5.redhat-1");

    private static final String bestMatchVersion = version + ".redhat-1";

    private static final List<String> daCoreVersionsBest = Arrays.asList("1.1.1.redhat-2",
            "1.2.3.redhat-1", "1.3.4.redhat-3", "1.3.5.redhat-1", bestMatchVersion);

    private static final GAVDependencyTree daCoreNoDT = new GAVDependencyTree(daCoreGAV,
            new HashSet<>());

    private static final GAV daUtilGAV = new GAV("org.jboss.da", "util", version);

    private static final GAV daCommonGAV = new GAV("org.jboss.da", "common", version);

    private static final GAVDependencyTree daUtilDT = new GAVDependencyTree(daUtilGAV,
            new HashSet<>());

    private static final GAVDependencyTree daCommonDT = new GAVDependencyTree(daCommonGAV,
            new HashSet<>());

    private static final GAVDependencyTree daCoreDT = new GAVDependencyTree(daCoreGAV,
            new HashSet<>(Arrays.asList(daUtilDT, daCommonDT)));

    private void prepare(boolean whitelisted, boolean blacklisted, List<String> versions,
            String best, GAVDependencyTree dependencyTree) throws CommunicationException {
        when(versionFinderImpl.getVersionsFor(daCoreGAV)).thenReturn(versions);
        when(versionFinderImpl.getBestMatchVersionFor(daCoreGAV)).thenReturn(best);
        when(versionFinderImpl.getBestMatchVersionFor(daCoreGAV, versions)).thenReturn(best);
        when(blackArtifactService.isArtifactPresent(daCoreGAV)).thenReturn(blacklisted);
        when(whiteArtifactService.isArtifactPresent(daCoreGAV)).thenReturn(whitelisted);
        when(aproxClient.getDependencyTreeOfGAV(daCoreGAV)).thenReturn(daCoreNoDT);
    }

    private void prepareMulti() throws CommunicationException {
        prepare(false, false, daCoreVersionsBest, bestMatchVersion, daCoreNoDT);
        when(aproxClient.getDependencyTreeOfGAV(daCoreGAV)).thenReturn(daCoreDT);

        when(versionFinderImpl.getVersionsFor(daUtilGAV)).thenReturn(daCoreVersionsBest);
        when(versionFinderImpl.getBestMatchVersionFor(daUtilGAV)).thenReturn(bestMatchVersion);
        when(versionFinderImpl.getBestMatchVersionFor(daUtilGAV, daCoreVersionsBest)).thenReturn(
                bestMatchVersion);
        when(blackArtifactService.isArtifactPresent(daUtilGAV)).thenReturn(false);
        when(whiteArtifactService.isArtifactPresent(daUtilGAV)).thenReturn(false);

        when(versionFinderImpl.getVersionsFor(daCommonGAV)).thenReturn(daCoreVersionsNoBest);
        when(versionFinderImpl.getBestMatchVersionFor(daCommonGAV)).thenReturn(null);
        when(versionFinderImpl.getBestMatchVersionFor(daCommonGAV, daCoreVersionsNoBest))
                .thenReturn(null);
        when(blackArtifactService.isArtifactPresent(daCommonGAV)).thenReturn(false);
        when(whiteArtifactService.isArtifactPresent(daCommonGAV)).thenReturn(false);
    }

    @Test
    public void testNullGAV() throws CommunicationException {
        try {
            generator.getReport(null);
            fail();
        } catch (IllegalArgumentException ex) {
            // ok
        }
    }

    @Test
    public void testNonExistingGAV() throws CommunicationException {
        when(versionFinderImpl.getVersionsFor(daGAV)).thenReturn(null);
        ArtifactReport report = generator.getReport(daGAV);
        assertNull(report);
    }

    @Test
    public void testNonListedNoBestMatch() throws CommunicationException {
        // non-listed, no-best-match GAV
        prepare(false, false, daCoreVersionsNoBest, null, daCoreNoDT);
        ArtifactReport report = generator.getReport(daCoreGAV);
        assertNotNull(report);
        assertTrue(report.getAvailableVersions().containsAll(daCoreVersionsNoBest));
        assertEquals(daCoreGAV, report.getGav());
        assertNull(report.getBestMatchVersion());
        assertTrue(report.getDependencies().isEmpty());
        assertFalse(report.isBlacklisted());
        assertFalse(report.isWhiteListed());

    }

    @Test
    public void testWhiteListedNoBestMatch() throws CommunicationException {
        // white-listed, no-best-match GAV
        prepare(true, false, daCoreVersionsNoBest, null, daCoreNoDT);
        ArtifactReport report = generator.getReport(daCoreGAV);
        assertNotNull(report);
        assertTrue(report.getAvailableVersions().containsAll(daCoreVersionsNoBest));
        assertEquals(daCoreGAV, report.getGav());
        assertNull(report.getBestMatchVersion());
        assertTrue(report.getDependencies().isEmpty());
        assertFalse(report.isBlacklisted());
        assertTrue(report.isWhiteListed());
    }

    @Test
    public void testBlackListedBestMatch() throws CommunicationException {
        // black-listed, best-match GAV
        prepare(false, true, daCoreVersionsBest, bestMatchVersion, daCoreNoDT);
        ArtifactReport report = generator.getReport(daCoreGAV);
        assertNotNull(report);
        assertTrue(report.getAvailableVersions().containsAll(daCoreVersionsNoBest));
        assertEquals(daCoreGAV, report.getGav());
        assertNotNull(report.getBestMatchVersion());
        assertEquals(bestMatchVersion, report.getBestMatchVersion());
        assertTrue(report.getDependencies().isEmpty());
        assertTrue(report.isBlacklisted());
        assertFalse(report.isWhiteListed());
    }

    @Test
    @Ignore
    // TODO: Remove this ignore when AProxCommunicator supports dependencies
    public void testGetMultipleReport() throws CommunicationException {
        prepareMulti();
        ArtifactReport report = generator.getReport(daCoreGAV);
        assertNotNull(report);
        assertTrue(report.getAvailableVersions().containsAll(daCoreVersionsNoBest));
        assertEquals(daCoreGAV, report.getGav());
        assertNotNull(report.getBestMatchVersion());
        assertEquals(bestMatchVersion, report.getBestMatchVersion());
        assertFalse(report.getDependencies().isEmpty());
        assertFalse(report.isBlacklisted());
        assertFalse(report.isWhiteListed());

        Set<ArtifactReport> deps = report.getDependencies();
        assertEquals(2, deps.size());

        for (ArtifactReport dep : deps) {
            GAV gav = dep.getGav();
            switch (gav.getArtifactId()) {
                case "util": {
                    assertTrue(dep.getAvailableVersions().containsAll(daCoreVersionsBest));
                    assertEquals(daUtilGAV, dep.getGav());
                    assertNotNull(dep.getBestMatchVersion());
                    assertEquals(bestMatchVersion, dep.getBestMatchVersion());
                    assertTrue(dep.getDependencies().isEmpty());
                    assertFalse(dep.isBlacklisted());
                    assertFalse(dep.isWhiteListed());
                    break;
                }
                case "common": {
                    assertTrue(dep.getAvailableVersions().containsAll(daCoreVersionsNoBest));
                    assertEquals(daCommonGAV, dep.getGav());
                    assertNull(dep.getBestMatchVersion());
                    assertTrue(dep.getDependencies().isEmpty());
                    assertFalse(dep.isBlacklisted());
                    assertFalse(dep.isWhiteListed());
                    break;
                }
                default: {
                    fail("Unknown artifact id");
                    break;
                }
            }
        }
    }
}
