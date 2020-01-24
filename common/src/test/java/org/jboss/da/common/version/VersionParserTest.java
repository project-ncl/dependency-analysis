package org.jboss.da.common.version;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class VersionParserTest {

    @Test
    public void testOSGIParser() {
        assertEquals("1.1.0.Final", VersionParser.getOSGiVersion("1.1.Final"));
        assertEquals("1.1.0", VersionParser.getOSGiVersion("1.1"));
        assertEquals("1.1.0.redhat-1", VersionParser.getOSGiVersion("1.1.redhat-1"));
        assertEquals("1.1.0.redhat-1", VersionParser.getOSGiVersion("1.1-redhat-1"));
        assertEquals("1.1.0", VersionParser.getOSGiVersion("1.1"));
        assertEquals("1.1.0.Final", VersionParser.getOSGiVersion("1.1Final"));
        assertEquals("1.1.5.CR4", VersionParser.getOSGiVersion("1.1.5CR4"));
        assertEquals("1.1.0", VersionParser.getOSGiVersion("1.1."));
        assertEquals("1.1.0.CR4", VersionParser.getOSGiVersion("1.1.CR4"));
        assertEquals("1.1.0.final", VersionParser.getOSGiVersion("1.1-final"));
        assertEquals("1.1.0.final-redhat-1", VersionParser.getOSGiVersion("1.1.0.final-redhat-1"));
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
        List<String> versions = Arrays.asList("1.5.8", "1.5.8-patch-01", "1.6.1", "1.7.21.t20180522-115319-991-redhat-1");

        VersionParser vp = new VersionParser("t20180522-115319-991-redhat");
        List<String> filtered = versions.stream().filter(v -> vp.parse(v).isSuffixed()).collect(Collectors.toList());
        assertEquals(1, filtered.size());
        assertTrue(filtered.contains("1.7.21.t20180522-115319-991-redhat-1"));
    }

    @Test
    public void testTemporaryAndRedhatSuffix() {
        List<String> versions = Arrays.asList("1.5.8", "1.5.8-patch-01", "1.6.1", "1.6.1-redhat-1", "1.6.1-redhat-2",
                "1.6.4.redhat-2", "1.7.21.t20180417-125043-536-redhat-1", "1.7.21.t20180425-112559-465-redhat-1",
                "1.7.21.temporary-redhat-1");

        VersionParser vp = new VersionParser("temporary-redhat");
        List<String> filtered = versions.stream().filter(v -> vp.parse(v).isSuffixed()).collect(Collectors.toList());
        assertEquals(6, filtered.size());
        assertTrue(filtered.contains("1.7.21.temporary-redhat-1"));
        assertTrue(filtered.contains("1.6.4.redhat-2"));
        assertTrue(filtered.contains("1.7.21.t20180425-112559-465-redhat-1"));
        assertFalse(filtered.contains("1.5.8-patch-01"));
    }
}
