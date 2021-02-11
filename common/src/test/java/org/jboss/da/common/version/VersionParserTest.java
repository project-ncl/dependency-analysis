package org.jboss.da.common.version;

import org.junit.Test;

import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class VersionParserTest {

    @Test
    public void testOSGIParser() {
        assertEquals("1.1.0.Final", VersionParser.getOSGiVersion("1.1.Final"));
        assertEquals("1.1.0", VersionParser.getOSGiVersion("1.1"));
        assertEquals("1.1.0.redhat-1", VersionParser.getOSGiVersion("1.1.redhat-1"));
        assertEquals("1.1.0.redhat-1", VersionParser.getOSGiVersion("1.1-redhat-1"));
        assertEquals("1.1.0.Final", VersionParser.getOSGiVersion("1.1Final"));
        assertEquals("1.1.5.CR4", VersionParser.getOSGiVersion("1.1.5CR4"));
        assertEquals("1.1.0", VersionParser.getOSGiVersion("1.1."));
        assertEquals("1.1.0.CR4", VersionParser.getOSGiVersion("1.1.CR4"));
        assertEquals("1.1.0.final", VersionParser.getOSGiVersion("1.1-final"));
        assertEquals("1.1.0.final-redhat-1", VersionParser.getOSGiVersion("1.1.0.final-redhat-1"));
        assertEquals("1.1.2", VersionParser.getOSGiVersion("1.1-2"));
        assertEquals("1.1.2.Final", VersionParser.getOSGiVersion("1.1-2.Final"));
        assertEquals("1.1.2.Final", VersionParser.getOSGiVersion("1.1-2-Final"));
        assertEquals("1.1.2.Final-redhat-2", VersionParser.getOSGiVersion("1.1-2.Final-redhat-2"));
        assertEquals("1.1.2.Final-redhat-2", VersionParser.getOSGiVersion("1.1-2-Final-redhat-2"));
    }

    @Test
    public void testVersionParser() {
        VersionParser versionParser = new VersionParser("redhat");
        assertEquals(new SuffixedVersion(1, 1, 0, "Final", "1.1.Final"), versionParser.parse("1.1.Final"));
        assertEquals(new SuffixedVersion(1, 1, 0, "", "1.1"), versionParser.parse("1.1"));
        assertEquals(
                new SuffixedVersion(1, 1, 0, "", "redhat", 1, "1.1.redhat-1"),
                versionParser.parse("1.1.redhat-1"));
        assertEquals(
                new SuffixedVersion(1, 1, 0, "", "redhat", 1, "1.1-redhat-1"),
                versionParser.parse("1.1-redhat-1"));
        assertEquals(new SuffixedVersion(1, 1, 0, "Final", "1.1Final"), versionParser.parse("1.1Final"));
        assertEquals(new SuffixedVersion(1, 1, 5, "CR4", "1.1.5CR4"), versionParser.parse("1.1.5CR4"));
        assertEquals(new SuffixedVersion(1, 1, 0, "", "1.1."), versionParser.parse("1.1."));
        assertEquals(new SuffixedVersion(1, 1, 0, "CR4", "1.1.CR4"), versionParser.parse("1.1.CR4"));
        assertEquals(new SuffixedVersion(1, 1, 0, "final", "1.1-final"), versionParser.parse("1.1-final"));
        assertEquals(
                new SuffixedVersion(1, 1, 0, "final", "redhat", 1, "1.1.0.final-redhat-1"),
                versionParser.parse("1.1.0.final-redhat-1"));
        assertEquals(new SuffixedVersion(1, 1, 2, "", "1.1-2"), versionParser.parse("1.1-2"));
        assertEquals(new SuffixedVersion(1, 1, 2, "Final", "1.1-2.Final"), versionParser.parse("1.1-2.Final"));
        assertEquals(new SuffixedVersion(1, 1, 2, "Final", "1.1-2-Final"), versionParser.parse("1.1-2-Final"));
        assertEquals(
                new SuffixedVersion(1, 1, 2, "Final", "redhat", 2, "1.1-2.Final-redhat-2"),
                versionParser.parse("1.1-2.Final-redhat-2"));
        assertEquals(
                new SuffixedVersion(1, 1, 2, "Final", "redhat", 2, "1.1-2-Final-redhat-2"),
                versionParser.parse("1.1-2-Final-redhat-2"));
    }

    @Test
    public void testRedhatVersion() {
        VersionParser versionParser = new VersionParser("redhat");
        assertFalse(versionParser.parse("1.1.Final").isSuffixed());
        assertFalse(versionParser.parse("1.1").isSuffixed());
        assertTrue(versionParser.parse("1.1.redhat-1").isSuffixed());
        assertTrue(versionParser.parse("1.1-redhat-1").isSuffixed());
        assertFalse(versionParser.parse("1.1").isSuffixed());
        assertFalse(versionParser.parse("1.1Final").isSuffixed());
        assertFalse(versionParser.parse("1.1.5CR4").isSuffixed());
        assertFalse(versionParser.parse("1.1.").isSuffixed());
        assertFalse(versionParser.parse("1.1.CR4").isSuffixed());
        assertFalse(versionParser.parse("1.1-final").isSuffixed());
        assertTrue(versionParser.parse("1.1.0.final-redhat-1").isSuffixed());
        assertTrue(versionParser.parse("1.1-redhat-001").isSuffixed());
    }

    @Test
    public void testTemporarySuffix() {
        List<String> versions = Arrays
                .asList("1.5.8", "1.5.8-patch-01", "1.6.1", "1.7.21.t20180522-115319-991-redhat-1");

        VersionParser vp = new VersionParser("t20180522-115319-991-redhat");
        List<String> filtered = versions.stream().filter(v -> vp.parse(v).isSuffixed()).collect(Collectors.toList());
        assertEquals(1, filtered.size());
        assertTrue(filtered.contains("1.7.21.t20180522-115319-991-redhat-1"));
    }

    @Test
    public void testTemporaryAndRedhatSuffix() {
        List<String> versions = Arrays.asList(
                "1.5.8",
                "1.5.8-patch-01",
                "1.6.1",
                "1.6.1-redhat-1",
                "1.6.1-redhat-2",
                "1.6.4.redhat-2",
                "1.7.21.t20180417-125043-536-redhat-1",
                "1.7.21.t20180425-112559-465-redhat-1",
                "1.7.21.temporary-redhat-1");

        VersionParser vp = new VersionParser("temporary-redhat", "redhat");
        List<String> filtered = versions.stream().filter(v -> vp.parse(v).isSuffixed()).collect(Collectors.toList());
        assertEquals(6, filtered.size());
        assertTrue(filtered.contains("1.7.21.temporary-redhat-1"));
        assertTrue(filtered.contains("1.6.4.redhat-2"));
        assertTrue(filtered.contains("1.7.21.t20180425-112559-465-redhat-1"));
        assertFalse(filtered.contains("1.5.8-patch-01"));
    }

    @Test
    public void shuldNormalizeVersionWhenParsing() {
        VersionParser vp1 = new VersionParser("A", "B-A", "B-C-A");
        assertEquals(new SuffixedVersion(1, 5, 8, "", "1.5.8"), vp1.parse("1.5.8"));
        assertEquals(new SuffixedVersion(1, 5, 8, "", "A", 2, "1.5.8.A-2"), vp1.parse("1.5.8.A-2"));
        assertEquals(new SuffixedVersion(1, 5, 8, "B-2", "1.5.8.B-2"), vp1.parse("1.5.8.B-2"));
        assertEquals(new SuffixedVersion(1, 5, 8, "", "B-A", 2, "1.5.8.B-A-2"), vp1.parse("1.5.8.B-A-2"));
        assertEquals(new SuffixedVersion(1, 5, 8, "C", "A", 2, "1.5.8.B-A-2"), vp1.parse("1.5.8.C-A-2"));
        assertEquals(new SuffixedVersion(1, 5, 8, "", "B-C-A", 2, "1.5.8.B-C-A-2"), vp1.parse("1.5.8.B-C-A-2"));

        VersionParser vp2 = new VersionParser("B-C-A", "B-A", "A");
        assertEquals(new SuffixedVersion(1, 5, 8, "", "1.5.8"), vp2.parse("1.5.8"));
        assertEquals(new SuffixedVersion(1, 5, 8, "", "A", 2, "1.5.8.A-2"), vp2.parse("1.5.8.A-2"));
        assertEquals(new SuffixedVersion(1, 5, 8, "B-2", "1.5.8.B-2"), vp2.parse("1.5.8.B-2"));
        assertEquals(new SuffixedVersion(1, 5, 8, "", "B-A", 2, "1.5.8.B-A-2"), vp2.parse("1.5.8.B-A-2"));
        assertEquals(new SuffixedVersion(1, 5, 8, "C", "A", 2, "1.5.8.B-A-2"), vp2.parse("1.5.8.C-A-2"));
        assertEquals(new SuffixedVersion(1, 5, 8, "", "B-C-A", 2, "1.5.8.B-C-A-2"), vp2.parse("1.5.8.B-C-A-2"));
    }

    @Test
    public void shouldParseMultipleSuffixes() {
        VersionParser vp1 = new VersionParser("A", "B-A", "B-C-A");

        assertTrue(vp1.parseSuffixed("1.5.8").isEmpty());

        String version1 = "1.5.8.A-2";
        Set<SuffixedVersion> set1 = vp1.parseSuffixed(version1);
        assertEquals(1, set1.size());
        assertEquals(singleton(new SuffixedVersion(1, 5, 8, "", "A", 2, version1)), set1);

        assertTrue(vp1.parseSuffixed("1.5.8.B-2").isEmpty());

        String version2 = "1.5.8.B-A-2";
        Set<SuffixedVersion> set2 = vp1.parseSuffixed(version2);
        assertEquals(2, set2.size());
        Set<SuffixedVersion> expected2 = new HashSet<>(
                Arrays.asList(
                        new SuffixedVersion(1, 5, 8, "B", "A", 2, version2),
                        new SuffixedVersion(1, 5, 8, "", "B-A", 2, version2)));
        assertEquals(expected2, set2);

        String version3 = "1.5.8.C-A-2";
        Set<SuffixedVersion> set3 = vp1.parseSuffixed(version3);
        assertEquals(1, set3.size());
        assertEquals(singleton(new SuffixedVersion(1, 5, 8, "C", "A", 2, version3)), set3);

        String version4 = "1.5.8.B-C-A-2";
        Set<SuffixedVersion> set4 = vp1.parseSuffixed(version4);
        assertEquals(2, set4.size());
        Set<SuffixedVersion> expected4 = new HashSet<>(
                Arrays.asList(
                        new SuffixedVersion(1, 5, 8, "B-C", "A", 2, version4),
                        new SuffixedVersion(1, 5, 8, "", "B-C-A", 2, version4)));
        assertEquals(expected4, set4);
    }
}
