/*
 * Copyright 2018 Honza Brázdil &lt;jbrazdil@redhat.com&gt;.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.da.common.version;

import org.jboss.da.common.CompiledConstraints;
import org.jboss.da.lookup.model.VersionDistanceRule;
import org.jboss.da.lookup.model.VersionFilter;
import org.jboss.pnc.api.dependencyanalyzer.dto.QualifiedVersion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.jboss.da.common.version.VersionComparator.VersionDifference.EQUAL;
import static org.jboss.da.common.version.VersionComparator.VersionDifference.MICRO;
import static org.jboss.da.common.version.VersionComparator.VersionDifference.MINOR;
import static org.jboss.da.common.version.VersionComparator.VersionDifference.QUALIFIER;
import static org.jboss.da.common.version.VersionComparator.VersionDifference.RH_SUFFIX;
import static org.jboss.da.common.version.VersionComparator.VersionDifference.SUFFIX;

/**
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public class VersionAnalyzer {

    public static final Map<VersionFilter, EnumSet<VersionComparator.VersionDifference>> ALLOWED_DIFFERENCE = new HashMap<>();
    static {
        ALLOWED_DIFFERENCE.put(VersionFilter.ALL, EnumSet.allOf(VersionComparator.VersionDifference.class));
        ALLOWED_DIFFERENCE.put(VersionFilter.MAJOR, EnumSet.of(MINOR, MICRO, QUALIFIER, SUFFIX, RH_SUFFIX, EQUAL));
        ALLOWED_DIFFERENCE.put(VersionFilter.MAJOR_MINOR, EnumSet.of(MICRO, QUALIFIER, SUFFIX, RH_SUFFIX, EQUAL));
        ALLOWED_DIFFERENCE.put(VersionFilter.MAJOR_MINOR_MICRO, EnumSet.of(QUALIFIER, SUFFIX, RH_SUFFIX, EQUAL));
        ALLOWED_DIFFERENCE.put(VersionFilter.MAJOR_MINOR_MICRO_QUALIFIER, EnumSet.of(SUFFIX, RH_SUFFIX, EQUAL));
    }

    private static final Pattern VERSION_PATTERN = Pattern
            .compile("^" + VersionParser.RE_MMM + VersionParser.RE_QUALIFIER_WITH_SEPARATOR + "?");

    private final VersionParser versionParser;
    private final List<String> suffixes = new ArrayList<>();
    private final VersionDistanceRule distanceRule;
    private final CompiledConstraints constraints;

    public VersionAnalyzer(List<String> suffixes) {
        this(suffixes, VersionDistanceRule.RECOMMENDED_REPLACEMENT);
    }

    public VersionAnalyzer(List<String> suffixes, CompiledConstraints constraints) {
        this(suffixes, VersionDistanceRule.RECOMMENDED_REPLACEMENT, constraints);
    }

    public VersionAnalyzer(List<String> suffixes, VersionDistanceRule distanceRule) {
        this(suffixes, distanceRule, CompiledConstraints.none());
    }

    public VersionAnalyzer(List<String> suffixes, VersionDistanceRule distanceRule, CompiledConstraints constraints) {
        this.suffixes.addAll(suffixes);
        this.versionParser = new VersionParser(suffixes);
        this.distanceRule = Objects.requireNonNull(distanceRule);
        this.constraints = Objects.requireNonNull(constraints);

        constraints.getRanks().overrideVersionComparator(this::compareByBuildNumber);
    }

    // TECHNICALLY NOT NEEDED, THIS IS FOR OLD MAVEN/NPM LOOKUP STUFF ENDPOINTS
    public List<String> sortVersions(String querry, Collection<String> versions) {
        VersionComparator comparator = new VersionComparator(querry, distanceRule, versionParser);
        List<String> sortedVersions = versions.stream().sorted(comparator).distinct().collect(Collectors.toList());
        return sortedVersions;
    }

    // NO NEED, USED FOR VERSIONS' ENDPOINTS
    public List<String> filterVersions(String query, VersionFilter vf, Collection<String> versions) {
        VersionComparator vc = new VersionComparator(query, distanceRule, versionParser);

        return versions.stream()
                .map(versionParser::parseSuffixed)
                .flatMap(Set::stream)
                .filter(v -> matches(vc, v, vf))
                .map(SuffixedVersion::getOriginalVersion)
                .sorted(vc)
                .distinct()
                .collect(Collectors.toList());
    }

    private boolean matches(VersionComparator vc, SuffixedVersion v, VersionFilter vf) {
        VersionComparator.VersionDifference difference = vc.difference(v);
        return ALLOWED_DIFFERENCE.get(vf).contains(difference);
    }

    public Optional<String> findBiggestMatchingVersion(String query, Collection<QualifiedVersion> versions) {
        String unsuffixedQuery = versionParser.parse(query).unsuffixedVesion();

        // IF QUERY==2.3.4.REDHAT-00001 then FILTER ALL versions in 'versions' to have 2.3.4
        List<SuffixedVersion> candidateSuffixedVersions = versions.stream()
                .map(versionParser::parseSuffixed)
                .flatMap(Set::stream)
                .filter(v -> unsuffixedQuery.equals(v.unsuffixedVesion()))
                .collect(Collectors.toList());

        List<SuffixedVersion> versionsToSearch = Collections.emptyList();
        for (String suffix : suffixes) {
            versionsToSearch = candidateSuffixedVersions.stream()
                    .filter(v -> suffix.equals(v.getSuffix().get()))
                    .collect(Collectors.toList());
            if (!versionsToSearch.isEmpty()) {
                break;
            }
        }

        return versionsToSearch.stream()
                .map(SuffixedVersion::getOriginalVersionWithMeta)
                .filter(constraints.getAllowList())
                .filter(constraints.getDenyList().negate())
                .max(constraints.getRanks())
                .map(QualifiedVersion::getVersion);
    }

    private int compareByBuildNumber(String first, String second) {
        SuffixedVersion firstSuffixed = versionParser.parse(first);
        SuffixedVersion secondSuffixed = versionParser.parse(second);

        int firstBuildNum = firstSuffixed.getSuffixVersion().get();
        int secondBuildNum = secondSuffixed.getSuffixVersion().get();

        if (firstBuildNum > secondBuildNum) {
            return 1;
        }
        if (firstBuildNum == secondBuildNum) {
            return getMoreSpecificVersion(first, second);
        }

        return -1;
    }

    private static int getMoreSpecificVersion(String first, String second) {
        Matcher firstMatcher = VERSION_PATTERN.matcher(first);
        Matcher secondMatcher = VERSION_PATTERN.matcher(second);
        if (!firstMatcher.matches()) {
            throw new IllegalArgumentException("Couldn't parse version " + first);
        }
        if (!secondMatcher.matches()) {
            throw new IllegalArgumentException("Couldn't parse version " + second);
        }
        boolean firstIsOSGi = first.equals(VersionParser.getOSGiVersion(first));
        boolean secondIsOSGi = second.equals(VersionParser.getOSGiVersion(second));
        String firstMinor = firstMatcher.group("minor");
        String firstMicro = firstMatcher.group("micro");
        boolean returnFirst;

        if (firstIsOSGi != secondIsOSGi) {
            returnFirst = firstIsOSGi; // One of the version is not OSGi, prefer the OSGi version
        } else if (!Objects.equals(firstMinor, secondMatcher.group("minor"))) {
            returnFirst = firstMinor != null; // One of the versions is missing minor number, prefer the one with it
        } else if (!Objects.equals(firstMicro, secondMatcher.group("micro"))) {
            returnFirst = firstMicro != null; // One of the versions is missing micro number, prefer the one with it
        } else {
            // Prefer the version that separates qualifier with '.', not something else like '-'
            // If both are the same, prefer first
            returnFirst = firstMatcher.group("qualifier").startsWith(".")
                    || !secondMatcher.group("qualifier").startsWith(".");
        }
        if (returnFirst) {
            return 1;
        } else {
            return -1;
        }
    }

    /**
     * Assuming the two versions have the same OSGi representation, returns the more specific version. That means
     * X.Y.Z.something is preffered to X.Y.something which is preffered to X.something.
     */
    private static String getMoreSpecificVersionString(String first, String second) {
        Matcher firstMatcher = VERSION_PATTERN.matcher(first);
        Matcher secondMatcher = VERSION_PATTERN.matcher(second);
        if (!firstMatcher.matches()) {
            throw new IllegalArgumentException("Couldn't parse version " + first);
        }
        if (!secondMatcher.matches()) {
            throw new IllegalArgumentException("Couldn't parse version " + second);
        }
        boolean firstIsOSGi = first.equals(VersionParser.getOSGiVersion(first));
        boolean secondIsOSGi = second.equals(VersionParser.getOSGiVersion(second));
        String firstMinor = firstMatcher.group("minor");
        String firstMicro = firstMatcher.group("micro");
        boolean returnFirst;

        if (firstIsOSGi != secondIsOSGi) {
            returnFirst = firstIsOSGi; // One of the version is not OSGi, prefer the OSGi version
        } else if (!Objects.equals(firstMinor, secondMatcher.group("minor"))) {
            returnFirst = firstMinor != null; // One of the versions is missing minor number, prefer the one with it
        } else if (!Objects.equals(firstMicro, secondMatcher.group("micro"))) {
            returnFirst = firstMicro != null; // One of the versions is missing micro number, prefer the one with it
        } else {
            // Prefer the version that separates qualifier with '.', not something else like '-'
            // If both are the same, prefer first
            returnFirst = firstMatcher.group("qualifier").startsWith(".")
                    || !secondMatcher.group("qualifier").startsWith(".");
        }
        if (returnFirst) {
            return first;
        } else {
            return second;
        }
    }
}
