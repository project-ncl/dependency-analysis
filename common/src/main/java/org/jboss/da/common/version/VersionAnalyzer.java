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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.Data;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public class VersionAnalyzer {

    private final VersionParser versionParser;

    public VersionAnalyzer(VersionParser versionParser) {
        this.versionParser = versionParser;
    }

    public VersionAnalysisResult analyseVersions(String querry, Collection<String> versions){
        VersionComparator comparator = new VersionComparator(querry, versionParser);
        List<String> sortedVersions = new ArrayList<>(versions);
        Collections.sort(sortedVersions, comparator);
        
        List<SuffixedVersion> parsedVersions = sortedVersions.stream()
                .map(versionParser::parse)
                .collect(Collectors.toList());
        SuffixedVersion version = versionParser.parse(querry);
        Optional<String> bmv = findBiggestMatchingVersion(version, parsedVersions);

        return new VersionAnalysisResult(bmv, sortedVersions);
    }

    private Optional<String> findBiggestMatchingVersion(SuffixedVersion version,
            Collection<SuffixedVersion> versions) {
        String bestMatchVersion = null;
        int biggestBuildNumber = 0;

        String unsuffixedVesion = version.unsuffixedVesion();
        for (SuffixedVersion ver : versions) {
            if (ver.isSuffixed() && unsuffixedVesion.equals(ver.unsuffixedVesion())) {
                int foundBuildNumber = ver.getSuffixVersion().get();
                if (bestMatchVersion == null || foundBuildNumber > biggestBuildNumber) {
                    bestMatchVersion = ver.getOriginalVersion();
                    biggestBuildNumber = foundBuildNumber;
                } else if (foundBuildNumber == biggestBuildNumber) {
                    bestMatchVersion = getMoreSpecificVersion(bestMatchVersion,
                            ver.getOriginalVersion());
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
        Pattern pattern = Pattern.compile("^" + VersionParser.RE_MMM + VersionParser.RE_QUALIFIER
                + "?");
        Matcher firstMatcher = pattern.matcher(first);
        Matcher secondMatcher = pattern.matcher(second);
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

    @Data
    public static class VersionAnalysisResult {

        private final Optional<String> bestMatchVersion;

        private final List<String> availableVersions;
    }
}
