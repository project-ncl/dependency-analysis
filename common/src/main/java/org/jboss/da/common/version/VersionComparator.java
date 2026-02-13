package org.jboss.da.common.version;

import org.jboss.da.lookup.model.VersionDistanceRule;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

import static org.jboss.da.common.version.VersionComparator.VersionDifference.MAJOR;
import static org.jboss.da.common.version.VersionComparator.VersionDifference.MICRO;
import static org.jboss.da.common.version.VersionComparator.VersionDifference.MINOR;
import static org.jboss.da.common.version.VersionComparator.VersionDifference.QUALIFIER;
import static org.jboss.da.common.version.VersionComparator.VersionDifference.RH_SUFFIX;
import static org.jboss.da.common.version.VersionComparator.VersionDifference.SUFFIX;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public class VersionComparator implements Comparator<String>, Serializable {

    private final VersionDistanceRule distanceRule;

    public enum VersionDifference {
        MAJOR, MINOR, MICRO, QUALIFIER, SUFFIX, RH_SUFFIX, EQUAL;
    }

    private final SuffixedVersion base;

    private final VersionParser versionParser;

    /**
     * Returns classic comparator for versions.
     * 
     * @param versionParser Parser that will be used to parse the version.
     */
    public VersionComparator(VersionParser versionParser) {
        this.base = null;
        this.distanceRule = null;
        this.versionParser = Objects.requireNonNull(versionParser);
    }

    /**
     * Returns comparator that compares versions by distance to the base version. The
     * {@link VersionDistanceRule#RECOMMENDED_REPLACEMENT} rule for computing the distance to base version is used.
     * 
     * @param base The base version.
     * @param versionParser Parser that will be used to parse the version.
     */
    public VersionComparator(String base, VersionParser versionParser) {
        this(base, VersionDistanceRule.RECOMMENDED_REPLACEMENT, versionParser);
    }

    /**
     * Returns comparator that compares versions by distance to the base version.
     *
     * @param base The base version.
     * @param distanceRule The rule that is used to compute the distance to base version.
     * @param versionParser Parser that will be used to parse the version.
     */
    public VersionComparator(String base, VersionDistanceRule distanceRule, VersionParser versionParser) {
        this.versionParser = Objects.requireNonNull(versionParser);
        this.base = versionParser.parse(Objects.requireNonNull(base));
        this.distanceRule = Objects.requireNonNull(distanceRule);
    }

    /**
     * Return information about first part of version that is different.
     */
    public VersionDifference difference(String version1, String version2) {
        SuffixedVersion v1 = versionParser.parse(version1);
        SuffixedVersion v2 = versionParser.parse(version2);
        return difference(v1, v2);
    }

    /**
     * Return information about first part of version that is different.
     */
    public VersionDifference difference(SuffixedVersion v1, SuffixedVersion v2) {
        if (v1.getMajor() != v2.getMajor()) {
            return MAJOR;
        }
        if (v1.getMinor() != v2.getMinor()) {
            return MINOR;
        }
        if (v1.getMicro() != v2.getMicro()) {
            return VersionDifference.MICRO;
        }
        if (!v1.getQualifier().equals(v2.getQualifier())) {
            return QUALIFIER;
        }
        if (!v1.getSuffix().equals(v2.getSuffix())) {
            return SUFFIX;
        }
        if (!v1.getSuffixVersion().equals(v2.getSuffixVersion())) {
            return RH_SUFFIX;
        }
        return VersionDifference.EQUAL;
    }

    /**
     * Return information about first part of version that is different, compared to the base version.
     */
    public VersionDifference difference(SuffixedVersion version) {
        return difference(base, version);
    }

    @Override
    public int compare(String version1, String version2) {
        SuffixedVersion v1 = versionParser.parse(version1);
        SuffixedVersion v2 = versionParser.parse(version2);
        int r = v1.compareTo(v2);
        if (r == 0 || base == null) {
            return r;
        } else {
            switch (distanceRule) {
                case RECOMMENDED_REPLACEMENT:
                    return compareAsRecommendedReplacement(v1, v2);
                case CLOSEST_BY_PARTS:
                    return compareAsClosestByParts(v1, v2);
                default:
                    throw new UnsupportedOperationException("Unknown distance rule " + distanceRule);
            }
        }
    }

    // Assuming different versions
    // Return -1 - v1 is closer to the base version
    // Return 1 - v2 is closer to the base version
    private int compareAsRecommendedReplacement(SuffixedVersion v1, SuffixedVersion v2) {
        // If one of the versions is the same as base, it is closer.
        if (base.equals(v1)) {
            return -1;
        }
        if (base.equals(v2)) {
            return 1;
        }

        // If one version differs from the base in major, the other is closer no matter what
        if (v1.getMajor() != v2.getMajor()) {
            if (v1.getMajor() == base.getMajor()) {
                return -1;
            }
            if (v2.getMajor() == base.getMajor()) {
                return 1;
            }
        }

        // Sort the 3 versions
        SuffixedVersion[] versions = { base, v1, v2 };
        Arrays.sort(versions);

        if (versions[0] == base) { // v1 and v2 are greater than base, use the lowest of them
            int candidate;
            if (versions[1] == v1) {
                candidate = -1;
            } else {
                candidate = 1;
            }

            // if differs only in qualifier, higher qualifier preferred
            if (v1.getMajor() == v2.getMajor() && v1.getMinor() == v2.getMinor() && v1.getMicro() == v2.getMicro()) {
                candidate *= -1;
            }
            return candidate;
        } else if (versions[2] == base) { // v1 and v2 are lower than base, use the greatest of them
            if (versions[1] == v1) {
                return -1;
            } else {
                return 1;
            }
        } else { // one is lower than base, second is greater than base. Use the greater.
            if (versions[2] == v1) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    // Assuming different versions
    // Return -1 - v1 is closer to the base version
    // Return 1 - v2 is closer to the base version
    private int compareAsClosestByParts(SuffixedVersion v1, SuffixedVersion v2) {
        // If one of the versions is the same as base, it is closer.
        if (base.equals(v1)) {
            return -1;
        }
        if (base.equals(v2)) {
            return 1;
        }

        VersionDifference difference = difference(v1, v2);
        VersionDifference differenceV1ToBase = difference(base, v1);
        VersionDifference differenceV2ToBase = difference(base, v2);

        // Rule 1
        if (differenceV1ToBase != differenceV2ToBase) {
            switch (difference) {
                case MAJOR:
                    return differenceV1ToBase == MAJOR ? 1 : -1;
                case MINOR:
                    return differenceV1ToBase == MINOR ? 1 : -1;
                case MICRO:
                    return differenceV1ToBase == MICRO ? 1 : -1;
                case QUALIFIER:
                    return differenceV1ToBase == QUALIFIER ? 1 : -1;
                case SUFFIX:
                    return differenceV1ToBase == SUFFIX ? 1 : -1;
                case RH_SUFFIX:
                    return differenceV1ToBase == RH_SUFFIX ? 1 : -1;
                default:
                    throw new IllegalStateException("Unknown difference " + difference);
            }
        }

        // Rule 2
        if (differenceV1ToBase == differenceV2ToBase && differenceV1ToBase == difference) {
            switch (difference) {
                case MAJOR:
                    return comparePart(base.getMajor(), v1.getMajor(), v2.getMajor());
                case MINOR:
                    return comparePart(base.getMinor(), v1.getMinor(), v2.getMinor());
                case MICRO:
                    return comparePart(base.getMicro(), v1.getMicro(), v2.getMicro());
            }
        }

        // Rule 3
        SuffixedVersion[] versions = { base, v1, v2 };
        Arrays.sort(versions);

        if (versions[0] == base) { // v1 and v2 are greater than base, use the lowest of them
            int candidate;
            if (versions[1] == v1) {
                candidate = -1;
            } else {
                candidate = 1;
            }

            // if differs only in qualifier, higher qualifier preferred
            if (difference != MAJOR && difference != MINOR && difference != MICRO) {
                candidate *= -1;
            }
            return candidate;
        } else if (versions[2] == base) { // v1 and v2 are lower than base, use the greatest of them
            if (versions[1] == v1) {
                return -1;
            } else {
                return 1;
            }
        } else { // one is lower than base, second is greater than base. They MUST differ in qualifier, so use greater
            if (difference == MAJOR || difference == MINOR || difference == MICRO) {
                throw new IllegalStateException("Broken rule 2");
            }
            if (versions[2] == v1) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    private int comparePart(int base, int v1, int v2) {
        int v1diff = Math.abs(base - v1);
        int v2diff = Math.abs(base - v2);
        if (v1diff == v2diff) {
            if (v1 > v2) {
                return -1;
            } else {
                return 1;
            }
        }
        return v1diff - v2diff;
    }
}
