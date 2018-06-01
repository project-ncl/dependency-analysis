package org.jboss.da.common.version;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public class VersionComparator implements Comparator<String>, Serializable {

    public enum VersionDifference {
        MAJOR, MINOR, MICRO, QUALIFIER, SUFFIX, RH_SUFFIX, EQUAL;
    }

    private final SuffixedVersion base;

    private final VersionParser versionParser;

    /**
     * Returns classic comparator for versions.
     * @param versionParser Parser that will be used to parse the version.
     */
    public VersionComparator(VersionParser versionParser) {
        this.base = null;
        this.versionParser = versionParser;
    }

    /**
     * Returns comparator that compares versions by distance to the base version.
     * @param base The base version.
     * @param versionParser Parser that will be used to parse the version.
     */
    public VersionComparator(String base, VersionParser versionParser) {
        this.versionParser = versionParser;
        this.base = versionParser.parse(base);
    }

    /**
     * Return information about first part of version that is different.
     */
    public VersionDifference difference(String version1, String version2) {
        SuffixedVersion v1 = versionParser.parse(version1);
        SuffixedVersion v2 = versionParser.parse(version2);

        if (v1.getMajor() != v2.getMajor()) {
            return VersionDifference.MAJOR;
        }
        if (v1.getMinor() != v2.getMinor()) {
            return VersionDifference.MINOR;
        }
        if (v1.getMicro() != v2.getMicro()) {
            return VersionDifference.MICRO;
        }
        if (!v1.getQualifier().equals(v2.getQualifier())) {
            return VersionDifference.QUALIFIER;
        }
        if (!v1.getSuffix().equals(v2.getSuffix())) {
            return VersionDifference.SUFFIX;
        }
        if (!v1.getSuffixVersion().equals(v2.getSuffixVersion())) {
            return VersionDifference.RH_SUFFIX;
        }
        return VersionDifference.EQUAL;
    }

    @Override
    public int compare(String version1, String version2) {
        SuffixedVersion v1 = versionParser.parse(version1);
        SuffixedVersion v2 = versionParser.parse(version2);
        int r = v1.compareTo(v2);
        if (r == 0 || base == null) {
            return r;
        } else {
            return compareByDistance(v1, v2);
        }
    }

    // Assuming different versions
    // Return -1 - v1 is closer to the base version
    // Return 1  - v2 is closer to the base version
    private int compareByDistance(SuffixedVersion v1, SuffixedVersion v2) {
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

        if (versions[0] == base) { // v1 and v2 are greater then base, use the lower of them
            int candidate;
            if (versions[1] == v1) {
                candidate = -1;
            } else {
                candidate = 1;
            }

            // if differs only in qualifier, higher qualifier prefered
            if (v1.getMajor() == v2.getMajor() && v1.getMinor() == v2.getMinor()
                    && v1.getMicro() == v2.getMicro()) {
                candidate *= -1;
            }
            return candidate;
        } else if (versions[2] == base) { // v1 and v2 are lower then base, use the greater of them
            if (versions[1] == v1) {
                return -1;
            } else {
                return 1;
            }
        } else { // one is lower then base, second is greater then base. Use the greater.
            if (versions[2] == v1) {
                return -1;
            } else {
                return 1;
            }
        }
    }

}
