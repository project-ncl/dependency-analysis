package org.jboss.da.model.rest;

import static org.jboss.da.model.rest.VersionComparator.VersionDifference.EQUAL;
import static org.jboss.da.model.rest.VersionComparator.VersionDifference.MAJOR;
import static org.jboss.da.model.rest.VersionComparator.VersionDifference.MICRO;
import static org.jboss.da.model.rest.VersionComparator.VersionDifference.MINOR;
import static org.jboss.da.model.rest.VersionComparator.VersionDifference.QUALIFIER;
import static org.jboss.da.model.rest.VersionComparator.VersionDifference.RH_SUFFIX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@RunWith(MockitoJUnitRunner.class)
public class VersionComparatorTest {

    @Test
    public void testCompareVersion() {
        VersionComparator vc = new VersionComparator();

        assertTrue(vc.compare("3.4.2.Final", "2.4.2.Final") > 0);
        assertTrue(vc.compare("3.4.2.Final", "3.3.2.Final") > 0);
        assertTrue(vc.compare("3.4.2.Final", "3.4.1.Final") > 0);
        assertTrue(vc.compare("3.4.2.Final", "3.4.2.Beta") > 0);

        assertTrue(vc.compare("3.0.0.Final", "3.Final") == 0);
        assertTrue(vc.compare("3.0.0.Final", "3.0.Final") == 0);
        assertTrue(vc.compare("3.0.0.Final", "3.0.0.Final") == 0);

        assertTrue(vc.compare("3.0.0", "3") == 0);
        assertTrue(vc.compare("3.0.0", "3.0") == 0);
        assertTrue(vc.compare("3.0.0", "3.0.0") == 0);

        assertTrue(vc.compare("3.4.2.Final", "4.4.2.Final") < 0);
        assertTrue(vc.compare("3.4.2.Final", "3.5.2.Final") < 0);
        assertTrue(vc.compare("3.4.2.Final", "3.4.3.Final") < 0);

        assertTrue(vc.compare("3.4.2.Alpha", "3.4.2.Beta") < 0);

        assertTrue(vc.compare("3.4.2.Final", "3.4.2.Final-redhat-1") < 0);
        assertTrue(vc.compare("3.4.2", "3.4.2.redhat-1") < 0);

        assertTrue(vc.compare("3.4.2.Final-redhat-1", "3.4.2.Final-redhat-2") < 0);
        assertTrue(vc.compare("3.4.2.redhat-1", "3.4.2.redhat-2") < 0);
    }

