package org.jboss.da.reports.backend.impl;

import org.jboss.da.common.version.VersionParser;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.aprox.api.AproxConnector;
import org.jboss.da.model.rest.GAV;
import org.jboss.da.reports.api.VersionLookupResult;
import org.jboss.da.reports.backend.api.VersionFinder;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * Performs single lookups for the built artifacts
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
@ApplicationScoped
public class VersionFinderImpl implements VersionFinder {

    @Inject
    Logger log;

    @Inject
    private AproxConnector aproxConnector;

    @Inject
    private VersionParser osgiParser;

    @Override
    public List<String> getBuiltVersionsFor(GAV gav) throws CommunicationException {
        List<String> allVersions = aproxConnector.getVersionsOfGA(gav.getGA());
        return getBuiltVersionsFor0(allVersions);
    }

    @Override
    public VersionLookupResult lookupBuiltVersions(GAV gav) throws CommunicationException {
        if (!gav.getGA().isValid()) {
            log.warn("Received nonvalid GAV: " + gav);
            return new VersionLookupResult(Optional.empty(), Collections.emptyList());
        }
        List<String> allVersions = aproxConnector.getVersionsOfGA(gav.getGA());
        return new VersionLookupResult(getBestMatchVersionFor(gav, allVersions),
                getBuiltVersionsFor0(allVersions));
    }

    @Override
    public Optional<String> getBestMatchVersionFor(GAV gav) throws CommunicationException {
        List<String> obtainedVersions = aproxConnector.getVersionsOfGA(gav.getGA());
        return findBiggestMatchingVersion(gav, obtainedVersions);
    }

    @Override
    public Optional<String> getBestMatchVersionFor(GAV gav, List<String> availableVersions) {
        return findBiggestMatchingVersion(gav, availableVersions);
    }

    private List<String> getBuiltVersionsFor0(List<String> allVersions) {
        List<String> redhatVersions = allVersions.stream()
                .filter(VersionParser::isRedhatVersion)
                .collect(Collectors.toList());

        return redhatVersions;
    }

    private Optional<String> findBiggestMatchingVersion(GAV gav, List<String> obtainedVersions) {
        if (obtainedVersions.isEmpty())
            return Optional.empty();

        String origVersion = gav.getVersion();

        Matcher origMatcher = osgiParser.getVersionMatcher(origVersion);
        Optional<String> bestMatchVersion = findBiggestMatchingVersion(obtainedVersions,
                origMatcher);
        if (!bestMatchVersion.isPresent()) {
            String osgiVersion = osgiParser.getOSGiVersion(origVersion);
            Matcher osgiMatcher = osgiParser.getVersionMatcher(osgiVersion);
            bestMatchVersion = findBiggestMatchingVersion(obtainedVersions, osgiMatcher);
        }

        return bestMatchVersion;
    }

    private Optional<String> findBiggestMatchingVersion(List<String> obtainedVersions,
            Matcher matcher) {
        String bestMatchVersion = null;
        int biggestBuildNumber = 0;

        for (String ver : obtainedVersions) {
            matcher.reset(ver);
            if (matcher.matches()) {
                int foundBuildNumber = Integer.parseInt(matcher.group(1));
                if (foundBuildNumber > biggestBuildNumber) {
                    bestMatchVersion = ver;
                    biggestBuildNumber = foundBuildNumber;
                }
            }
        }

        return Optional.ofNullable(bestMatchVersion);
    }

}
