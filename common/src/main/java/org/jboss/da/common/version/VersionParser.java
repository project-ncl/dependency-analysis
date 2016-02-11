package org.jboss.da.common.version;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.commonjava.maven.ext.manip.impl.Version;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class VersionParser {

    private static final String PATTERN_SUFFIX_BUILT_VERSION = "[.-]redhat-([1-9]\\d*)";

    private static final Pattern redhatSuffixPattern = Pattern.compile(PATTERN_SUFFIX_BUILT_VERSION
            + "$");

    /**
     * Converts version to osgi compliant
     * 
     * @param version
     * @return
     */
    public String getOSGiVersion(String version) {
        if (isRedhatVersion(version)) {
            return toOsgi(version);
        } else {
            String osgiS = toOsgi(version + ".redhat");
            if (osgiS.endsWith(".redhat"))
                return osgiS.replace(".redhat", "");
            else
                return osgiS.replace("-redhat", "");
        }
    }

    /**
     * Removes redhat suffix if present and converts version to osgi compliant.
     *
     * @param version
     * @return
     */
    public String getNonRedhatOSGiVersion(String version) {
        return removeRedhatSuffix(getOSGiVersion(version));
    }

    public String removeRedhatSuffix(String version) {
        return redhatSuffixPattern.matcher(version).replaceFirst("");
    }

    public Matcher getVersionMatcher(String version) {
        String nonRedhatVersion = removeRedhatSuffix(version);
        return Pattern.compile(Pattern.quote(nonRedhatVersion) + PATTERN_SUFFIX_BUILT_VERSION)
                .matcher("");
    }

    public static boolean isRedhatVersion(String version) {
        return redhatSuffixPattern.matcher(version).find();
    }

    private String toOsgi(String version) {
        return (new Version(version)).getOSGiVersionString();
    }
}