    @Test
    public void testBaseVersion() {
        VersionComparator vc = new VersionComparator("3.4.2.Final");

        assertTrue(vc.compare("3.4.3.Final", "3.4.4.Final") < 0);
        assertTrue(vc.compare("3.4.3.Final", "3.4.1.Final") < 0);
        assertTrue(vc.compare("3.4.99.Final", "3.5.0.Final") < 0);
        assertTrue(vc.compare("3.4.3.Final", "3.4.3.Alpha") < 0);
        assertTrue(vc.compare("3.4.3.Beta", "3.4.3.Alpha") < 0);
        assertTrue(vc.compare("3.4.3.Final", "3.4.2.CR1") < 0);
        assertTrue(vc.compare("3.5.0.Final", "3.4.2.Beta") < 0);
        assertTrue(vc.compare("3.4.2.Alpha", "4.2.3.Final") < 0);
        assertTrue(vc.compare("3.4.4.Final", "4.4.1.Final") < 0);
        assertTrue(vc.compare("3.4.2.Alpha", "4.4.1.Final") < 0);

        vc = new VersionComparator("2.2.2.Beta2");

        assertTrue(vc.compare("2.2.2.Beta4", "2.2.2.Beta3") < 0);
        assertTrue(vc.compare("2.2.2.Beta3", "2.2.2.Beta1") < 0);
        assertTrue(vc.compare("2.2.2.Final", "2.2.2.Alpha") < 0);
        assertTrue(vc.compare("2.2.2.Final", "2.2.2.Alpha") < 0);
        assertTrue(vc.compare("2.3.3.Final", "2.2.2.Alpha") < 0);
        assertTrue(vc.compare("2.2.3.Alpha", "2.2.2.Alpha") < 0);
        assertTrue(vc.compare("2.2.2.Beta3", "2.2.1.Final") < 0);
        assertTrue(vc.compare("2.2.2.CR1", "2.2.1.Final") < 0);
        assertTrue(vc.compare("2.2.2.Final", "2.2.1.Final") < 0);
        assertTrue(vc.compare("2.2.2.Alpha", "2.2.1.Final") < 0);

        vc = new VersionComparator("2.2");

        assertTrue(vc.compare("2.2", "2.2.0") == 0);

        assertTrue(vc.compare("2.2", "2.2-beta-5") < 0);
        assertTrue(vc.compare("2.2", "2.2.0-b21") < 0);
        assertTrue(vc.compare("2.2", "2.2.0-b10") < 0);
        assertTrue(vc.compare("2.2", "2.2.SP4") < 0);
        assertTrue(vc.compare("2.2", "2.2.0.SP1") < 0);
        assertTrue(vc.compare("2.2", "2.2.Final") < 0);
        assertTrue(vc.compare("2.2", "2.2.0.Final") < 0);
        assertTrue(vc.compare("2.2", "2.2.0.CR1") < 0);

        assertTrue(vc.compare("2.2.0", "2.2-beta-5") < 0);
        assertTrue(vc.compare("2.2.0", "2.2.0-b21") < 0);
        assertTrue(vc.compare("2.2.0", "2.2.0-b10") < 0);
        assertTrue(vc.compare("2.2.0", "2.2.SP4") < 0);
        assertTrue(vc.compare("2.2.0", "2.2.0.SP1") < 0);
        assertTrue(vc.compare("2.2.0", "2.2.Final") < 0);
        assertTrue(vc.compare("2.2.0", "2.2.0.Final") < 0);
        assertTrue(vc.compare("2.2.0", "2.2.0.CR1") < 0);

        assertTrue(vc.compare("2.2.0.b21", "2.2.0.b10") < 0);
        assertTrue(vc.compare("2.2.0.SP4", "2.2.0.b21") < 0);
        assertTrue(vc.compare("2.2.0.Final", "2.2.0.SP4") > 0); // Too difficult to implement correctly - the list of special qualifiers (like Final, GA, ...) would be needed
        assertTrue(vc.compare("2.2.0.Final", "2.2.0.CR1") < 0);
        assertTrue(vc.compare("2.2.0.GA", "2.2.0.SP4") > 0); // Too difficult to implement correctly
        assertTrue(vc.compare("2.2.0.GA", "2.2.0.CR1") < 0);
        assertTrue(vc.compare("2.2.0.MR1", "2.2.0.SP4") > 0); // Too difficult to implement correctly
        assertTrue(vc.compare("2.2.0.MR1", "2.2.0.CR1") < 0);
    }

    @Test
    public void testVersionDifference() {
        assertEquals(MAJOR, VersionComparator.difference("3.4.2.Final", "2.4.2.Final"));
        assertEquals(MINOR, VersionComparator.difference("3.4.2.Final", "3.3.2.Final"));
        assertEquals(MICRO, VersionComparator.difference("3.4.2.Final", "3.4.1.Final"));
        assertEquals(QUALIFIER, VersionComparator.difference("3.4.2.Final", "3.4.2.Beta"));

        assertEquals(EQUAL, VersionComparator.difference("3.0.0.Final", "3.Final"));
        assertEquals(EQUAL, VersionComparator.difference("3.0.0.Final", "3.0.Final"));
        assertEquals(EQUAL, VersionComparator.difference("3.0.0.Final", "3.0.0.Final"));

        assertEquals(EQUAL, VersionComparator.difference("3.0.0", "3"));
        assertEquals(EQUAL, VersionComparator.difference("3.0.0", "3.0"));
        assertEquals(EQUAL, VersionComparator.difference("3.0.0", "3.0.0"));

        assertEquals(MAJOR, VersionComparator.difference("3.4.2.Final", "4.4.2.Final"));
        assertEquals(MINOR, VersionComparator.difference("3.4.2.Final", "3.5.2.Final"));
        assertEquals(MICRO, VersionComparator.difference("3.4.2.Final", "3.4.3.Final"));

        assertEquals(QUALIFIER, VersionComparator.difference("3.4.2.Alpha", "3.4.2.Beta"));

        assertEquals(RH_SUFFIX, VersionComparator.difference("3.4.2.Final", "3.4.2.Final-redhat-1"));
        assertEquals(RH_SUFFIX, VersionComparator.difference("3.4.2", "3.4.2.redhat-1"));

        assertEquals(RH_SUFFIX,
                VersionComparator.difference("3.4.2.Final-redhat-1", "3.4.2.Final-redhat-2"));
        assertEquals(RH_SUFFIX, VersionComparator.difference("3.4.2.redhat-1", "3.4.2.redhat-2"));

        assertEquals(QUALIFIER,
                VersionComparator.difference("1.1.0.SP18-redhat-1", "1.1.0.SP17-redhat-1")); // NCL-3208
    }
}
