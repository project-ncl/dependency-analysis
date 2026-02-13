package org.jboss.da.common.version;

import org.jboss.da.common.CommunicationException;
import org.jboss.pnc.api.dependencyanalyzer.dto.QualifiedVersion;
import org.jboss.pnc.api.enums.Qualifier;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * 
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
public class VersionAnalyzerTest {

    private VersionAnalyzer versionFinder = new VersionAnalyzer(Collections.singletonList("redhat"));

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

    private static final String NON_OSGI_VERSION_3 = "1.2.3.foo.bar.baz";

    private static final String NON_OSGI_VERSION_3_RHT = "1.2.3.foo-bar-baz-redhat-5";

    private static final String NON_OSGI_VERSION_4 = "1.5.9.foo,bar,baz";

    private static final String NON_OSGI_VERSION_4_RHT = "1.5.9.foo-bar-baz-redhat-8";

    private static final List<String> All_VERSIONS = Arrays.asList(
            OTHER_RH_VERSION_1,
            OTHER_RH_VERSION_2,
            NO_BUILT_VERSION_2,
            NO_BUILT_VERSION,
            OTHER_RH_VERSION_3,
            MULTI_BUILT_VERSION_RH2,
            BUILT_VERSION_RH,
            MULTI_BUILT_VERSION_RH1,
            BUILT_VERSION_2_RH,
            MULTI_BUILT_VERSION_RH_BEST,
            MULTI_BUILT_VERSION_RH4,
            NON_OSGI_VERSION,
            NON_OSGI_VERSION_RHT,
            BUILT_VERSION_2,
            NON_OSGI_VERSION_2,
            NON_OSGI_VERSION_2_RHT,
            NON_OSGI_VERSION_3,
            NON_OSGI_VERSION_3_RHT,
            NON_OSGI_VERSION_4,
            NON_OSGI_VERSION_4_RHT);

    private static final List<String> BUILT_VERSIONS = Arrays.asList(
            OTHER_RH_VERSION_1,
            OTHER_RH_VERSION_2,
            OTHER_RH_VERSION_3,
            MULTI_BUILT_VERSION_RH2,
            BUILT_VERSION_RH,
            MULTI_BUILT_VERSION_RH1,
            BUILT_VERSION_2_RH,
            MULTI_BUILT_VERSION_RH_BEST,
            MULTI_BUILT_VERSION_RH4,
            NON_OSGI_VERSION_RHT,
            NON_OSGI_VERSION_2_RHT,
            NON_OSGI_VERSION_3_RHT,
            NON_OSGI_VERSION_4_RHT);

    @Test
    public void getBestMatchVersionForNonExistingGAV() throws CommunicationException {
        Optional<String> bmv = versionFinder.findBiggestMatchingVersion("0.0.1", Collections.EMPTY_LIST);
        assertFalse("Best match version expected to not be present", bmv.isPresent());
    }

    @Test
    public void getBestMatchVersionForNotBuiltGAV() throws CommunicationException {
        Optional<String> bmv = versionFinder.findBiggestMatchingVersion(
                NO_BUILT_VERSION,
                All_VERSIONS.stream().map(QualifiedVersion::new).collect(Collectors.toList()));
        assertFalse("Best match version expected to not be present", bmv.isPresent());
    }

    @Test
    public void getBestMatchVersionForBuiltGAV() throws CommunicationException {
        checkBMV(BUILT_VERSION_RH, BUILT_VERSION, All_VERSIONS.toArray(new String[All_VERSIONS.size()]));
        checkBMV(BUILT_VERSION_2_RH, BUILT_VERSION_2, All_VERSIONS.toArray(new String[All_VERSIONS.size()]));
    }

    @Test
    public void getBestMatchVersionForMultipleBuiltGAV() throws CommunicationException {
        checkBMV(
                MULTI_BUILT_VERSION_RH_BEST,
                MULTI_BUILT_VERSION,
                All_VERSIONS.toArray(new String[All_VERSIONS.size()]));
    }

    @Test
    public void getBestMatchVersionForNoOSGIGAV() throws CommunicationException {
        checkBMV(NON_OSGI_VERSION_RHT, NON_OSGI_VERSION, All_VERSIONS.toArray(new String[All_VERSIONS.size()]));
        checkBMV(NON_OSGI_VERSION_2_RHT, NON_OSGI_VERSION_2, All_VERSIONS.toArray(new String[All_VERSIONS.size()]));
        checkBMV(NON_OSGI_VERSION_3_RHT, NON_OSGI_VERSION_3, All_VERSIONS.toArray(new String[All_VERSIONS.size()]));
        checkBMV(NON_OSGI_VERSION_4_RHT, NON_OSGI_VERSION_4, All_VERSIONS.toArray(new String[All_VERSIONS.size()]));
    }

    @Test
    public void NCL2931ReproducerTest() {
        String[] avaliableVersions = {
                "1.4.0.redhat-4",
                "1.4.redhat-3",
                "1.4-redhat-2",
                "1.4-redhat-1",
                "1.6.0.redhat-5",
                "1.6.0.redhat-4",
                "1.6.0.redhat-3",
                "1.6.redhat-2",
                "1.6.redhat-1",
                "1.9.0.redhat-1",
                "1.10.0.redhat-5",
                "1.10.0.redhat-4",
                "1.10.0.redhat-3",
                "1.10.0.redhat-2",
                "1.10.0.redhat-1" };
        checkBMV("1.4.0.redhat-4", "1.4", avaliableVersions);
    }

    @Test
    public void ambiguousNonOSGIVersionsTest() {
        String[] avaliableVersionsWithOSGI = { "1.0.0.redhat-1", "1.0.redhat-1", "1.redhat-1" };
        checkBMV("1.0.0.redhat-1", "1.0.0", avaliableVersionsWithOSGI);
        checkBMV("1.0.0.redhat-1", "1.0", avaliableVersionsWithOSGI);
        checkBMV("1.0.0.redhat-1", "1", avaliableVersionsWithOSGI);

        String[] avaliableVersionsWithOSGIRev = { "1.redhat-1", "1.0.redhat-1", "1.0.0.redhat-1" };
        checkBMV("1.0.0.redhat-1", "1.0.0", avaliableVersionsWithOSGIRev);
        checkBMV("1.0.0.redhat-1", "1.0", avaliableVersionsWithOSGIRev);
        checkBMV("1.0.0.redhat-1", "1", avaliableVersionsWithOSGIRev);

        String[] avaliableVersionsWithoutOSGI = { "1.0.redhat-1", "1.redhat-1" };
        checkBMV("1.0.redhat-1", "1.0.0", avaliableVersionsWithoutOSGI);
        checkBMV("1.0.redhat-1", "1.0", avaliableVersionsWithoutOSGI);
        checkBMV("1.0.redhat-1", "1", avaliableVersionsWithoutOSGI);

        String[] avaliableVersionsWithoutOSGI2 = { "1.redhat-1" };
        checkBMV("1.redhat-1", "1.0.0", avaliableVersionsWithoutOSGI2);
        checkBMV("1.redhat-1", "1.0", avaliableVersionsWithoutOSGI2);
        checkBMV("1.redhat-1", "1", avaliableVersionsWithoutOSGI2);
    }

