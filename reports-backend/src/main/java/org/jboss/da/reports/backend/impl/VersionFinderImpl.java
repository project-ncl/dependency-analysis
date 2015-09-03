package org.jboss.da.reports.backend.impl;

import org.jboss.da.common.version.OSGiVersionParser;
import org.jboss.da.communication.CommunicationException;
import org.jboss.da.communication.aprox.api.AproxConnector;
import org.jboss.da.communication.model.GAV;
import org.jboss.da.reports.api.VersionLookupResult;
import org.jboss.da.reports.backend.api.VersionFinder;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Performs single lookups for the built artifacts
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
@ApplicationScoped
public class VersionFinderImpl implements VersionFinder {

    private static final String PATTERN_SUFFIX_BUILT_VERSION = "[.-]redhat-(\\d+)\\D*";

    @Inject
    private AproxConnector aproxConnector;

    @Inject
    private OSGiVersionParser osgiParser;

    @Override
    public List<String> getBuiltVersionsFor(GAV gav) throws CommunicationException {
        List<String> allVersions = aproxConnector.getVersionsOfGA(gav.getGA());
        return getBuiltVersionsFor0(allVersions);
    }

    @Override
    public VersionLookupResult lookupBuiltVersions(GAV gav) throws CommunicationException {
        List<String> allVersions = aproxConnector.getVersionsOfGA(gav.getGA());
        if (allVersions == null)
            return null;
        else
            return new VersionLookupResult(getBestMatchVersionFor(gav, allVersions),
                    getBuiltVersionsFor0(allVersions));
    }

    @Override
    public String getBestMatchVersionFor(GAV gav) throws CommunicationException {
        List<String> obtainedVersions = aproxConnector.getVersionsOfGA(gav.getGA());
        return findBiggestMatchingVersion(gav, obtainedVersions);
    }

    @Override
    public String getBestMatchVersionFor(GAV gav, List<String> availableVersions) {
        return findBiggestMatchingVersion(gav, availableVersions);
    }

    private List<String> getBuiltVersionsFor0(List<String> allVersions) {
        Pattern pattern = Pattern.compile(".*" + PATTERN_SUFFIX_BUILT_VERSION);
        if (allVersions == null) {
            return null;
        } else {
            List<String> redhatVersions = allVersions.stream()
                    .filter(version -> pattern.matcher(version).matches())
                    .collect(Collectors.toList());

            return redhatVersions.isEmpty() ? null : redhatVersions;
        }
    }

    private String findBiggestMatchingVersion(GAV gav, List<String> obtainedVersions) {
        if (obtainedVersions == null)
            return null;

        String origVersion = gav.getVersion();
        Pattern pattern = Pattern.compile(origVersion + PATTERN_SUFFIX_BUILT_VERSION);

        String bestMatchVersion = findBiggestMatchingVersion(obtainedVersions, pattern);
        if (bestMatchVersion == null) {
            String osgiVersion = osgiParser.getOSGiVersion(origVersion);
            pattern = Pattern.compile(osgiVersion + PATTERN_SUFFIX_BUILT_VERSION);
            bestMatchVersion = findBiggestMatchingVersion(obtainedVersions, pattern);
        }

        return bestMatchVersion;
    }

    private String findBiggestMatchingVersion(List<String> obtainedVersions, Pattern pattern) {
        String bestMatchVersion = null;
        int biggestBuildNumber = 0;

        for (String ver : obtainedVersions) {
            Matcher matcher = pattern.matcher(ver);
            if (matcher.matches()) {
                int foundBuildNumber = Integer.parseInt(matcher.group(1));
                if (foundBuildNumber > biggestBuildNumber) {
                    bestMatchVersion = ver;
                    biggestBuildNumber = foundBuildNumber;
                }
            }
        }

        return bestMatchVersion;
    }

}
