package org.jboss.da.reports.api;

import org.jboss.da.common.version.VersionParser;
import org.jboss.da.communication.model.GAV;
import org.jboss.da.listings.api.service.WhiteArtifactService;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;

public class AdvancedArtifactReport {

    private WhiteArtifactService whiteArtifactService;

    @Getter
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

    public AdvancedArtifactReport(ArtifactReport artifactReport,
            WhiteArtifactService whiteArtifactService) {
        this.artifactReport = artifactReport;
        this.whiteArtifactService = whiteArtifactService;
        advancedReportGeneration();
    }

    public void advancedReportGeneration() {
        Set<GAV> modulesAnalyzed = new HashSet<>();
        populateAdvancedFields(artifactReport, modulesAnalyzed);
    }

    /**
     * The logic on how the advanced fields are populated is determined from
     * DA-204
     *
     * @param report          report to analyze
     * @param modulesAnalyzed set of modules already analyzed
     */
    private void populateAdvancedFields(ArtifactReport report, Set<GAV> modulesAnalyzed) {

        for (ArtifactReport dep : report.getDependencies()) {
            if (modulesAnalyzed.contains(dep.getGav())) {
                // if module already analyzed, skip
                continue;
            } else if (isDependencyAModule(dep)) {
                // if dependency is a module, but not yet analyzed
                modulesAnalyzed.add(dep.getGav());
                populateAdvancedFields(dep, modulesAnalyzed);
            } else {
                // only generate populate advanced report with community GAVs
                if (VersionParser.isRedhatVersion(dep.getVersion())) continue;

                // we have a top-level module dependency
                if (dep.isWhitelisted()) whitelistArtifacts.add(dep.getGav());
                if (dep.isBlacklisted()) blacklistArtifacts.add(dep.getGav());

                if (dep.getBestMatchVersion().isPresent()) {
                    communityGavsWithBestMatchVersions.add(dep.getGav());
                    // also add bestMatchVersion GAV to set
                    communityGavsWithBestMatchVersions.add(new GAV(dep.getGroupId(),
                            dep.getArtifactId(), dep.getBestMatchVersion().get()));
                } else if (!dep.getAvailableVersions().isEmpty()) {
                    communityGavsWithBuiltVersions.add(dep.getGav());

                    // also add available versions to set
                    dep.getAvailableVersions().stream()
                            .forEach(v -> communityGavsWithBuiltVersions
                                    .add(new GAV(dep.getGroupId(), dep.getArtifactId(), v)));

                    // add any whitelisted GAVs, if any
                    addWhitelistedGAVToCommunityGavsWithBuiltVersions(dep.getGroupId(), dep.getArtifactId());
                } else if (hasWhitelistedGA(dep.getGroupId(), dep.getArtifactId())) {
                    // no bestmatch version, no built versions, but we have GAVs whitelisted
                    addWhitelistedGAVToCommunityGavsWithBuiltVersions(dep.getGroupId(), dep.getArtifactId());
                } else {
                    communityGavs.add(dep.getGav());
                }
            }
        }
    }

    private boolean isDependencyAModule(ArtifactReport dependency) {
        return dependency.getGroupId().equals(artifactReport.getGroupId())
                && dependency.getVersion().equals(artifactReport.getVersion());
    }

    private boolean hasWhitelistedGA(String groupId, String artifactId) {
        if (whiteArtifactService.getAll() == null) {
            return false;
        }
        return whiteArtifactService.getAll().stream()
                .anyMatch(white -> white.getArtifactId().equals(artifactId) && white.getGroupId().equals(groupId));
    }

    private void addWhitelistedGAVToCommunityGavsWithBuiltVersions(String groupId, String artifactId) {
        if (whiteArtifactService.getAll() == null) {
            return;
        }
        whiteArtifactService.getAll().stream()
                .filter(white -> white.getArtifactId().equals(artifactId) && white.getGroupId().equals(groupId))
                .forEach(white -> communityGavsWithBuiltVersions.add(new GAV(groupId, artifactId, white.getVersion())));
    }
}