    @Test
    public void nonOSGIVersionsTest() {
        String[] avaliableVersions1 = { "1.0.0.redhat-1", "1.0.redhat-2", "1.redhat-3" };
        checkBMV("1.redhat-3", "1.0.0", avaliableVersions1);
        checkBMV("1.redhat-3", "1.0", avaliableVersions1);
        checkBMV("1.redhat-3", "1", avaliableVersions1);

        String[] avaliableVersions10 = { "1.0.0.redhat-1", "1.0.redhat-3", "1.redhat-2" };
        checkBMV("1.0.redhat-3", "1.0.0", avaliableVersions10);
        checkBMV("1.0.redhat-3", "1.0", avaliableVersions10);
        checkBMV("1.0.redhat-3", "1", avaliableVersions10);

        String[] avaliableVersions100 = { "1.0.0.redhat-3", "1.0.redhat-2", "1.redhat-1" };
        checkBMV("1.0.0.redhat-3", "1.0.0", avaliableVersions100);
        checkBMV("1.0.0.redhat-3", "1.0", avaliableVersions100);
        checkBMV("1.0.0.redhat-3", "1", avaliableVersions100);
    }

    @Test
    public void NCL4266ReproducerTest() {
        String[] avaliableVersions1 = {
                "2.2.3.redhat-00001",
                "2.2.0.temporary-redhat-00001",
                "2.2.0.redhat-00001",
                "2.1.16.redhat-00001",
                "2.1.9.redhat-1",
                "2.1.9.redhat-001",
                "2.1.3.redhat-001" };
        checkBMV(
                new VersionAnalyzer(Arrays.asList("temporary-redhat", "redhat")),
                "2.2.3.redhat-00001",
                "2.2.3",
                avaliableVersions1);
    }

    @Test
    public void preferOSGiVersionFormatTest() {
        // as 3.0.0-redhat-2 and 3.0.0.redhat-2 are the same version then ordering in the array matters
        // (if they were in opposite direction test would pass even without OSGi preference)
        String[] availableVersions = {
                "3-redhat-2",
                "3.0.0-redhat-2",
                "3.0.0.redhat-2",
                "3.0.0.redhat-1",
                "2.1.1.redhat-3",
                "2.1.16-redhat-9",
                "2.9.9-redhat-00001" };
        checkBMV("3.0.0.redhat-2", "3", availableVersions);
        Collections.reverse(Arrays.asList(availableVersions));
        checkBMV("3.0.0.redhat-2", "3", availableVersions);
        String[] availableVersions2 = {
                "2.1.1.redhat-3",
                "2.1.16-redhat-9",
                "3-redhat-2",
                "3.0-redhat-2",
                "3.0.redhat-2",
                "3.0.-redhat-2",
                "3.0.0-redhat-2",
                "3.0.0.redhat-1",
                "3.0.0.redhat-2",
                "2.9.9-redhat-00001" };
        checkBMV("3.0.0.redhat-2", "3", availableVersions2);
        Collections.reverse(Arrays.asList(availableVersions2));
        checkBMV("3.0.0.redhat-2", "3", availableVersions2);
        String[] availableVersions3 = {
                "2.1.1.redhat-3",
                "2.1.16-redhat-9",
                "3-redhat-2",
                "3.0-redhat-2",
                "3.0.redhat-2",
                "3.0.-redhat-2",
                "3.0.0-redhat-2",
                "3.0.0.redhat-1",
                "2.9.9-redhat-00001" };
        checkBMV("3.0.0-redhat-2", "3", availableVersions3);
        Collections.reverse(Arrays.asList(availableVersions3));
        checkBMV("3.0.0-redhat-2", "3", availableVersions3);
        String[] availableVersions4 = {
                "2.1.1.redhat-3",
                "2.1.16-redhat-9",
                "3-redhat-2",
                "3.0-redhat-2",
                "3.0.redhat-2",
                "3.0.0.redhat-1",
                "2.9.9-redhat-00001" };
        checkBMV("3.0.redhat-2", "3", availableVersions4);
        Collections.reverse(Arrays.asList(availableVersions4));
        checkBMV("3.0.redhat-2", "3", availableVersions4);
    }

    private void checkBMV(String expectedVersion, String version, String[] versions) {
        checkBMV(versionFinder, expectedVersion, version, versions);
    }

    private void checkBMV(VersionAnalyzer versionAnalyzer, String expectedVersion, String version, String[] versions) {
        Optional<String> bmv = versionAnalyzer.findBiggestMatchingVersion(
                version,
                Arrays.stream(versions).map(QualifiedVersion::new).collect(Collectors.toList()));

        assertTrue("Best match version expected to be present", bmv.isPresent());
        assertEquals(expectedVersion, bmv.get());
    }

    private void checkBMV(
            VersionAnalyzer versionAnalyzer,
            String expectedVersion,
            String version,
            QualifiedVersion[] versions) {
        Optional<String> bmv = versionAnalyzer
                .findBiggestMatchingVersion(version, Arrays.stream(versions).collect(Collectors.toList()));

        assertTrue("Best match version expected to be present", bmv.isPresent());
        assertEquals(expectedVersion, bmv.get());
    }

    @Test
    public void testDifferentSuffix() {
        VersionAnalyzer versionAnalyzer = new VersionAnalyzer(Arrays.asList("temporary-redhat", "redhat"));
        String version = "1.4.0";
        String expectedVersion = "1.4.0.temporary-redhat-1";

        String[] avaliableVersionsOrder1 = { "1.4.0.redhat-1", "1.4.0.temporary-redhat-1" };
        checkBMV(versionAnalyzer, expectedVersion, version, avaliableVersionsOrder1);

        String[] avaliableVersionsOrder2 = { "1.4.0.temporary-redhat-1", "1.4.0.redhat-1" };
        checkBMV(versionAnalyzer, expectedVersion, version, avaliableVersionsOrder2);

        String[] avaliableVersionsMultiple = {
                "1.4.0.redhat-4",
                "1.4.0.redhat-3",
                "1.4.0.redhat-2",
                "1.4.0.redhat-1",
                "1.4.0.temporary-redhat-1", };
        checkBMV(versionAnalyzer, expectedVersion, version, avaliableVersionsMultiple);
    }

    @Test
    public void testDifferentSuffixWithOnlyDefaultVersions() {
        VersionAnalyzer versionAnalyzer = new VersionAnalyzer(Arrays.asList("t20180522-115319-991-redhat", "redhat"));
        String version = "1.4.0";

        String[] avaliableVersionsOrder1 = { "1.4.0.redhat-1", "1.4.0.temporary-redhat-1" };
        checkBMV(versionAnalyzer, "1.4.0.redhat-1", version, avaliableVersionsOrder1);

        String[] avaliableVersionsOrder2 = { "1.4.0.temporary-redhat-1", "1.4.0.redhat-1" };
        checkBMV(versionAnalyzer, "1.4.0.redhat-1", version, avaliableVersionsOrder2);

        String[] avaliableVersionsMultiple = {
                "1.4.0.redhat-4",
                "1.4.0.redhat-3",
                "1.4.0.redhat-2",
                "1.4.0.redhat-1",
                "1.4.0.temporary-redhat-1", };
        checkBMV(versionAnalyzer, "1.4.0.redhat-4", version, avaliableVersionsMultiple);
    }

