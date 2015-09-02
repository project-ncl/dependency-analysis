package org.jboss.da.reports.impl;

import org.jboss.da.communication.CommunicationException;
import org.jboss.da.communication.aprox.api.AproxConnector;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;
import org.jboss.da.communication.model.GAV;
import org.jboss.da.listings.api.service.BlackArtifactService;
import org.jboss.da.listings.api.service.WhiteArtifactService;
import org.jboss.da.reports.api.ArtifactReport;
import org.jboss.da.reports.backend.api.VersionFinder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
@RunWith(MockitoJUnitRunner.class)
public class ReportsGeneratorImplTest {

    @Mock
    private AproxConnector aproxClient;

    @Mock
    private VersionFinder versionFinderImpl;

    @Mock
    private BlackArtifactService blackArtifactService;

    @Mock
    private WhiteArtifactService whiteArtifactService;

    @InjectMocks
    private ReportsGeneratorImpl generator;

    private final GAV daGAV = new GAV("org.jboss", "dependency-analysis", "1.0.1");

    private final String version = "0.1.0";

    private final GAV daCoreGAV = new GAV("org.jboss.da", "core", version);

    private final List<String> daCoreVersionsNoBest = Arrays.asList("1.1.1.redhat-2",
            "1.2.3.redhat-1", "1.3.4.redhat-3", "1.3.5.redhat-1");

    private final String bestMatchVersion = version + ".redhat-1";

    private final List<String> daCoreVersionsBest = Arrays.asList("1.1.1.redhat-2",
            "1.2.3.redhat-1", "1.3.4.redhat-3", "1.3.5.redhat-1", bestMatchVersion);

    private final GAVDependencyTree daCoreNoDT = new GAVDependencyTree(daCoreGAV, new HashSet<>());

    private final GAV daUtilGAV = new GAV("org.jboss.da", "util", version);

    private final GAV daCommonGAV = new GAV("org.jboss.da", "common", version);

    private final GAVDependencyTree daUtilDT = new GAVDependencyTree(daUtilGAV, new HashSet<>());

    private final GAVDependencyTree daCommonDT = new GAVDependencyTree(daCommonGAV, new HashSet<>());

    private final GAVDependencyTree daCoreDT = new GAVDependencyTree(daCoreGAV, new HashSet<>(
            Arrays.asList(daUtilDT, daCommonDT)));

    private static final String NO_BEST_MATCH_VERSION = null;

    private void prepare(boolean whitelisted, boolean blacklisted, List<String> versions,
            String best, GAVDependencyTree dependencyTree) throws CommunicationException {
        when(versionFinderImpl.getBuiltVersionsFor(daCoreGAV)).thenReturn(versions);
        when(versionFinderImpl.getBestMatchVersionFor(daCoreGAV)).thenReturn(best);
        when(versionFinderImpl.getBestMatchVersionFor(daCoreGAV, versions)).thenReturn(best);
        when(blackArtifactService.isArtifactPresent(daCoreGAV)).thenReturn(blacklisted);
        when(whiteArtifactService.isArtifactPresent(daCoreGAV)).thenReturn(whitelisted);
        when(aproxClient.getDependencyTreeOfGAV(daCoreGAV)).thenReturn(dependencyTree);
    }

