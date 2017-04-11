package org.jboss.da.common.version;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OSGiParserTest {

    @Test
    public void testVersionParser() {
        VersionParser parser = new VersionParser();

        assertEquals("1.1.0.Final", parser.getOSGiVersion("1.1.Final"));
        assertEquals("1.1.0", parser.getOSGiVersion("1.1"));
        assertEquals("1.1.0.redhat-1", parser.getOSGiVersion("1.1.redhat-1"));
        assertEquals("1.1.0.redhat-1", parser.getOSGiVersion("1.1-redhat-1"));
        assertEquals("1.1.0", parser.getOSGiVersion("1.1"));
        assertEquals("1.1.0.Final", parser.getOSGiVersion("1.1Final"));
        assertEquals("1.1.5.CR4", parser.getOSGiVersion("1.1.5CR4"));
        assertEquals("1.1.0", parser.getOSGiVersion("1.1."));
        assertEquals("1.1.0.CR4", parser.getOSGiVersion("1.1.CR4"));
        assertEquals("1.1.0.final", parser.getOSGiVersion("1.1-final"));
        assertEquals("1.1.0.final-redhat-1", parser.getOSGiVersion("1.1.0.final-redhat-1"));
    }

    @Test
    public void testRedhatVersion() {
        assertFalse(VersionParser.isRedhatVersion("1.1.Final"));
        assertFalse(VersionParser.isRedhatVersion("1.1"));
        assertTrue(VersionParser.isRedhatVersion("1.1.redhat-1"));
        assertTrue(VersionParser.isRedhatVersion("1.1-redhat-1"));
        assertFalse(VersionParser.isRedhatVersion("1.1"));
        assertFalse(VersionParser.isRedhatVersion("1.1Final"));
        assertFalse(VersionParser.isRedhatVersion("1.1.5CR4"));
        assertFalse(VersionParser.isRedhatVersion("1.1."));
        assertFalse(VersionParser.isRedhatVersion("1.1.CR4"));
        assertFalse(VersionParser.isRedhatVersion("1.1-final"));
        assertTrue(VersionParser.isRedhatVersion("1.1.0.final-redhat-1"));
        assertTrue(VersionParser.isRedhatVersion("1.1-redhat-001"));
    }

}