    @Test
    public void testSingleRankOfQuality() {
        VersionStrategy strat = VersionStrategy.from(List.of("QUALITY:RELEASED"), null, null);
        VersionAnalyzer versionAnalyzer = new VersionAnalyzer(List.of("redhat"), strat);
        String version = "1.4.0";

        List<QualifiedVersion> versions = new ArrayList<>(
                List.of(
                        new QualifiedVersion("1.4.0.redhat-4", Map.of(Qualifier.QUALITY, Set.of("TESTED"))),
                        new QualifiedVersion("1.4.0.redhat-3", Map.of(Qualifier.PRODUCT_ID, Set.of("1"))),
                        new QualifiedVersion("1.4.0.redhat-2", Map.of(Qualifier.QUALITY, Set.of("RELEASED"))),
                        new QualifiedVersion("1.4.0.redhat-1", Map.of(Qualifier.QUALITY, Set.of("RELEASED")))));

        String[] expectedOrder = { "1.4.0.redhat-2", "1.4.0.redhat-1", "1.4.0.redhat-4" };

        expectOrder(versionAnalyzer, expectedOrder, version, versions);
    }

    @Test
    public void testSingleRankOfProduct() {
        VersionStrategy strat = VersionStrategy.from(List.of("PRODUCT_ID:1"), null, null);
        VersionAnalyzer versionAnalyzer = new VersionAnalyzer(List.of("redhat"), strat);
        String version = "1.4.0";

        List<QualifiedVersion> versions = new ArrayList<>(
                List.of(
                        new QualifiedVersion("1.4.0.redhat-4", Map.of(Qualifier.QUALITY, Set.of("TESTED"))),
                        new QualifiedVersion("1.4.0.redhat-3", Map.of(Qualifier.PRODUCT_ID, Set.of("1"))),
                        new QualifiedVersion("1.4.0.redhat-2", Map.of(Qualifier.QUALITY, Set.of("RELEASED"))),
                        new QualifiedVersion("1.4.0.redhat-1", Map.of(Qualifier.QUALITY, Set.of("RELEASED")))));

        String[] expectedOrder = { "1.4.0.redhat-3", "1.4.0.redhat-4", "1.4.0.redhat-2" };

        expectOrder(versionAnalyzer, expectedOrder, version, versions);
    }

    @Test
    public void testSingleRankWithNoMatch() {
        VersionStrategy strat = VersionStrategy.from(List.of("QUALITY:RELEASED"), null, null);
        VersionAnalyzer versionAnalyzer = new VersionAnalyzer(List.of("redhat"), strat);
        String version = "1.4.0";

        List<QualifiedVersion> versions = new ArrayList<>(
                List.of(
                        new QualifiedVersion("1.4.0.redhat-4", Map.of(Qualifier.QUALITY, Set.of("TESTED"))),
                        new QualifiedVersion("1.4.0.redhat-3", Map.of(Qualifier.PRODUCT_ID, Set.of("1"))),
                        new QualifiedVersion("1.4.0.redhat-2", Map.of(Qualifier.QUALITY, Set.of("NEW"))),
                        new QualifiedVersion("1.4.0.redhat-1", Map.of(Qualifier.QUALITY, Set.of("BLACKLISTED")))));

        String[] expectedOrder = { "1.4.0.redhat-4", "1.4.0.redhat-3", "1.4.0.redhat-2" };

        expectOrder(versionAnalyzer, expectedOrder, version, versions);
    }

    @Test
    public void testTwoSimilarRanks() {
        VersionStrategy strat = VersionStrategy.from(List.of("QUALITY:RELEASED", "QUALITY:TESTED"), null, null);
        VersionAnalyzer versionAnalyzer = new VersionAnalyzer(List.of("redhat"), strat);
        String version = "1.4.0";

        List<QualifiedVersion> versions = new ArrayList<>(
                List.of(
                        new QualifiedVersion("1.4.0.redhat-4", Map.of(Qualifier.PRODUCT_ID, Set.of("1"))),
                        new QualifiedVersion("1.4.0.redhat-3", Map.of(Qualifier.QUALITY, Set.of("TESTED"))),
                        new QualifiedVersion("1.4.0.redhat-2", Map.of(Qualifier.QUALITY, Set.of("RELEASED"))),
                        new QualifiedVersion("1.4.0.redhat-1", Map.of(Qualifier.QUALITY, Set.of("RELEASED")))));

        String[] expectedOrder = { "1.4.0.redhat-2", "1.4.0.redhat-1", "1.4.0.redhat-3" };

        expectOrder(versionAnalyzer, expectedOrder, version, versions);
    }

    @Test
    public void testTwoSimilarRanksReversed() {
        VersionStrategy strat = VersionStrategy.from(List.of("QUALITY:TESTED", "QUALITY:RELEASED"), null, null);
        VersionAnalyzer versionAnalyzer = new VersionAnalyzer(List.of("redhat"), strat);
        String version = "1.4.0";

        List<QualifiedVersion> versions = new ArrayList<>(
                List.of(
                        new QualifiedVersion("1.4.0.redhat-4", Map.of(Qualifier.PRODUCT_ID, Set.of("1"))),
                        new QualifiedVersion("1.4.0.redhat-3", Map.of(Qualifier.QUALITY, Set.of("TESTED"))),
                        new QualifiedVersion("1.4.0.redhat-2", Map.of(Qualifier.QUALITY, Set.of("RELEASED"))),
                        new QualifiedVersion("1.4.0.redhat-1", Map.of(Qualifier.QUALITY, Set.of("RELEASED")))));

        String[] expectedOrder = { "1.4.0.redhat-3", "1.4.0.redhat-2", "1.4.0.redhat-1" };

        expectOrder(versionAnalyzer, expectedOrder, version, versions);
    }

    @Test
    public void testTwoDistinctRanks() {
        /**
         * Ranks: 1st: PRODUCT:EAP 2nd: QUALITY:RELEASED 3rd: SUFFIX-VERSION
         */
        VersionStrategy strat = VersionStrategy.from(List.of("PRODUCT:EAP", "QUALITY:RELEASED"), null, null);
        VersionAnalyzer versionAnalyzer = new VersionAnalyzer(List.of("redhat"), strat);
        String version = "1.4.0";

        List<QualifiedVersion> versions = new ArrayList<>(
                List.of(
                        new QualifiedVersion("1.4.0.redhat-6", Map.of(Qualifier.PRODUCT, Set.of("EAP"))),
                        new QualifiedVersion(
                                "1.4.0.redhat-5",
                                Map.of(Qualifier.QUALITY, Set.of("RELEASED"), Qualifier.PRODUCT, Set.of("RHSSO"))),
                        new QualifiedVersion("1.4.0.redhat-4", Map.of(Qualifier.PRODUCT, Set.of("RHSSO"))),
                        new QualifiedVersion(
                                "1.4.0.redhat-3",
                                Map.of(Qualifier.QUALITY, Set.of("RELEASED"), Qualifier.PRODUCT, Set.of("EAP"))),
                        new QualifiedVersion("1.4.0.redhat-2", Map.of()),
                        new QualifiedVersion("1.4.0.redhat-1", Map.of(Qualifier.QUALITY, Set.of("TESTED")))));

        String[] expectedOrder = {
                "1.4.0.redhat-3",
                "1.4.0.redhat-6",
                "1.4.0.redhat-5",
                "1.4.0.redhat-4",
                "1.4.0.redhat-2" };

        expectOrder(versionAnalyzer, expectedOrder, version, versions);
    }