    private void prepareMulti() throws CommunicationException {
        prepare(false, false, daCoreVersionsBest, bestMatchVersion, daCoreNoDT);
        when(aproxClient.getDependencyTreeOfGAV(daCoreGAV)).thenReturn(daCoreDT);

        when(versionFinderImpl.getBuiltVersionsFor(daUtilGAV)).thenReturn(daCoreVersionsBest);
        when(versionFinderImpl.getBestMatchVersionFor(daUtilGAV)).thenReturn(bestMatchVersion);
        when(versionFinderImpl.getBestMatchVersionFor(daUtilGAV, daCoreVersionsBest)).thenReturn(
                bestMatchVersion);
        when(blackArtifactService.isArtifactPresent(daUtilGAV)).thenReturn(false);
        when(whiteArtifactService.isArtifactPresent(daUtilGAV)).thenReturn(false);

        when(versionFinderImpl.getBuiltVersionsFor(daCommonGAV)).thenReturn(daCoreVersionsNoBest);
        when(versionFinderImpl.getBestMatchVersionFor(daCommonGAV)).thenReturn(null);
        when(versionFinderImpl.getBestMatchVersionFor(daCommonGAV, daCoreVersionsNoBest))
                .thenReturn(null);
        when(blackArtifactService.isArtifactPresent(daCommonGAV)).thenReturn(false);
        when(whiteArtifactService.isArtifactPresent(daCommonGAV)).thenReturn(false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullGAV() throws CommunicationException {
        generator.getReport(null);
    }

    @Test
    public void testNonExistingGAV() throws CommunicationException {
        when(versionFinderImpl.getBuiltVersionsFor(daGAV)).thenReturn(null);

        ArtifactReport report = generator.getReport(daGAV);

        assertNull(report);
    }

    @Test
    public void testNonListedNoBestMatchGAV() throws CommunicationException {
        prepare(false, false, daCoreVersionsNoBest, null, daCoreNoDT);

        ArtifactReport report = generator.getReport(daCoreGAV);

        assertTrue(report.getAvailableVersions().containsAll(daCoreVersionsNoBest));
        assertEquals(daCoreGAV, report.getGav());
        assertEquals(NO_BEST_MATCH_VERSION, report.getBestMatchVersion());
        assertTrue(report.getDependencies().isEmpty());
        assertFalse(report.isBlacklisted());
        assertFalse(report.isWhiteListed());

    }

    @Test
    public void testWhiteListedNoBestMatchGAV() throws CommunicationException {
        prepare(true, false, daCoreVersionsNoBest, null, daCoreNoDT);

        ArtifactReport report = generator.getReport(daCoreGAV);

        assertTrue(report.getAvailableVersions().containsAll(daCoreVersionsNoBest));
        assertEquals(daCoreGAV, report.getGav());
        assertEquals(NO_BEST_MATCH_VERSION, report.getBestMatchVersion());
        assertTrue(report.getDependencies().isEmpty());
        assertFalse(report.isBlacklisted());
        assertTrue(report.isWhiteListed());
    }

    @Test
    public void testBlackListedBestMatchGAV() throws CommunicationException {
        prepare(false, true, daCoreVersionsBest, bestMatchVersion, daCoreNoDT);

        ArtifactReport report = generator.getReport(daCoreGAV);

        assertTrue(report.getAvailableVersions().containsAll(daCoreVersionsNoBest));
        assertEquals(daCoreGAV, report.getGav());
        assertEquals(bestMatchVersion, report.getBestMatchVersion());
        assertTrue(report.getDependencies().isEmpty());
        assertTrue(report.isBlacklisted());
        assertFalse(report.isWhiteListed());
    }

    @Test
    public void testArtifactReportShouldNotHaveNullValuesInAvailableVersionsWhenBestMatchVersionIsNull()
            throws CommunicationException {
        prepare(false, false, daCoreVersionsBest, null, daCoreNoDT);

        ArtifactReport report = generator.getReport(daCoreGAV);

        assertNull(report.getBestMatchVersion());
        assertFalse(report.getAvailableVersions().stream().anyMatch(version -> version == null));
    }

    @Test
    public void testGetMultipleReport() throws CommunicationException {
        prepareMulti();

        ArtifactReport report = generator.getReport(daCoreGAV);

        assertTrue(report.getAvailableVersions().containsAll(daCoreVersionsNoBest));
        assertEquals(daCoreGAV, report.getGav());
        assertEquals(bestMatchVersion, report.getBestMatchVersion());
        assertFalse(report.isBlacklisted());
        assertFalse(report.isWhiteListed());
        assertMultipleDependencies(report.getDependencies());
    }

    private void assertMultipleDependencies(Set<ArtifactReport> deps) {
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
