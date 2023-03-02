package org.jboss.da.common.version;

import org.jboss.da.lookup.model.VersionDistanceRule;
import org.junit.Test;

import static org.jboss.da.common.version.VersionComparator.VersionDifference.EQUAL;
import static org.jboss.da.common.version.VersionComparator.VersionDifference.MAJOR;
import static org.jboss.da.common.version.VersionComparator.VersionDifference.MICRO;
import static org.jboss.da.common.version.VersionComparator.VersionDifference.MINOR;
import static org.jboss.da.common.version.VersionComparator.VersionDifference.QUALIFIER;
import static org.jboss.da.common.version.VersionComparator.VersionDifference.RH_SUFFIX;
import static org.jboss.da.common.version.VersionComparator.VersionDifference.SUFFIX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public class VersionComparatorTest {

    private static final VersionParser VERSION_PARSER = new VersionParser("redhat");

    @Test
    public void testCompareVersion() {
        VersionComparator vc = new VersionComparator(VERSION_PARSER);

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

        assertTrue(vc.compare("b10", "0.0.0.b10") == 0);

        assertTrue(vc.compare("3.4.2.Final", "4.4.2.Final") < 0);
        assertTrue(vc.compare("3.4.2.Final", "3.5.2.Final") < 0);
        assertTrue(vc.compare("3.4.2.Final", "3.4.3.Final") < 0);

        assertTrue(vc.compare("3.4.2.Alpha", "3.4.2.Beta") < 0);

        assertTrue(vc.compare("3.4.2.Final", "3.4.2.Final-redhat-1") < 0);
        assertTrue(vc.compare("3.4.2", "3.4.2.redhat-1") < 0);

        assertTrue(vc.compare("3.4.2.Final-redhat-1", "3.4.2.Final-redhat-2") < 0);
        assertTrue(vc.compare("3.4.2.redhat-1", "3.4.2.redhat-2") < 0);

        // These are equivalent, but OSGI version is preferred (NCL-4532)
        assertTrue(vc.compare("3-redhat-2", "3.0.0.redhat-2") < 0);
        assertTrue(vc.compare("3.redhat-2", "3.0.0.redhat-2") < 0);
        assertTrue(vc.compare("3.0-redhat-2", "3.0.0.redhat-2") < 0);
        assertTrue(vc.compare("3.0.redhat-2", "3.0.0.redhat-2") < 0);
        assertTrue(vc.compare("3.0.0-redhat-2", "3.0.0.redhat-2") < 0);
    }

    @Test
    public void testCompareVersionWithMultipleSuffixes() {
        VersionComparator vc1 = new VersionComparator(new VersionParser("temporary-redhat", "redhat"));
        VersionComparator vc2 = new VersionComparator(new VersionParser("redhat", "temporary-redhat"));

        assertTrue(vc1.compare("3.0.0.Final", "3.0.0.Final-redhat-1") < 0);
        assertTrue(vc1.compare("3.0.0.Final", "3.0.0.Final-temporary-redhat-1") < 0);
        assertTrue(vc1.compare("3.0.0.Final-redhat-1", "3.0.0.Final-temporary-redhat-1") < 0);
        assertTrue(vc2.compare("3.0.0.Final-redhat-1", "3.0.0.Final-temporary-redhat-1") < 0);
    }

    @Test
    public void testBaseVersion() {
        VersionComparator vc = new VersionComparator("3.4.2.Final", VERSION_PARSER);

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

        vc = new VersionComparator("2.2.2.Beta2", VERSION_PARSER);

        assertTrue(vc.compare("2.2.2.Beta4", "2.2.2.Beta3") < 0);
        assertTrue(vc.compare("2.2.2.Beta3", "2.2.2.Beta1") < 0);
        assertTrue(vc.compare("2.2.2.Final", "2.2.2.Alpha") < 0);
        assertTrue(vc.compare("2.3.3.Final", "2.2.2.Alpha") < 0);
        assertTrue(vc.compare("2.2.3.Alpha", "2.2.2.Alpha") < 0);
        assertTrue(vc.compare("2.2.2.Beta3", "2.2.1.Final") < 0);
        assertTrue(vc.compare("2.2.2.CR1", "2.2.1.Final") < 0);
        assertTrue(vc.compare("2.2.2.Final", "2.2.1.Final") < 0);
        assertTrue(vc.compare("2.2.2.Alpha", "2.2.1.Final") < 0);

        vc = new VersionComparator("2.2", VERSION_PARSER);

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
        assertTrue(vc.compare("2.2.0.Final", "2.2.0.SP4") > 0); // Too difficult to implement correctly - the list of
                                                                // special
                                                                // qualifiers (like Final, GA, ...) would be needed
        assertTrue(vc.compare("2.2.0.Final", "2.2.0.CR1") < 0);
        assertTrue(vc.compare("2.2.0.GA", "2.2.0.SP4") > 0); // Too difficult to implement correctly
        assertTrue(vc.compare("2.2.0.GA", "2.2.0.CR1") < 0);
        assertTrue(vc.compare("2.2.0.MR1", "2.2.0.SP4") > 0); // Too difficult to implement correctly
        assertTrue(vc.compare("2.2.0.MR1", "2.2.0.CR1") < 0);
    }

    @Test
    public void testBaseVersionWithClosestByPartsRule() {
        VersionComparator vc = new VersionComparator("2.0.0", VersionDistanceRule.CLOSEST_BY_PARTS, VERSION_PARSER);

        assertTrue(vc.compare("2.0.1", "3.0.0") < 0); // Rule 1 - less different parts
        assertTrue(vc.compare("3.0.0", "4.0.0") < 0); // Rule 2a - lesser difference in differing part
        assertTrue(vc.compare("1.0.0", "0.0.0") < 0);
        assertTrue(vc.compare("3.0.0", "1.0.0") < 0); // Rule 2b - same difference in differing part, but higher
        assertTrue(vc.compare("3.0.0", "3.0.1") < 0); // Rule 3 - closer to base by order
        assertTrue(vc.compare("1.0.1", "1.0.0") < 0); // Rule 3 - closer to base by order

        vc = new VersionComparator("3.4.2.Final", VersionDistanceRule.CLOSEST_BY_PARTS, VERSION_PARSER);

        assertTrue(vc.compare("3.4.3.Final", "3.4.4.Final") < 0);
        assertTrue(vc.compare("3.4.3.Final", "3.4.1.Final") < 0);
        assertTrue(vc.compare("3.4.4.Final", "3.4.1.Final") > 0); // 1 is close to 2 then 4
        assertTrue(vc.compare("3.4.99.Final", "3.5.0.Final") < 0);
        assertTrue(vc.compare("3.4.3.Final", "3.4.3.Alpha") < 0);
        assertTrue(vc.compare("3.4.3.Beta", "3.4.3.Alpha") < 0);
        assertTrue(vc.compare("3.4.3.Final", "3.4.2.CR1") > 0); // Right one matches in micro
        assertTrue(vc.compare("3.5.0.Final", "3.4.2.Beta") > 0); // Right one matches in minor
        assertTrue(vc.compare("3.4.2.Alpha", "4.2.3.Final") < 0);
        assertTrue(vc.compare("3.4.4.Final", "4.4.1.Final") < 0);
        assertTrue(vc.compare("3.4.2.Alpha", "4.4.1.Final") < 0);

        vc = new VersionComparator("2.2.2.Beta2", VersionDistanceRule.CLOSEST_BY_PARTS, VERSION_PARSER);

        assertTrue(vc.compare("2.2.2.Beta4", "2.2.2.Beta3") < 0);
        assertTrue(vc.compare("2.2.2.Beta3", "2.2.2.Beta1") < 0);
        assertTrue(vc.compare("2.2.2.Final", "2.2.2.Alpha") < 0);
        assertTrue(vc.compare("2.3.3.Final", "2.2.2.Alpha") > 0); // Right one matches in minor
        assertTrue(vc.compare("2.2.3.Alpha", "2.2.2.Alpha") > 0); // Right one matches in micro
        assertTrue(vc.compare("2.2.2.Beta3", "2.2.1.Final") < 0);
        assertTrue(vc.compare("2.2.2.CR1", "2.2.1.Final") < 0);
        assertTrue(vc.compare("2.2.2.Final", "2.2.1.Final") < 0);
        assertTrue(vc.compare("2.2.2.Alpha", "2.2.1.Final") < 0);

        assertTrue(vc.compare("3.3.3.Final", "3.3.3.Final") == 0);
        assertTrue(vc.compare("3.3.3.Final", "3.3.3.Alpha") < 0);
        assertTrue(vc.compare("3.3.2.Final", "3.3.3.Final") < 0);
        assertTrue(vc.compare("3.3.0.Final", "3.3.2.Final") < 0);
        assertTrue(vc.compare("3.3.1.Final", "3.3.3.Final") < 0);
        assertTrue(vc.compare("3.3.0.Final", "3.3.0.Final") == 0);
        assertTrue(vc.compare("3.2.0.Final", "3.3.0.Final") < 0);
        assertTrue(vc.compare("3.0.0.Final", "3.3.0.Final") < 0);
        assertTrue(vc.compare("3.1.0.Final", "3.3.0.Final") < 0);
        assertTrue(vc.compare("13.0.0.Final", "13.0.0.Final") == 0);
        assertTrue(vc.compare("12.0.0.Final", "13.0.0.Final") < 0);
        assertTrue(vc.compare("2.0.0.Final", "13.0.0.Final") < 0);
        assertTrue(vc.compare("1.0.0.Final", "3.0.0.Final") > 0);
        assertTrue(vc.compare("1.1.1.Final", "1.1.1.Final") == 0);
        assertTrue(vc.compare("1.1.1.Final", "1.1.1.Alpha") < 0);
        assertTrue(vc.compare("1.1.1.Final", "1.1.2.Final") > 0);
        assertTrue(vc.compare("1.1.1.Final", "1.1.3.Final") > 0);
        assertTrue(vc.compare("1.1.1.Final", "1.1.4.Final") > 0);
        assertTrue(vc.compare("1.1.0.Final", "1.1.0.Final") == 0);
        assertTrue(vc.compare("1.1.0.Final", "1.2.0.Final") > 0);
        assertTrue(vc.compare("1.1.0.Final", "1.3.0.Final") > 0);
        assertTrue(vc.compare("1.1.0.Final", "1.4.0.Final") > 0);

        vc = new VersionComparator("2.2", VersionDistanceRule.CLOSEST_BY_PARTS, VERSION_PARSER);

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
        assertTrue(vc.compare("2.2.0.Final", "2.2.0.SP4") > 0); // Too difficult to implement correctly - the list of
                                                                // special
                                                                // qualifiers (like Final, GA, ...) would be needed
        assertTrue(vc.compare("2.2.0.Final", "2.2.0.CR1") < 0);
        assertTrue(vc.compare("2.2.0.GA", "2.2.0.SP4") > 0); // Too difficult to implement correctly
        assertTrue(vc.compare("2.2.0.GA", "2.2.0.CR1") < 0);
        assertTrue(vc.compare("2.2.0.MR1", "2.2.0.SP4") > 0); // Too difficult to implement correctly
        assertTrue(vc.compare("2.2.0.MR1", "2.2.0.CR1") < 0);
    }

    @Test
    public void testBaseVersionWithMultipleSuffixes() {
        VersionComparator vc1 = new VersionComparator("3.4.2.Final", new VersionParser("temporary-redhat", "redhat"));

        assertTrue(vc1.compare("3.4.2.Final", "3.4.2.Final-redhat-1") < 0);
        assertTrue(vc1.compare("3.4.2.Final-redhat-2", "3.4.2.Final-redhat-1") < 0);
        assertTrue(vc1.compare("3.4.2.Final", "3.4.2.Final-temporary-redhat-1") < 0);
        assertTrue(vc1.compare("3.4.2.Final-temporary-redhat-2", "3.4.2.Final-temporary-redhat-1") < 0);
        assertTrue(vc1.compare("3.4.3.Final", "3.4.1.Final") < 0);
        assertTrue(vc1.compare("3.4.99.Final", "3.5.0.Final") < 0);
        assertTrue(vc1.compare("3.4.3.Final", "3.4.3.Alpha") < 0);
    }

    @Test
    public void testVersionDifference() {
        VersionComparator vc = new VersionComparator(VERSION_PARSER);
        assertEquals(MAJOR, vc.difference("3.4.2.Final", "2.4.2.Final"));
        assertEquals(MINOR, vc.difference("3.4.2.Final", "3.3.2.Final"));
        assertEquals(MICRO, vc.difference("3.4.2.Final", "3.4.1.Final"));
        assertEquals(QUALIFIER, vc.difference("3.4.2.Final", "3.4.2.Beta"));

        assertEquals(EQUAL, vc.difference("3.0.0.Final", "3.Final"));
        assertEquals(EQUAL, vc.difference("3.0.0.Final", "3.0.Final"));
        assertEquals(EQUAL, vc.difference("3.0.0.Final", "3.0.0.Final"));

        assertEquals(EQUAL, vc.difference("3.0.0", "3"));
        assertEquals(EQUAL, vc.difference("3.0.0", "3.0"));
        assertEquals(EQUAL, vc.difference("3.0.0", "3.0.0"));

        assertEquals(MAJOR, vc.difference("3.4.2.Final", "4.4.2.Final"));
        assertEquals(MINOR, vc.difference("3.4.2.Final", "3.5.2.Final"));
        assertEquals(MICRO, vc.difference("3.4.2.Final", "3.4.3.Final"));

        assertEquals(QUALIFIER, vc.difference("3.4.2.Alpha", "3.4.2.Beta"));

        assertEquals(SUFFIX, vc.difference("3.4.2.Final", "3.4.2.Final-redhat-1"));
        assertEquals(SUFFIX, vc.difference("3.4.2", "3.4.2.redhat-1"));

        assertEquals(RH_SUFFIX, vc.difference("3.4.2.Final-redhat-1", "3.4.2.Final-redhat-2"));
        assertEquals(RH_SUFFIX, vc.difference("3.4.2.redhat-1", "3.4.2.redhat-2"));

        assertEquals(QUALIFIER, vc.difference("1.1.0.SP18-redhat-1", "1.1.0.SP17-redhat-1")); // NCL-3208
    }

    @Test
    public void testBaseVersionWithClosestByPartsRuleReproduceNCLSUP898() {
        VersionComparator vc = new VersionComparator(
                "8.34.0.Final",
                VersionDistanceRule.CLOSEST_BY_PARTS,
                VERSION_PARSER);

        assertTrue(vc.compare("8.24.1.Beta-redhat-00006", "8.24.1.Beta-redhat-00005") < 0);
        assertTrue(vc.compare("8.24.1.Beta-redhat-00006", "8.24.0.Beta-redhat-00004") < 0);
        assertTrue(vc.compare("8.27.0.Beta-redhat-00005", "8.24.1.Beta-redhat-00006") < 0);
        assertTrue(vc.compare("8.32.0.Final-redhat-00004", "8.24.1.Beta-redhat-00006") < 0);
        assertTrue(vc.compare("8.33.0.Final-redhat-00003", "8.24.1.Beta-redhat-00006") < 0);
        assertTrue(vc.compare("8.24.1.Beta-redhat-00006", "6.5.0.Final-redhat-27") < 0);

    }
}