    @Test
    public void testTwoSimilarRanksInOr() {
        /**
         * Ranks: 1st: PRODUCT:EAP or PRODUCT:RHSSO 2nd: QUALITY:RELEASED 3rd: SUFFIX-VERSION
         */
        VersionStrategy strat = VersionStrategy
                .from(List.of("PRODUCT:EAP or PRODUCT:RHSSO", "QUALITY:RELEASED"), null, null);
        VersionAnalyzer versionAnalyzer = new VersionAnalyzer(List.of("redhat"), strat);
        String version = "1.4.0";

        List<QualifiedVersion> versions = new ArrayList<>(
                List.of(
                        new QualifiedVersion("1.4.0.redhat-6", Map.of(Qualifier.PRODUCT, Set.of("EAP"))),
                        new QualifiedVersion(
                                "1.4.0.redhat-5",
                                Map.of(Qualifier.QUALITY, Set.of("RELEASED"), Qualifier.PRODUCT, Set.of("RHSSO"))),
                        new QualifiedVersion("1.4.0.redhat-4", Map.of(Qualifier.PRODUCT, Set.of("RHSSO"))),
                        new QualifiedVersion(
                                "1.4.0.redhat-3",
                                Map.of(Qualifier.QUALITY, Set.of("RELEASED"), Qualifier.PRODUCT, Set.of("EAP"))),
                        new QualifiedVersion("1.4.0.redhat-2", Map.of(Qualifier.QUALITY, Set.of("TESTED"))),
                        new QualifiedVersion("1.4.0.redhat-1", Map.of(Qualifier.QUALITY, Set.of("RELEASED")))));

        String[] expectedOrder = {
                "1.4.0.redhat-5",
                "1.4.0.redhat-3",
                "1.4.0.redhat-6",
                "1.4.0.redhat-4",
                "1.4.0.redhat-1" };

        expectOrder(versionAnalyzer, expectedOrder, version, versions);
    }

    @Test
    public void testMultiValueRanksInOr() {
        /**
         * Ranks: 1st: QUALITY:RELEASED or QUALITY:TESTED 2nd: PRODUCT:EAP 3rd: SUFFIX-VERSION
         */
        VersionStrategy strat = VersionStrategy
                .from(List.of("QUALITY:RELEASED or QUALITY:TESTED", "PRODUCT:EAP"), null, null);
        VersionAnalyzer versionAnalyzer = new VersionAnalyzer(List.of("redhat"), strat);
        String version = "1.4.0";

        List<QualifiedVersion> versions = new ArrayList<>(
                List.of(
                        new QualifiedVersion("1.4.0.redhat-6", Map.of(Qualifier.PRODUCT, Set.of("EAP"))),
                        new QualifiedVersion(
                                "1.4.0.redhat-5",
                                Map.of(
                                        Qualifier.QUALITY,
                                        Set.of("RELEASED", "TESTED"),
                                        Qualifier.PRODUCT,
                                        Set.of("RHSSO"))),
                        new QualifiedVersion("1.4.0.redhat-4", Map.of(Qualifier.PRODUCT, Set.of("RHSSO"))),
                        new QualifiedVersion(
                                "1.4.0.redhat-3",
                                Map.of(Qualifier.QUALITY, Set.of("RELEASED"), Qualifier.PRODUCT, Set.of("EAP"))),
                        new QualifiedVersion("1.4.0.redhat-2", Map.of()),
                        new QualifiedVersion("1.4.0.redhat-1", Map.of(Qualifier.QUALITY, Set.of("TESTED")))));

        String[] expectedOrder = {
                "1.4.0.redhat-3",
                "1.4.0.redhat-5",
                "1.4.0.redhat-1",
                "1.4.0.redhat-6",
                "1.4.0.redhat-4" };

        expectOrder(versionAnalyzer, expectedOrder, version, versions);
    }

    @Test
    public void testMultiValueRanksInOrder() {
        /**
         * Ranks: 1st: QUALITY:RELEASED 2nd: QUALITY:TESTED 3rd: PRODUCT:EAP 4th: SUFFIX-VERSION
         */
        VersionStrategy strat = VersionStrategy
                .from(List.of("QUALITY:RELEASED", "QUALITY:TESTED", "PRODUCT:EAP"), null, null);
        VersionAnalyzer versionAnalyzer = new VersionAnalyzer(List.of("redhat"), strat);
        String version = "1.4.0";

        List<QualifiedVersion> versions = new ArrayList<>(
                List.of(
                        new QualifiedVersion("1.4.0.redhat-6", Map.of(Qualifier.PRODUCT, Set.of("EAP"))),
                        new QualifiedVersion(
                                "1.4.0.redhat-5",
                                Map.of(
                                        Qualifier.QUALITY,
                                        Set.of("RELEASED", "TESTED"),
                                        Qualifier.PRODUCT,
                                        Set.of("RHSSO"))),
                        new QualifiedVersion("1.4.0.redhat-4", Map.of(Qualifier.PRODUCT, Set.of("RHSSO"))),
                        new QualifiedVersion(
                                "1.4.0.redhat-3",
                                Map.of(Qualifier.QUALITY, Set.of("RELEASED"), Qualifier.PRODUCT, Set.of("EAP"))),
                        new QualifiedVersion("1.4.0.redhat-2", Map.of()),
                        new QualifiedVersion("1.4.0.redhat-1", Map.of(Qualifier.QUALITY, Set.of("TESTED")))));

        String[] expectedOrder = {
                "1.4.0.redhat-5",
                "1.4.0.redhat-3",
                "1.4.0.redhat-1",
                "1.4.0.redhat-6",
                "1.4.0.redhat-4" };

        expectOrder(versionAnalyzer, expectedOrder, version, versions);
    }

    @Test
    public void testLogicOperatorPriorities() {
        /**
         * Ranks: 1st: PRODUCT:EAP or QUALITY:TESTED and QUALITY:RELEASED 2nd: SUFFIX-VERSION
         * <p>
         * With explicit parentheses, Ranks would look like this: 1st: PRODUCT:EAP or (QUALITY:TESTED and
         * QUALITY:RELEASED) 2nd: SUFFIX-VERSION
         */
        VersionStrategy strat = VersionStrategy
                .from(List.of("PRODUCT:EAP or QUALITY:TESTED and QUALITY:RELEASED"), null, null);
        VersionAnalyzer versionAnalyzer = new VersionAnalyzer(List.of("redhat"), strat);
        String version = "1.4.0";

        List<QualifiedVersion> versions = new ArrayList<>(
                List.of(
                        new QualifiedVersion("1.4.0.redhat-6", Map.of(Qualifier.PRODUCT, Set.of("EAP"))),
                        new QualifiedVersion(
                                "1.4.0.redhat-5",
                                Map.of(
                                        Qualifier.QUALITY,
                                        Set.of("RELEASED", "TESTED"),
                                        Qualifier.PRODUCT,
                                        Set.of("RHSSO"))),
                        new QualifiedVersion("1.4.0.redhat-4", Map.of(Qualifier.PRODUCT, Set.of("RHSSO"))),
                        new QualifiedVersion(
                                "1.4.0.redhat-3",
                                Map.of(Qualifier.QUALITY, Set.of("RELEASED"), Qualifier.PRODUCT, Set.of("EAP"))),
                        new QualifiedVersion("1.4.0.redhat-2", Map.of()),
                        new QualifiedVersion("1.4.0.redhat-1", Map.of(Qualifier.QUALITY, Set.of("TESTED")))));

        String[] expectedOrder = {
                "1.4.0.redhat-6",
                "1.4.0.redhat-5",
                "1.4.0.redhat-3",
                "1.4.0.redhat-4",
                "1.4.0.redhat-2" };

        expectOrder(versionAnalyzer, expectedOrder, version, versions);
    }

