package org.jboss.da.reports.backend.impl;

import org.jboss.da.common.version.VersionParser;
import org.jboss.da.model.rest.GAV;
import org.jboss.da.model.rest.VersionComparator;
import org.jboss.da.reports.api.VersionLookupResult;
import org.jboss.da.reports.backend.api.VersionFinder;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.jboss.da.listings.api.service.BlackArtifactService;
import org.jboss.da.products.backend.api.ProductArtifacts;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * Performs single lookups for the built artifacts
 * 
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
@ApplicationScoped
public class VersionFinderImpl implements VersionFinder {

    @Inject
    Logger log;

    @Inject
    private VersionParser osgiParser;

    @Inject
    private BlackArtifactService blackArtifactService;

    @Override
    public Optional<String> getBestMatchVersionFor(GAV gav, List<String> availableVersions) {
        List<String> versions = filterVersions(availableVersions.stream(), gav).collect(
                Collectors.toList());
        return findBiggestMatchingVersion(gav, versions);
    }

    @Override
    public CompletableFuture<VersionLookupResult> getVersionsFor(GAV gav, CompletableFuture<Set<ProductArtifacts>> availableArtifacts) {
        return availableArtifacts.thenApply(pas -> {
            List<String> versions = getVersions(pas, gav);
            Optional<String> bmv = findBiggestMatchingVersion(gav, versions);

            return new VersionLookupResult(bmv, versions);
        });
    }

    private Stream<String> filterVersions(Stream<String> versions, GAV gav) {
        return versions
                .distinct()
                .filter(v -> !blackArtifactService.isArtifactPresent(new GAV(gav.getGA(), v)));
    }

    private List<String> getVersions(Set<ProductArtifacts> pas, GAV gav) {
        Stream<String> versions = pas.stream()
                .flatMap(as -> as.getArtifacts().stream())
                .map(a -> a.getGav().getVersion());
        
        return filterVersions(versions, gav)
                .sorted(new VersionComparator(gav.getVersion()))
                .collect(Collectors.toList());
    }

    private Optional<String> findBiggestMatchingVersion(GAV gav, List<String> obtainedVersions) {
        if (obtainedVersions.isEmpty())
            return Optional.empty();

        return findBiggestMatchingVersion(obtainedVersions, gav.getVersion());
    }

    private Optional<String> findBiggestMatchingVersion(List<String> obtainedVersions,
            String version) {
        Matcher matcher = osgiParser.getVersionMatcher(osgiParser.getOSGiVersion(version));
        String bestMatchVersion = null;
        int biggestBuildNumber = 0;

        for (String ver : obtainedVersions) {
            String osgiver = osgiParser.getOSGiVersion(ver);
            matcher.reset(osgiver);
            if (matcher.matches()) {
                int foundBuildNumber = Integer.parseInt(matcher.group(1));
                if (bestMatchVersion == null || foundBuildNumber > biggestBuildNumber) {
                    bestMatchVersion = ver;
                    biggestBuildNumber = foundBuildNumber;
                } else if (foundBuildNumber == biggestBuildNumber) {
                    bestMatchVersion = getMoreSpecificVersion(bestMatchVersion, ver);
                }
            }
        }

        return Optional.ofNullable(bestMatchVersion);
    }

    /**
     * Assuming the two versions have the same OSGi representation, returns the more specific
     * version. That means X.Y.Z.something is preffered to X.Y.something which is preffered to
     * X.something.
     */
    private String getMoreSpecificVersion(String first, String second) {
        Matcher firstMatcher = VersionComparator.VERSION_PATTERN.matcher(first);
        Matcher secondMatcher = VersionComparator.VERSION_PATTERN.matcher(second);
        if (!firstMatcher.matches()) {
            throw new IllegalArgumentException("Couldn't parse version " + first);
        }
        if (!secondMatcher.matches()) {
            throw new IllegalArgumentException("Couldn't parse version " + second);
        }
        if (firstMatcher.group("minor") == null && secondMatcher.group("minor") != null) {
            return second;
        }
        if (firstMatcher.group("micro") == null && secondMatcher.group("micro") != null) {
            return second;
        }
        return first;
    }
}
