package org.jboss.da.common.version;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.commonjava.maven.ext.manip.impl.Version;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class VersionParser {

    private static final String DEFAULT_SUFFIX = "redhat";

    private final String suffix;

    private final String suffixVersionPattern;

    private final Pattern suffixPattern;

    public VersionParser() {
        this(DEFAULT_SUFFIX);
    }

    public VersionParser(String suffix) {
        this.suffix = suffix;
        this.suffixVersionPattern = "[.-]" + suffix + "-(\\d+)";
        this.suffixPattern = Pattern.compile(suffixVersionPattern + "$");
    }

    /**
     * Converts version to osgi compliant
     * 
     * @param version
     * @return
     */
    public String getOSGiVersion(String version) {
        if (isSuffixedVersion(version)) {
            return toOsgi(version);
        } else {
            String osgiS = toOsgi(version + "." + suffix);
            if (osgiS.endsWith("." + suffix))
                return osgiS.replace("." + suffix, "");
            else
                return osgiS.replace("-" + suffix, "");
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
        return suffixPattern.matcher(version).replaceFirst("");
    }

    public Matcher getVersionMatcher(String version) {
        String nonRedhatVersion = removeRedhatSuffix(version);
        return Pattern.compile(Pattern.quote(nonRedhatVersion) + suffixVersionPattern).matcher("");
    }

    public boolean isSuffixedVersion(String version) {
        return suffixPattern.matcher(version).find();
    }

    private String toOsgi(String version) {
        return (new Version(version)).getOSGiVersionString();
    }
}
