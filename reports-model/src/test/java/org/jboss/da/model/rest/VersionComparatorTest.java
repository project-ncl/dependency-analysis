package org.jboss.da.model.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public class VersionComparatorTest {

    @Test
    public void testCompareVersion() {
        DummyVersionComparator vc = new DummyVersionComparator();

        assertTrue(vc.compare("3.4.2.Final", "2.4.2.Final") > 0);
        assertTrue(vc.compare("3.4.2.Final", "3.3.2.Final") > 0);
        assertTrue(vc.compare("3.4.2.Final", "3.4.1.Final") > 0);
        assertTrue(vc.compare("3.4.2.Final", "3.4.2.Beta") > 0);

        assertEquals(0, vc.compare("3.0.0.Final", "3.Final"));
        assertEquals(0, vc.compare("3.0.0.Final", "3.0.Final"));
        // noinspection EqualsWithItself
        assertEquals(0, vc.compare("3.0.0.Final", "3.0.0.Final"));

        assertEquals(0, vc.compare("3.0.0", "3"));
        assertEquals(0, vc.compare("3.0.0", "3.0"));
        // noinspection EqualsWithItself
        assertEquals(0, vc.compare("3.0.0", "3.0.0"));

        assertEquals(0, vc.compare("b10", "0.0.0.b10"));

        assertTrue(vc.compare("3.4.2.Final", "4.4.2.Final") < 0);
        assertTrue(vc.compare("3.4.2.Final", "3.5.2.Final") < 0);
        assertTrue(vc.compare("3.4.2.Final", "3.4.3.Final") < 0);

        assertTrue(vc.compare("3.4.2.Alpha", "3.4.2.Beta") < 0);

        assertTrue(vc.compare("3.4.2.Final", "3.4.2.Final-redhat-1") < 0);
        assertTrue(vc.compare("3.4.2", "3.4.2.redhat-1") < 0);

        assertTrue(vc.compare("3.4.2.Final-redhat-1", "3.4.2.Final-redhat-2") < 0);
        assertTrue(vc.compare("3.4.2.redhat-1", "3.4.2.redhat-2") < 0);
    }
}