    @Test
    public void testLogicOperatorOverridePrecedence() {
        /**
         * Ranks: 1st: (PRODUCT:EAP or QUALITY:TESTED) and QUALITY:RELEASED 2nd: SUFFIX-VERSION
         */
        VersionStrategy strat = VersionStrategy
                .from(List.of("(PRODUCT:EAP or QUALITY:TESTED) and QUALITY:RELEASED"), null, null);
        VersionAnalyzer versionAnalyzer = new VersionAnalyzer(List.of("redhat"), strat);
        String version = "1.4.0";

        List<QualifiedVersion> versions = new ArrayList<>(
                List.of(
                        new QualifiedVersion("1.4.0.redhat-6", Map.of(Qualifier.PRODUCT, Set.of("EAP"))),
                        new QualifiedVersion(
                                "1.4.0.redhat-5",
                                Map.of(
                                        Qualifier.QUALITY,
                                        Set.of("RELEASED", "TESTED"),
                                        Qualifier.PRODUCT,
                                        Set.of("RHSSO"))),
                        new QualifiedVersion("1.4.0.redhat-4", Map.of(Qualifier.PRODUCT, Set.of("RHSSO"))),
                        new QualifiedVersion(
                                "1.4.0.redhat-3",
                                Map.of(Qualifier.QUALITY, Set.of("RELEASED"), Qualifier.PRODUCT, Set.of("EAP"))),
                        new QualifiedVersion("1.4.0.redhat-2", Map.of()),
                        new QualifiedVersion("1.4.0.redhat-1", Map.of(Qualifier.QUALITY, Set.of("TESTED")))));

        String[] expectedOrder = {
                "1.4.0.redhat-5",
                "1.4.0.redhat-3",
                "1.4.0.redhat-6",
                "1.4.0.redhat-4",
                "1.4.0.redhat-2" };

        expectOrder(versionAnalyzer, expectedOrder, version, versions);
    }

    @Test
    public void testHardSorting() {
        /**
         * Ranks: 1st: PRODUCT:EAP sort-by SUFFIX-VERSION 2nd: QUALITY:RELEASED sort-by SUFFIX-VERSION 3rd:
         * SUFFIX-VERSION
         */
        VersionStrategy strat = VersionStrategy.from(
                List.of("PRODUCT:EAP sort-by SUFFIX-VERSION", "QUALITY:RELEASED sort-by SUFFIX-VERSION"),
                null,
                null);
        VersionAnalyzer versionAnalyzer = new VersionAnalyzer(List.of("redhat"), strat);
        String version = "1.4.0";

        List<QualifiedVersion> versions = new ArrayList<>(
                List.of(
                        new QualifiedVersion("1.4.0.redhat-6", Map.of(Qualifier.PRODUCT, Set.of("EAP"))),
                        new QualifiedVersion(
                                "1.4.0.redhat-5",
                                Map.of(Qualifier.QUALITY, Set.of("RELEASED"), Qualifier.PRODUCT, Set.of("RHSSO"))),
                        new QualifiedVersion("1.4.0.redhat-4", Map.of(Qualifier.PRODUCT, Set.of("RHSSO"))),
                        new QualifiedVersion(
                                "1.4.0.redhat-3",
                                Map.of(Qualifier.QUALITY, Set.of("RELEASED"), Qualifier.PRODUCT, Set.of("EAP"))),
                        new QualifiedVersion("1.4.0.redhat-2", Map.of(Qualifier.QUALITY, Set.of())),
                        new QualifiedVersion("1.4.0.redhat-1", Map.of(Qualifier.QUALITY, Set.of("TESTED")))));

        String[] expectedOrder = {
                "1.4.0.redhat-6",
                "1.4.0.redhat-3",
                "1.4.0.redhat-5",
                "1.4.0.redhat-4",
                "1.4.0.redhat-2" };
        expectOrder(versionAnalyzer, expectedOrder, version, versions);
    }

    @Test
    public void testHardSortingWithOr() {
        /**
         * Ranks: 1st: PRODUCT:EAP or QUALITY:TESTED sort-by SUFFIX-VERSION 2nd: QUALITY:RELEASED sort-by SUFFIX-VERSION
         * 3rd: SUFFIX-VERSION
         */
        VersionStrategy strat = VersionStrategy.from(
                List.of(
                        "PRODUCT:EAP or QUALITY:TESTED sort-by SUFFIX-VERSION",
                        "QUALITY:RELEASED sort-by SUFFIX-VERSION"),
                null,
                null);
        VersionAnalyzer versionAnalyzer = new VersionAnalyzer(List.of("redhat"), strat);
        String version = "1.4.0";

        List<QualifiedVersion> versions = new ArrayList<>(
                List.of(
                        new QualifiedVersion("1.4.0.redhat-6", Map.of(Qualifier.PRODUCT, Set.of("EAP"))),
                        new QualifiedVersion(
                                "1.4.0.redhat-5",
                                Map.of(Qualifier.QUALITY, Set.of("RELEASED"), Qualifier.PRODUCT, Set.of("RHSSO"))),
                        new QualifiedVersion("1.4.0.redhat-4", Map.of(Qualifier.PRODUCT, Set.of("RHSSO"))),
                        new QualifiedVersion(
                                "1.4.0.redhat-3",
                                Map.of(Qualifier.QUALITY, Set.of("RELEASED"), Qualifier.PRODUCT, Set.of("EAP"))),
                        new QualifiedVersion("1.4.0.redhat-2", Map.of(Qualifier.QUALITY, Set.of())),
                        new QualifiedVersion("1.4.0.redhat-1", Map.of(Qualifier.QUALITY, Set.of("TESTED")))));

        String[] expectedOrder = {
                "1.4.0.redhat-6",
                "1.4.0.redhat-3",
                "1.4.0.redhat-1",
                "1.4.0.redhat-5",
                "1.4.0.redhat-4" };

        expectOrder(versionAnalyzer, expectedOrder, version, versions);
    }

