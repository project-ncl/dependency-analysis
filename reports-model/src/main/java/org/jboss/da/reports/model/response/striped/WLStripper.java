package org.jboss.da.reports.model.response.striped;

import org.jboss.da.reports.model.response.*;
import org.jboss.da.reports.model.response.RestVersionProduct;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class WLStripper {

    public static AdvancedReport strip(org.jboss.da.reports.model.response.AdvancedReport report) {
        return new AdvancedReport(
                strip(report.getReport()),
                report.getBlacklistedArtifacts(),
                report.getCommunityGavsWithBestMatchVersions(),
                report.getCommunityGavsWithBuiltVersions(),
                report.getCommunityGavs());
    }

    public static AlignReport strip(org.jboss.da.reports.model.response.AlignReport report) {
        List<AlignReportModule> internallyBuilt = report.getInternallyBuilt()
                .stream()
                .map(WLStripper::map)
                .collect(Collectors.toList());
        List<AlignReportModuleWithDiff> builtInDifferentVersion = report.getBuiltInDifferentVersion()
                .stream()
                .map(WLStripper::map)
                .collect(Collectors.toList());

        return new AlignReport(internallyBuilt, builtInDifferentVersion, report.getNotBuilt(), report.getBlacklisted());
    }

    private static AlignReportModule map(RestGA2RestGAV2VersionProducts module) {
        List<GAVAvailableVersions> dependencies = module.getGavProducts()
                .stream()
                .map(WLStripper::map)
                .collect(Collectors.toList());
        return new AlignReportModule(module.getGroupId(), module.getArtifactId(), dependencies);
    }

    private static AlignReportModuleWithDiff map(RestGA2RestGAV2VersionProductsWithDiff module) {
        List<GAVAvailableVersionsWithDiff> dependencies = module.getGavProducts()
                .stream()
                .map(WLStripper::map)
                .collect(Collectors.toList());
        return new AlignReportModuleWithDiff(module.getGroupId(), module.getArtifactId(), dependencies);
    }

    private static GAVAvailableVersions map(RestGAV2VersionProducts gavAV) {
        Set<String> versions = gavAV.getGavProducts()
                .stream()
                .map(RestVersionProduct::getVersion)
                .collect(Collectors.toSet());
        return new GAVAvailableVersions(gavAV.getGroupId(), gavAV.getArtifactId(), gavAV.getVersion(), versions);
    }

    private static GAVAvailableVersionsWithDiff map(RestGAV2VersionProductsWithDiff gavAV) {
        List<VersionWithDifference> versionsWithDiff = gavAV.getGavProducts()
                .stream()
                .map(WLStripper::map)
                .collect(Collectors.toList());
        return new GAVAvailableVersionsWithDiff(
                gavAV.getGroupId(),
                gavAV.getArtifactId(),
                gavAV.getVersion(),
                versionsWithDiff);
    }

    private static VersionWithDifference map(RestVersionProductWithDifference versionWithDiff) {
        return new VersionWithDifference(versionWithDiff.getVersion(), versionWithDiff.getDifferenceType());
    }

    public static Report strip(org.jboss.da.reports.model.response.Report report) {
        return new Report(
                report.getGroupId(),
                report.getArtifactId(),
                report.getVersion(),
                report.getAvailableVersions(),
                report.getBestMatchVersion(),
                report.isDependencyVersionsSatisfied(),
                report.getDependencies().stream().map(WLStripper::strip).collect(Collectors.toList()),
                report.isBlacklisted(),
                report.getNotBuiltDependencies());
    }
}
