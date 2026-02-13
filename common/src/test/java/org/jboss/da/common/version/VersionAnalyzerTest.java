package org.jboss.da.common.version;

import org.jboss.da.common.CommunicationException;
import org.jboss.pnc.api.dependencyanalyzer.dto.QualifiedVersion;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

}