    @Test
    public void testHardSortingWithAnd() {
        /**
         * Ranks: 1st: PRODUCT:EAP and QUALITY:TESTED sort-by SUFFIX-VERSION 2nd: QUALITY:RELEASED sort-by
         * SUFFIX-VERSION 3rd: SUFFIX-VERSION
         */
        VersionStrategy strat = VersionStrategy.from(
                List.of(
                        "PRODUCT:EAP and QUALITY:TESTED sort-by SUFFIX-VERSION",
                        "QUALITY:RELEASED sort-by SUFFIX-VERSION"),
                null,
                null);
        VersionAnalyzer versionAnalyzer = new VersionAnalyzer(List.of("redhat"), strat);
        String version = "1.4.0";

        List<QualifiedVersion> versions = new ArrayList<>(
                List.of(
                        new QualifiedVersion("1.4.0.redhat-6", Map.of(Qualifier.PRODUCT, Set.of("EAP"))),
                        new QualifiedVersion(
                                "1.4.0.redhat-5",
                                Map.of(Qualifier.QUALITY, Set.of("RELEASED"), Qualifier.PRODUCT, Set.of("RHSSO"))),
                        new QualifiedVersion("1.4.0.redhat-4", Map.of(Qualifier.PRODUCT, Set.of("RHSSO"))),
                        new QualifiedVersion(
                                "1.4.0.redhat-3",
                                Map.of(Qualifier.QUALITY, Set.of("RELEASED"), Qualifier.PRODUCT, Set.of("EAP"))),
                        new QualifiedVersion("1.4.0.redhat-2", Map.of(Qualifier.QUALITY, Set.of())),
                        new QualifiedVersion("1.4.0.redhat-1", Map.of(Qualifier.QUALITY, Set.of("TESTED")))));

        String[] expectedOrder = {
                "1.4.0.redhat-5",
                "1.4.0.redhat-3",
                "1.4.0.redhat-6",
                "1.4.0.redhat-4",
                "1.4.0.redhat-2" };

        expectOrder(versionAnalyzer, expectedOrder, version, versions);
    }

    @Test
    public void testSortingCombination() {
        /**
         * Ranks: 1st: PRODUCT:EAP 2nd: QUALITY:RELEASED sort-by SUFFIX-VERSION 3rd: QUALITY:TESTED 4th: SUFFIX-VERSION
         */
        VersionStrategy strat = VersionStrategy
                .from(List.of("PRODUCT:EAP", "QUALITY:RELEASED sort-by SUFFIX-VERSION", "QUALITY:TESTED"), null, null);
        VersionAnalyzer versionAnalyzer = new VersionAnalyzer(List.of("redhat"), strat);
        String version = "1.4.0";

        List<QualifiedVersion> versions = new ArrayList<>(
                List.of(
                        new QualifiedVersion("1.4.0.redhat-6", Map.of(Qualifier.PRODUCT, Set.of("EAP"))),
                        new QualifiedVersion(
                                "1.4.0.redhat-5",
                                Map.of(Qualifier.QUALITY, Set.of("RELEASED"), Qualifier.PRODUCT, Set.of("RHSSO"))),
                        new QualifiedVersion("1.4.0.redhat-4", Map.of(Qualifier.PRODUCT, Set.of("RHSSO"))),
                        new QualifiedVersion(
                                "1.4.0.redhat-3",
                                Map.of(Qualifier.QUALITY, Set.of("RELEASED"), Qualifier.PRODUCT, Set.of("EAP"))),
                        new QualifiedVersion(
                                "1.4.0.redhat-2",
                                Map.of(
                                        Qualifier.QUALITY,
                                        Set.of("RELEASED", "TESTED"),
                                        Qualifier.PRODUCT,
                                        Set.of("EAP"))),
                        new QualifiedVersion("1.4.0.redhat-1", Map.of(Qualifier.QUALITY, Set.of("TESTED")))));

        String[] expectedOrder = {
                "1.4.0.redhat-3",
                "1.4.0.redhat-2",
                "1.4.0.redhat-6",
                "1.4.0.redhat-5",
                "1.4.0.redhat-1" };

        expectOrder(versionAnalyzer, expectedOrder, version, versions);
    }

    @Test
    public void testSortingCombinationWithoutHard() {
        // DEMONSTRATES distinction against #testSortingCombination()
        /**
         * Ranks: 1st: PRODUCT:EAP 2nd: QUALITY:RELEASED 3rd: QUALITY:TESTED 4th: SUFFIX-VERSION
         */
        VersionStrategy strat = VersionStrategy
                .from(List.of("PRODUCT:EAP", "QUALITY:RELEASED", "QUALITY:TESTED"), null, null);
        VersionAnalyzer versionAnalyzer = new VersionAnalyzer(List.of("redhat"), strat);
        String version = "1.4.0";

        List<QualifiedVersion> versions = new ArrayList<>(
                List.of(
                        new QualifiedVersion("1.4.0.redhat-6", Map.of(Qualifier.PRODUCT, Set.of("EAP"))),
                        new QualifiedVersion(
                                "1.4.0.redhat-5",
                                Map.of(Qualifier.QUALITY, Set.of("RELEASED"), Qualifier.PRODUCT, Set.of("RHSSO"))),
                        new QualifiedVersion("1.4.0.redhat-4", Map.of(Qualifier.PRODUCT, Set.of("RHSSO"))),
                        new QualifiedVersion(
                                "1.4.0.redhat-3",
                                Map.of(Qualifier.QUALITY, Set.of("RELEASED"), Qualifier.PRODUCT, Set.of("EAP"))),
                        new QualifiedVersion(
                                "1.4.0.redhat-2",
                                Map.of(
                                        Qualifier.QUALITY,
                                        Set.of("RELEASED", "TESTED"),
                                        Qualifier.PRODUCT,
                                        Set.of("EAP"))),
                        new QualifiedVersion("1.4.0.redhat-1", Map.of(Qualifier.QUALITY, Set.of("TESTED")))));

        String[] expectedOrder = {
                "1.4.0.redhat-2",
                "1.4.0.redhat-3",
                "1.4.0.redhat-6",
                "1.4.0.redhat-5",
                "1.4.0.redhat-1" };

        expectOrder(versionAnalyzer, expectedOrder, version, versions);
    }

    @Test
    public void testSimpleDenyList() {
        /**
         * Deny list: QUALITY:RELEASED
         */
        VersionStrategy strat = VersionStrategy.from(null, null, "QUALITY:RELEASED");
        VersionAnalyzer versionAnalyzer = new VersionAnalyzer(List.of("redhat"), strat);
        String version = "1.4.0";

        List<QualifiedVersion> versions = new ArrayList<>(
                List.of(
                        new QualifiedVersion("1.4.0.redhat-6", Map.of(Qualifier.PRODUCT, Set.of("EAP"))),
                        new QualifiedVersion(
                                "1.4.0.redhat-5",
                                Map.of(Qualifier.QUALITY, Set.of("RELEASED"), Qualifier.PRODUCT, Set.of("RHSSO"))),
                        new QualifiedVersion("1.4.0.redhat-4", Map.of(Qualifier.PRODUCT, Set.of("RHSSO"))),
                        new QualifiedVersion(
                                "1.4.0.redhat-3",
                                Map.of(Qualifier.QUALITY, Set.of("RELEASED"), Qualifier.PRODUCT, Set.of("EAP"))),
                        new QualifiedVersion("1.4.0.redhat-2", Map.of(Qualifier.QUALITY, Set.of())),
                        new QualifiedVersion("1.4.0.redhat-1", Map.of(Qualifier.QUALITY, Set.of("TESTED")))));

        String[] expectedOrder = { "1.4.0.redhat-6", "1.4.0.redhat-4", "1.4.0.redhat-2", "1.4.0.redhat-1" };
        expectOrder(versionAnalyzer, expectedOrder, version, versions);
    }

