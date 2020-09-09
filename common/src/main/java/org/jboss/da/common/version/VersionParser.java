package org.jboss.da.common.version;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.commonjava.maven.ext.manip.impl.Version;

public class VersionParser {

    public static final String DEFAULT_SUFFIX = "redhat";

    private final String suffix;

    private final Pattern defaultPattern = Pattern
            .compile("^" + RE_MMM + RE_QUALIFIER + "??" + RE_SUFFIX_S + DEFAULT_SUFFIX + RE_SUFFIX_E + "$");

    private final Pattern versionPattern;

    // major.minor.micro.qualifier-suffix-X
    // numbers limited to max 9 digits, beacuse of integer limitatations
    static final String RE_MMM = "((?<major>[0-9]{1,9})?(\\.(?<minor>[0-9]{1,9})(\\.(?<micro>[0-9]{1,9}))?)?)";

    static final String RE_QUALIFIER = "([.-]?(?<qualifier>.+?))";

    private static final String RE_SUFFIX_S = "([.-]";

    private static final String RE_SUFFIX_E = "-(?<suffixversion>[0-9]{1,9}))?";

    private static final Pattern UNSUFFIXED_PATTERN = Pattern.compile("^" + RE_MMM + RE_QUALIFIER + "?" + "$");

    public VersionParser(String suffix) {
        this.suffix = suffix;
        this.versionPattern = Pattern
                .compile("^" + RE_MMM + RE_QUALIFIER + "??" + RE_SUFFIX_S + suffix + RE_SUFFIX_E + "$");
    }

    public static SuffixedVersion parseUnsuffixed(String version) {
        return parseVersion(UNSUFFIXED_PATTERN.matcher(version), version);
    }

    public SuffixedVersion parse(String version) {
        SuffixedVersion suffixedVersion = parseVersion(versionPattern.matcher(version), version, suffix);
        if (!suffixedVersion.isSuffixed()) {
            suffixedVersion = parseVersion(defaultPattern.matcher(version), version, DEFAULT_SUFFIX);
        }

        return suffixedVersion;
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
