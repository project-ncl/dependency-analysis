package org.jboss.da.common.version;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class OSGiParserTest {

    @Test
    public void testVersionParser() {
        OSGiVersionParser parser = new OSGiVersionParser();

        assertEquals("1.1.0.Final", parser.getOSGiVersion("1.1.Final"));
        assertEquals("1.1", parser.getOSGiVersion("1.1"));
        assertEquals("1.1.0.redhat-1", parser.getOSGiVersion("1.1.redhat-1"));
        assertEquals("1.1.0.redhat-1", parser.getOSGiVersion("1.1-redhat-1"));
        assertEquals("1.1.0.Final", parser.getOSGiVersion("1.1Final"));
        assertEquals("1.1.5.CR4", parser.getOSGiVersion("1.1.5CR4"));
        assertEquals("1.1.0", parser.getOSGiVersion("1.1."));
        assertEquals("1.1.0.CR4", parser.getOSGiVersion("1.1.CR4"));
    }
}
