package org.jboss.da.common.version;

import org.commonjava.maven.ext.manip.impl.Version;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionParser {

    private final Map<String, Pattern> versionPatterns = new HashMap<>();

    // single dot at the end of the version indicates ommited micro "0"
    // NCLSUP-132 asks to allow dash instead of dot before micro
    static final String RE_MICRO = "(\\.$|[.-](?<micro>[0-9]{1,9}))";

    // major.minor.micro.qualifier-suffix-X
    // numbers limited to max 9 digits, beacuse of integer limitatations
    static final String RE_MMM = "((?<major>[0-9]{1,9})?(\\.(?<minor>[0-9]{1,9})" + RE_MICRO + "?)?)";

    static final String RE_QUALIFIER = "([.-]?(?<qualifier>.+?))";
    // this differs from RE_QUALIFIER only in that the group also contains the [.-] separator
    static final String RE_QUALIFIER_WITH_SEPARATOR = "(?<qualifier>[.-]?(.+?))";

    private static final String RE_SUFFIX_S = "([.-]";

    private static final String RE_SUFFIX_E = "-(?<suffixversion>[0-9]{1,9}))?";

    private static final Pattern UNSUFFIXED_PATTERN = Pattern.compile("^" + RE_MMM + RE_QUALIFIER + "?" + "$");

    public VersionParser(String... suffix) {
        this(Arrays.asList(suffix));
    }

    public VersionParser(List<String> suffixes) {
        for (String suffix : suffixes) {
            this.versionPatterns.put(
                    suffix,
                    Pattern.compile("^" + RE_MMM + RE_QUALIFIER + "??" + RE_SUFFIX_S + suffix + RE_SUFFIX_E + "$"));
        }
    }

    public static SuffixedVersion parseUnsuffixed(String version) {
        return parseVersion(UNSUFFIXED_PATTERN.matcher(version), version);
    }

    /**
     * Parses the version string and returns the normalized version (with longest suffix). Because the version may have
     * any of the suffixes (ore none) the normalized version is the one with the longest suffix (or in other words with
     * shortest version string after removing the suffix).
     *
     * @param version The original version string.
     * @return The normalized version
     */
    public SuffixedVersion parse(String version) {
        SuffixedVersion normalized = parseUnsuffixed(version);
        int length = normalized.getQualifier().length();
        for (SuffixedVersion suffixedVersion : parseSuffixed(version)) {
            if (suffixedVersion.getQualifier().length() < length) {
                normalized = suffixedVersion;
                length = suffixedVersion.getQualifier().length();
            }
        }
        return normalized;
    }

    /**
     * Returns suffixed versions that can be parsed from the provided version string.
     *
     * @param version The version string to parse
     * @return Set of suffixed versions parsable from the version string.
     */
    public Set<SuffixedVersion> parseSuffixed(String version) {
        Set<SuffixedVersion> ret = new HashSet<>();
        for (Map.Entry<String, Pattern> entry : versionPatterns.entrySet()) {
            String suffix = entry.getKey();
            Pattern versionPattern = entry.getValue();
            SuffixedVersion suffixedVersion = parseVersion(versionPattern.matcher(version), version, suffix);
            if (suffixedVersion.isSuffixed()) {
                ret.add(suffixedVersion);
            }
        }
        return ret;
    }

    private static SuffixedVersion parseVersion(Matcher versionMatcher, String version)
            throws NumberFormatException, IllegalArgumentException {
        if (!versionMatcher.matches()) {
            throw new IllegalArgumentException("Version " + version + "is unparsable");
        }
        String majorString = versionMatcher.group("major");
        String minorString = versionMatcher.group("minor");
        String microString = versionMatcher.group("micro");
        String qualifierString = versionMatcher.group("qualifier");

        int major = parseNumberString(majorString);
        int minor = parseNumberString(minorString);
        int micro = parseNumberString(microString);
        String qualifier = qualifierString == null ? "" : qualifierString.replace('.', '-').replace(',', '-');
        return new SuffixedVersion(major, minor, micro, qualifier, version);
    }

    private static SuffixedVersion parseVersion(Matcher versionMatcher, String version, String parseSuffix)
            throws NumberFormatException, IllegalArgumentException {
        if (!versionMatcher.matches()) {
            throw new IllegalArgumentException("Version " + version + "is unparsable");
        }
        String majorString = versionMatcher.group("major");
        String minorString = versionMatcher.group("minor");
        String microString = versionMatcher.group("micro");
        String qualifierString = versionMatcher.group("qualifier");
        String suffixVersionString = versionMatcher.group("suffixversion");

        int major = parseNumberString(majorString);
        int minor = parseNumberString(minorString);
        int micro = parseNumberString(microString);
        String qualifier = qualifierString == null ? "" : qualifierString.replace('.', '-').replace(',', '-');
        if (suffixVersionString == null) {
            return new SuffixedVersion(major, minor, micro, qualifier, version);
        } else {
            int suffixVersion = Integer.parseInt(suffixVersionString);
            return new SuffixedVersion(major, minor, micro, qualifier, parseSuffix, suffixVersion, version);
        }
    }

    private static int parseNumberString(String segmentString) {
        return segmentString == null ? 0 : Integer.parseInt(segmentString);
    }

    /**
     * Converts version to osgi compliant
     *
     * @param version
     * @return
     */
    public static String getOSGiVersion(String version) {
        String osgiS = (new Version(version + ".foo")).getOSGiVersionString();
        int len = osgiS.length();
        return osgiS.substring(0, len - 4);
    }

}