    @Test
    public void testMultipleDenyList() {
        /**
         * Deny list: QUALITY:RELEASED, PRODUCT:RHSSO
         */
        VersionStrategy strat = VersionStrategy.from(null, null, "QUALITY:RELEASED, PRODUCT:RHSSO");
        VersionAnalyzer versionAnalyzer = new VersionAnalyzer(List.of("redhat"), strat);
        String version = "1.4.0";

        List<QualifiedVersion> versions = new ArrayList<>(
                List.of(
                        new QualifiedVersion("1.4.0.redhat-6", Map.of(Qualifier.PRODUCT, Set.of("EAP"))),
                        new QualifiedVersion(
                                "1.4.0.redhat-5",
                                Map.of(Qualifier.QUALITY, Set.of("RELEASED"), Qualifier.PRODUCT, Set.of("RHSSO"))),
                        new QualifiedVersion("1.4.0.redhat-4", Map.of(Qualifier.PRODUCT, Set.of("RHSSO"))),
                        new QualifiedVersion(
                                "1.4.0.redhat-3",
                                Map.of(Qualifier.QUALITY, Set.of("RELEASED"), Qualifier.PRODUCT, Set.of("EAP"))),
                        new QualifiedVersion("1.4.0.redhat-2", Map.of(Qualifier.QUALITY, Set.of())),
                        new QualifiedVersion("1.4.0.redhat-1", Map.of(Qualifier.QUALITY, Set.of("TESTED")))));

        String[] expectedOrder = { "1.4.0.redhat-6", "1.4.0.redhat-2", "1.4.0.redhat-1" };
        expectOrder(versionAnalyzer, expectedOrder, version, versions);
    }

    @Test
    public void testAllowList() {
        /**
         * Allow list: QUALITY:RELEASED
         */
        VersionStrategy strat = VersionStrategy.from(null, "QUALITY:RELEASED", null);
        VersionAnalyzer versionAnalyzer = new VersionAnalyzer(List.of("redhat"), strat);
        String version = "1.4.0";

        List<QualifiedVersion> versions = new ArrayList<>(
                List.of(
                        new QualifiedVersion("1.4.0.redhat-6", Map.of(Qualifier.PRODUCT, Set.of("EAP"))),
                        new QualifiedVersion(
                                "1.4.0.redhat-5",
                                Map.of(Qualifier.QUALITY, Set.of("RELEASED"), Qualifier.PRODUCT, Set.of("RHSSO"))),
                        new QualifiedVersion("1.4.0.redhat-4", Map.of(Qualifier.PRODUCT, Set.of("RHSSO"))),
                        new QualifiedVersion(
                                "1.4.0.redhat-3",
                                Map.of(Qualifier.QUALITY, Set.of("RELEASED"), Qualifier.PRODUCT, Set.of("EAP"))),
                        new QualifiedVersion("1.4.0.redhat-2", Map.of(Qualifier.QUALITY, Set.of())),
                        new QualifiedVersion("1.4.0.redhat-1", Map.of(Qualifier.QUALITY, Set.of("TESTED")))));

        String[] expectedOrder = { "1.4.0.redhat-5", "1.4.0.redhat-3" };
        expectOrder(versionAnalyzer, expectedOrder, version, versions);
    }

    @Test
    public void testMultipleAllowList() {
        /**
         * Allow list: QUALITY:RELEASED, PRODUCT:EAP
         */
        VersionStrategy strat = VersionStrategy.from(null, "QUALITY:RELEASED, PRODUCT:EAP", null);
        VersionAnalyzer versionAnalyzer = new VersionAnalyzer(List.of("redhat"), strat);
        String version = "1.4.0";

        List<QualifiedVersion> versions = new ArrayList<>(
                List.of(
                        new QualifiedVersion("1.4.0.redhat-6", Map.of(Qualifier.PRODUCT, Set.of("EAP"))),
                        new QualifiedVersion(
                                "1.4.0.redhat-5",
                                Map.of(Qualifier.QUALITY, Set.of("RELEASED"), Qualifier.PRODUCT, Set.of("RHSSO"))),
                        new QualifiedVersion("1.4.0.redhat-4", Map.of(Qualifier.PRODUCT, Set.of("RHSSO"))),
                        new QualifiedVersion(
                                "1.4.0.redhat-3",
                                Map.of(Qualifier.QUALITY, Set.of("RELEASED"), Qualifier.PRODUCT, Set.of("EAP"))),
                        new QualifiedVersion("1.4.0.redhat-2", Map.of(Qualifier.QUALITY, Set.of())),
                        new QualifiedVersion("1.4.0.redhat-1", Map.of(Qualifier.QUALITY, Set.of("TESTED")))));

        String[] expectedOrder = { "1.4.0.redhat-6", "1.4.0.redhat-5", "1.4.0.redhat-3" };
        expectOrder(versionAnalyzer, expectedOrder, version, versions);
    }

    @Test
    public void testAllowAndDenyAtTheSameTime() {
        /**
         * Allow list: QUALITY:RELEASED Deny list: PRODUCT:RHSSO
         */
        VersionStrategy strat = VersionStrategy.from(null, "QUALITY:RELEASED", "PRODUCT:RHSSO");
        VersionAnalyzer versionAnalyzer = new VersionAnalyzer(List.of("redhat"), strat);
        String version = "1.4.0";

        List<QualifiedVersion> versions = new ArrayList<>(
                List.of(
                        new QualifiedVersion("1.4.0.redhat-6", Map.of(Qualifier.PRODUCT, Set.of("EAP"))),
                        new QualifiedVersion(
                                "1.4.0.redhat-5",
                                Map.of(Qualifier.QUALITY, Set.of("RELEASED"), Qualifier.PRODUCT, Set.of("RHSSO"))),
                        new QualifiedVersion("1.4.0.redhat-4", Map.of(Qualifier.PRODUCT, Set.of("RHSSO"))),
                        new QualifiedVersion(
                                "1.4.0.redhat-3",
                                Map.of(Qualifier.QUALITY, Set.of("RELEASED"), Qualifier.PRODUCT, Set.of("EAP"))),
                        new QualifiedVersion("1.4.0.redhat-2", Map.of(Qualifier.QUALITY, Set.of("RELEASED"))),
                        new QualifiedVersion("1.4.0.redhat-1", Map.of(Qualifier.QUALITY, Set.of("TESTED")))));

        String[] expectedOrder = { "1.4.0.redhat-3", "1.4.0.redhat-2" };
        expectOrder(versionAnalyzer, expectedOrder, version, versions);
    }

