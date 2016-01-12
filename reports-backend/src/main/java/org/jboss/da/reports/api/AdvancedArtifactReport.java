package org.jboss.da.reports.api;

import org.jboss.da.communication.model.GAV;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

public class AdvancedArtifactReport {

    @Getter
    @Setter
    private ArtifactReport artifactReport;

    @Getter
    private Set<GAV> blacklistedArtifacts = new HashSet<>();

    @Getter
    private Set<GAV> whitelistedArtifacts = new HashSet<>();

    @Getter
    private Set<GAV> communityGavsWithBestMatchVersions = new HashSet<>();

    @Getter
    private Set<GAV> communityGavsWithBuiltVersions = new HashSet<>();

    @Getter
    private Set<GAV> communityGavs = new HashSet<>();

    public void addBlacklistedArtifact(GAV gav) {
        blacklistedArtifacts.add(gav);
    }

    public void addWhitelistedArtifact(GAV gav) {
        whitelistedArtifacts.add(gav);
    }

    public void addCommunityGavWithBestMatchVersion(GAV gav) {
        communityGavsWithBestMatchVersions.add(gav);
    }

    public void addCommunityGavWithBuiltVersion(GAV gav) {
        communityGavsWithBuiltVersions.add(gav);
    }

    public void addCommunityGav(GAV gav) {
        communityGavs.add(gav);
    }
}
