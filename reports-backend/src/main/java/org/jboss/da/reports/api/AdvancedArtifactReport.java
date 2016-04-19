package org.jboss.da.reports.api;

import org.jboss.da.listings.api.model.ProductVersion;
import org.jboss.da.model.rest.GAV;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

public class AdvancedArtifactReport {

    @Getter
    @Setter
    private ArtifactReport artifactReport;

    @Getter
    private final Set<GAV> blacklistedArtifacts = new HashSet<>();

    @Getter
    private final Map<GAV, Set<ProductVersion>> whitelistedArtifacts = new HashMap<>();

    @Getter
    private final Map<GAV, String> communityGavsWithBestMatchVersions = new HashMap<>();

    @Getter
    private final Map<GAV, Set<String>> communityGavsWithBuiltVersions = new HashMap<>();

    @Getter
    private final Set<GAV> communityGavs = new HashSet<>();

    public void addBlacklistedArtifact(GAV gav) {
        blacklistedArtifacts.add(gav);
    }

    public void addWhitelistedArtifact(GAV gav, Set<ProductVersion> products) {
        whitelistedArtifacts.put(gav, products);
    }

    public void addCommunityGavWithBestMatchVersion(GAV gav, String bestMatch) {
        communityGavsWithBestMatchVersions.put(gav, bestMatch);
    }

    public void addCommunityGavWithBuiltVersion(GAV gav, Set<String> versions) {
        communityGavsWithBuiltVersions.put(gav, versions);
    }

    public void addCommunityGav(GAV gav) {
        communityGavs.add(gav);
    }
}
