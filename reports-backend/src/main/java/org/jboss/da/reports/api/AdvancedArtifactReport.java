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
    private Set<GAV> blacklistArtifacts = new HashSet<>();

    @Getter
    private Set<GAV> whitelistArtifacts = new HashSet<>();

    @Getter
    private Set<GAV> communityGavsWithBestMatchVersions = new HashSet<>();

    @Getter
    private Set<GAV> communityGavsWithBuiltVersions = new HashSet<>();

    @Getter
    private Set<GAV> communityGavs = new HashSet<>();

    public void addBlacklistArtifact(GAV gav) {
        blacklistArtifacts.add(gav);
    }

    public void addWhitelistArtifact(GAV gav) {
        whitelistArtifacts.add(gav);
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
