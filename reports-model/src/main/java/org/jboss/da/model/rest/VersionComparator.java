package org.jboss.da.model.rest;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
public class VersionComparator implements Comparator<String> {

    // major.minor.micro.qualifier-redhat-X
    // numbers limited to max 9 digits, beacuse of integer limitatations
    private static final Pattern VERSION_PATTERN = Pattern
            .compile("^(?<major>[0-9]{1,9})?(\\.(?<minor>[0-9]{1,9})(\\.(?<micro>[0-9]{1,9}))?)?([.-]?(?<qualifier>.+?))??([.-]redhat-(?<rhversion>[0-9]{1,9}))?$");

    public enum VersionDifference {
        MAJOR, MINOR, MICRO, QUALIFIER, RH_SUFFIX, EQUAL;
    }

    private final Version base;

    /**
     * Returns classic comparator for versions.
     */
    public VersionComparator() {
        this.base = null;
    }

    /**
     * Returns comparator that compares versions by distance to the base version.
     * @param base The base version.
     */
    public VersionComparator(String base) {
        this.base = new Version(Objects.requireNonNull(base));
    }

    /**
     * Return information about first part of version that is different.
     */
    public static VersionDifference difference(String version1, String version2) {
        Version v1 = new Version(version1);
        Version v2 = new Version(version2);

        if (v1.major != v2.major) {
            return VersionDifference.MAJOR;
        }
        if (v1.minor != v2.minor) {
            return VersionDifference.MINOR;
        }
        if (v1.micro != v2.micro) {
            return VersionDifference.MICRO;
        }
        if (v1.qualifier.equals(v2.qualifier)) {
            return VersionDifference.QUALIFIER;
        }
        if (v1.rhversion != v2.rhversion) {
            return VersionDifference.RH_SUFFIX;
        }
        return VersionDifference.EQUAL;
    }

    @Override
    public int compare(String version1, String version2) {
        Version v1 = new Version(version1);
        Version v2 = new Version(version2);
        int r = v1.compareTo(v2);
        if (r == 0 || base == null) {
            return r;
        } else {
            return compareByDistance(v1, v2);
        }
    }

    /**
     * Compare two versions.
     */
    public static int compareVersions(String version1, String version2) {
        Version v1 = new Version(version1);
        Version v2 = new Version(version2);
        return v1.compareTo(v2);
    }

    // Return -1 - v1 is closer to the base version
    // Return 1  - v2 is closer to the base version
    private int compareByDistance(Version v1, Version v2) {
        // If one of the versions is the same as base, it is closer.
        if (base.equals(v1)) {
            return -1;
        }
        if (base.equals(v2)) {
            return 1;
        }

        // If one version differs from the base in major, the other is closer no matter what
        if (v1.major != v2.major) {
            if (v1.major == base.major) {
                return -1;
            }
            if (v2.major == base.major) {
                return 1;
            }
        }

        // Sort the 3 versions
        Version[] versions = { base, v1, v2 };
        Arrays.sort(versions);

        if (versions[0] == base) { // v1 and v2 are greater then base, use the lower of them
            int candidate;
            if (versions[1] == v1) {
                candidate = -1;
            } else {
                candidate = 1;
            }

            // if differs only in qualifier, higher qualifier prefered
            if (v1.major == v2.major && v1.minor == v2.minor && v1.micro == v2.micro) {
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

    private static class Version implements Comparable<Version> {

        private final int major;

        private final int minor;

        private final int micro;

        private final String qualifier;

        private final int rhversion;

        public Version(int major, int minor, int micro, String qualifier, int rhversion) {
            this.major = major;
            this.minor = minor;
            this.micro = micro;
            this.qualifier = qualifier;
            this.rhversion = rhversion;
        }

        public Version(String version) {
            Matcher versionMatcher = VERSION_PATTERN.matcher(version);

            if (!versionMatcher.matches()) {
                throw new IllegalArgumentException("Version " + version + "is unparsable");
            }

            String majorString = versionMatcher.group("major");
            String minorString = versionMatcher.group("minor");
            String microString = versionMatcher.group("micro");
            String qualifierString = versionMatcher.group("qualifier");
            String rhversionString = versionMatcher.group("rhversion");

            major = parseNumberString(majorString);
            minor = parseNumberString(minorString);
            micro = parseNumberString(microString);
            qualifier = qualifierString == null ? "" : qualifierString;
            rhversion = rhversionString == null ? 0 : Integer.parseInt(rhversionString);
        }

        private int parseNumberString(String segmentString) throws NumberFormatException {
            if (segmentString == null) {
                return 0;
            } else {
                return Integer.parseInt(segmentString);
            }
        }

        @Override
        public int compareTo(Version other) {
            int r = Integer.compare(this.major, other.major);
            if (r != 0)
                return r;
            r = Integer.compare(this.minor, other.minor);
            if (r != 0)
                return r;
            r = Integer.compare(this.micro, other.micro);
            if (r != 0)
                return r;
            r = this.qualifier.compareToIgnoreCase(other.qualifier);
            if (r != 0)
                return r;
            r = Integer.compare(this.rhversion, other.rhversion);
            return r;
        }

        @Override
        public String toString() {
            return major + "." + minor + "." + micro + (qualifier.isEmpty() ? "" : "." + qualifier);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 17 * hash + this.major;
            hash = 17 * hash + this.minor;
            hash = 17 * hash + this.micro;
            hash = 17 * hash + Objects.hashCode(this.qualifier);
            hash = 17 * hash + this.rhversion;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Version other = (Version) obj;
            return compareTo(other) == 0;
        }

    }
}