    @Test
    public void testAllowWithRanking() {
        /**
         * Allow list: QUALITY:RELEASED, PRODUCT:RHSSO
         *
         * Ranks: 1st: PRODUCT:EAP 2nd: SUFFIX-VERSION
         */
        VersionStrategy strat = VersionStrategy.from(List.of("PRODUCT:EAP"), "QUALITY:RELEASED, PRODUCT:RHSSO", null);
        VersionAnalyzer versionAnalyzer = new VersionAnalyzer(List.of("redhat"), strat);
        String version = "1.4.0";

        List<QualifiedVersion> versions = new ArrayList<>(
                List.of(
                        new QualifiedVersion("1.4.0.redhat-6", Map.of(Qualifier.PRODUCT, Set.of("EAP"))),
                        new QualifiedVersion(
                                "1.4.0.redhat-5",
                                Map.of(Qualifier.QUALITY, Set.of("RELEASED"), Qualifier.PRODUCT, Set.of("RHSSO"))),
                        new QualifiedVersion("1.4.0.redhat-4", Map.of(Qualifier.PRODUCT, Set.of("RHSSO"))),
                        new QualifiedVersion(
                                "1.4.0.redhat-3",
                                Map.of(Qualifier.QUALITY, Set.of("RELEASED"), Qualifier.PRODUCT, Set.of("EAP"))),
                        new QualifiedVersion("1.4.0.redhat-2", Map.of(Qualifier.QUALITY, Set.of("RELEASED"))),
                        new QualifiedVersion("1.4.0.redhat-1", Map.of(Qualifier.QUALITY, Set.of("TESTED")))));

        String[] expectedOrder = { "1.4.0.redhat-3", "1.4.0.redhat-5", "1.4.0.redhat-4", "1.4.0.redhat-2" };
        expectOrder(versionAnalyzer, expectedOrder, version, versions);
    }

    @Test
    public void testRankingWithSuffixes() {
        /**
         * Ranks: 1st: PRODUCT:RHSSO 2nd: PRODUCT:EAP 3rd: SUFFIX-VERSION
         */
        VersionStrategy strat = VersionStrategy.from(List.of("PRODUCT:RHSSO", "PRODUCT:EAP"), null, null);
        VersionAnalyzer versionAnalyzer = new VersionAnalyzer(List.of("temporary-redhat", "redhat"), strat);
        String version = "1.4.0";

        List<QualifiedVersion> versions = new ArrayList<>(
                List.of(
                        new QualifiedVersion("1.4.0.redhat-6", Map.of(Qualifier.PRODUCT, Set.of("EAP"))),
                        new QualifiedVersion(
                                "1.4.0.redhat-5",
                                Map.of(Qualifier.QUALITY, Set.of("RELEASED"), Qualifier.PRODUCT, Set.of("RHSSO"))),
                        new QualifiedVersion("1.4.0.redhat-4", Map.of(Qualifier.PRODUCT, Set.of("RHSSO"))),
                        new QualifiedVersion(
                                "1.4.0.temporary-redhat-3",
                                Map.of(Qualifier.QUALITY, Set.of("RELEASED"), Qualifier.PRODUCT, Set.of("EAP"))),
                        new QualifiedVersion("1.4.0.temporary-redhat-2", Map.of(Qualifier.QUALITY, Set.of())),
                        new QualifiedVersion(
                                "1.4.0.temporary-redhat-1",
                                Map.of(Qualifier.QUALITY, Set.of("TESTED"), Qualifier.PRODUCT, Set.of("RHSSO")))));

        String[] expectedOrder = {
                "1.4.0.temporary-redhat-1",
                "1.4.0.temporary-redhat-3",
                "1.4.0.temporary-redhat-2",
                "1.4.0.redhat-5",
                "1.4.0.redhat-4",
                "1.4.0.redhat-6" };

        expectOrder(versionAnalyzer, expectedOrder, version, versions);
    }

    @Test
    public void testDenyListFullRemovingSuffixes() {
        /**
         * Deny list: QUALITY:DELETE
         */
        VersionStrategy strat = VersionStrategy.from(null, null, "QUALITY:DELETE");
        VersionAnalyzer versionAnalyzer = new VersionAnalyzer(List.of("temporary-redhat", "redhat"), strat);
        String version = "1.4.0";

        List<QualifiedVersion> versions = new ArrayList<>(
                List.of(
                        new QualifiedVersion("1.4.0.redhat-6", Map.of(Qualifier.PRODUCT, Set.of("EAP"))),
                        new QualifiedVersion(
                                "1.4.0.redhat-5",
                                Map.of(Qualifier.QUALITY, Set.of("RELEASED"), Qualifier.PRODUCT, Set.of("RHSSO"))),
                        new QualifiedVersion("1.4.0.redhat-4", Map.of(Qualifier.PRODUCT, Set.of("RHSSO"))),
                        new QualifiedVersion(
                                "1.4.0.temporary-redhat-3",
                                Map.of(Qualifier.QUALITY, Set.of("DELETE"), Qualifier.PRODUCT, Set.of("EAP"))),
                        new QualifiedVersion("1.4.0.temporary-redhat-2", Map.of(Qualifier.QUALITY, Set.of("DELETE"))),
                        new QualifiedVersion(
                                "1.4.0.temporary-redhat-1",
                                Map.of(Qualifier.QUALITY, Set.of("DELETE"), Qualifier.PRODUCT, Set.of("RHSSO")))));

        String[] expectedOrder = { "1.4.0.redhat-6", "1.4.0.redhat-5", "1.4.0.redhat-4" };

        expectOrder(versionAnalyzer, expectedOrder, version, versions);
    }

    @Test
    public void testDenyRemovingVersionsFromBothSuffixes() {
        /**
         * Deny list: QUALITY:DELETE
         */
        VersionStrategy strat = VersionStrategy.from(null, null, "QUALITY:DELETE");
        VersionAnalyzer versionAnalyzer = new VersionAnalyzer(List.of("temporary-redhat", "redhat"), strat);
        String version = "1.4.0";

        List<QualifiedVersion> versions = new ArrayList<>(
                List.of(
                        new QualifiedVersion("1.4.0.redhat-6", Map.of(Qualifier.PRODUCT, Set.of("EAP"))),
                        new QualifiedVersion(
                                "1.4.0.redhat-5",
                                Map.of(Qualifier.QUALITY, Set.of("DELETE"), Qualifier.PRODUCT, Set.of("RHSSO"))),
                        new QualifiedVersion("1.4.0.redhat-4", Map.of(Qualifier.PRODUCT, Set.of("RHSSO"))),
                        new QualifiedVersion(
                                "1.4.0.temporary-redhat-3",
                                Map.of(Qualifier.QUALITY, Set.of("DELETE"), Qualifier.PRODUCT, Set.of("EAP"))),
                        new QualifiedVersion("1.4.0.temporary-redhat-2", Map.of()),
                        new QualifiedVersion(
                                "1.4.0.temporary-redhat-1",
                                Map.of(Qualifier.QUALITY, Set.of("DELETE"), Qualifier.PRODUCT, Set.of("RHSSO")))));

        String[] expectedOrder = { "1.4.0.temporary-redhat-2", "1.4.0.redhat-6", "1.4.0.redhat-4" };

        expectOrder(versionAnalyzer, expectedOrder, version, versions);
    }

    private void expectOrder(
            VersionAnalyzer versionAnalyzer,
            String[] expectedOrder,
            String version,
            List<QualifiedVersion> versions) {
        for (String expected : expectedOrder) {
            checkBMV(versionAnalyzer, expected, version, versions.toArray(new QualifiedVersion[0]));

            // remove best version from list to verify the next one
            var expectedInList = versions.stream().filter(qv -> qv.getVersion().equals(expected)).findFirst().get();
            versions.remove(expectedInList);
        }
    }

}
