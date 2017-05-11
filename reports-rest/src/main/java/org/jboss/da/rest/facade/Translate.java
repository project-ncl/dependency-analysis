package org.jboss.da.rest.facade;

import org.jboss.da.listings.model.rest.RestGavProducts;
import org.jboss.da.listings.model.rest.RestProductInput;
import org.jboss.da.model.rest.GAV;
import org.jboss.da.products.api.Product;
import org.jboss.da.reports.api.AlignmentReportModule;
import org.jboss.da.reports.api.BuiltReportModule;
import org.jboss.da.reports.api.ProductArtifact;
import org.jboss.da.reports.model.rest.AlignReport;
import org.jboss.da.reports.model.rest.BuiltReport;
import org.jboss.da.reports.model.rest.RestGA2GAVs;
import org.jboss.da.reports.model.rest.RestGA2RestGAV2VersionProducts;
import org.jboss.da.reports.model.rest.RestGAV2VersionProducts;
import org.jboss.da.reports.model.rest.RestVersionProduct;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.da.reports.api.AdvancedArtifactReport;
import org.jboss.da.reports.api.ArtifactReport;
import org.jboss.da.reports.model.rest.AdvancedReport;
import org.jboss.da.reports.model.rest.GAVAvailableVersions;
import org.jboss.da.reports.model.rest.GAVBestMatchVersion;
import org.jboss.da.reports.model.rest.Report;
import org.jboss.da.reports.model.rest.RestGA2RestGAV2VersionProductsWithDiff;
import org.jboss.da.reports.model.rest.RestGAV2VersionProductsWithDiff;
import org.jboss.da.reports.model.rest.RestVersionProductWithDifference;

import java.util.ArrayList;
import java.util.List;

class Translate {

    static Report toReport(ArtifactReport report) {
        List<Report> dependencies = report.getDependencies().stream()
                .map(Translate::toReport)
                .collect(Collectors.toList());
        return new Report(report.getGav(),
                new ArrayList<>(report.getAvailableVersions()),
                report.getBestMatchVersion().orElse(null),
                report.isDependencyVersionSatisfied(),
                dependencies,
                report.isBlacklisted(),
                toWhitelisted(report.getWhitelisted()),
                report.getNotBuiltDependencies());
    }

    private static Set<GAVBestMatchVersion> toGAVBestMatchVersions(Map<GAV, String> bestMatchVersions) {
        return bestMatchVersions.entrySet().stream()
                .map(e -> new GAVBestMatchVersion(e.getKey(), e.getValue()))
                .collect(Collectors.toSet());
    }

    private static List<RestVersionProductWithDifference> toRestVersionProductsWithDiff(Set<ProductArtifact> productArtifacts) {
        return productArtifacts.stream()
                .map(Translate::toRestVersionProductWithDifference)
                .collect(Collectors.toList());
    }

    private static List<RestGAV2VersionProducts> toRestGAV2VersionProducts(Map<GAV, Set<ProductArtifact>> ib) {
        return ib.entrySet().stream()
                .filter(e -> !e.getValue().isEmpty())
                .map(e -> toRestGAV2VersionProducts(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private static RestGAV2VersionProducts toRestGAV2VersionProducts(GAV gav,
            Set<ProductArtifact> productArtifacts) {
        RestGAV2VersionProducts ret = new RestGAV2VersionProducts();
        ret.setGroupId(gav.getGroupId());
        ret.setArtifactId(gav.getArtifactId());
        ret.setVersion(gav.getVersion());
        ret.setGavProducts(toRestVersionProducts(productArtifacts));
        return ret;
    }

    private static Set<GAVAvailableVersions> toGAVAvailableVersions(Map<GAV, Set<String>> buildVersions) {
        return buildVersions.entrySet().stream()
                .map(e -> new GAVAvailableVersions(e.getKey(), e.getValue()))
                .collect(Collectors.toSet());
    }

    private static RestVersionProduct toRestVersionProduct(ProductArtifact productArtifact) {
        RestVersionProduct ret = new RestVersionProduct();
        ret.setVersion(productArtifact.getArtifact().getVersion());
        RestProductInput rpi = new RestProductInput(productArtifact.getProductName(),
                productArtifact.getProductVersion(), productArtifact.getSupportStatus());
        ret.setProduct(rpi);
        return ret;
    }

    private static RestVersionProductWithDifference toRestVersionProductWithDifference(
            ProductArtifact productArtifact) {
        RestVersionProductWithDifference ret = new RestVersionProductWithDifference();
        ret.setVersion(productArtifact.getArtifact().getVersion());
        RestProductInput rpi = new RestProductInput(productArtifact.getProductName(),
                productArtifact.getProductVersion(), productArtifact.getSupportStatus());
        ret.setProduct(rpi);
        ret.setDifferenceType(productArtifact.getDifferenceType());
        return ret;
    }

    private static RestProductInput toRestProductInput(Product product) {
        RestProductInput ret = new RestProductInput();
        ret.setName(product.getName());
        ret.setVersion(product.getVersion());
        ret.setSupportStatus(product.getStatus());
        return ret;
    }

    static AdvancedReport toAdvancedReport(AdvancedArtifactReport advancedArtifactReport) {
        Report report = toReport(advancedArtifactReport.getArtifactReport());
        return new AdvancedReport(report, advancedArtifactReport.getBlacklistedArtifacts(),
                toRestGAVProducts(advancedArtifactReport.getWhitelistedArtifacts()),
                toGAVBestMatchVersions(advancedArtifactReport
                        .getCommunityGavsWithBestMatchVersions()),
                toGAVAvailableVersions(advancedArtifactReport.getCommunityGavsWithBuiltVersions()),
                advancedArtifactReport.getCommunityGavs());
    }

    private static List<RestProductInput> toWhitelisted(List<Product> whitelisted) {
        return whitelisted.stream()
                .map(pv -> new RestProductInput(pv.getName(), pv.getVersion(), pv.getStatus()))
                .collect(Collectors.toList());
    }

    static AlignReport toAlignReport(Set<AlignmentReportModule> aligmentReport) {
        AlignReport ret = new AlignReport();
        List<RestGA2RestGAV2VersionProducts> internallyBuilt = ret.getInternallyBuilt();
        List<RestGA2RestGAV2VersionProductsWithDiff> builtInDifferentVersion = ret
                .getBuiltInDifferentVersion();
        List<RestGA2GAVs> notBuilt = ret.getNotBuilt();
        List<RestGA2GAVs> blacklisted = ret.getBlacklisted();
        for (AlignmentReportModule module : aligmentReport) {
            List<RestGAV2VersionProducts> ib = toRestGAV2VersionProducts(module
                    .getInternallyBuilt());
            List<RestGAV2VersionProductsWithDiff> dv = toRestGAV2VersionProductsWithDiff(module
                    .getDifferentVersion());
            Set<GAV> nb = module.getNotBuilt();
            Set<GAV> bl = module.getBlacklisted();
            if (!ib.isEmpty()) {
                RestGA2RestGAV2VersionProducts ibm = new RestGA2RestGAV2VersionProducts();
                ibm.setGroupId(module.getModule().getGroupId());
                ibm.setArtifactId(module.getModule().getArtifactId());
                ibm.setGavProducts(ib);
                internallyBuilt.add(ibm);
            }
            if (!dv.isEmpty()) {
                RestGA2RestGAV2VersionProductsWithDiff dvm = new RestGA2RestGAV2VersionProductsWithDiff();
                dvm.setGroupId(module.getModule().getGroupId());
                dvm.setArtifactId(module.getModule().getArtifactId());
                dvm.setGavProducts(dv);
                builtInDifferentVersion.add(dvm);
            }
            if (!nb.isEmpty()) {
                RestGA2GAVs nbm = new RestGA2GAVs();
                nbm.setGroupId(module.getModule().getGroupId());
                nbm.setArtifactId(module.getModule().getArtifactId());
                nbm.setGavs(nb);
                notBuilt.add(nbm);
            }
            if (!bl.isEmpty()) {
                RestGA2GAVs blm = new RestGA2GAVs();
                blm.setGroupId(module.getModule().getGroupId());
                blm.setArtifactId(module.getModule().getArtifactId());
                blm.setGavs(bl);
                blacklisted.add(blm);
            }
        }
        return ret;
    }

    private static List<RestGAV2VersionProductsWithDiff> toRestGAV2VersionProductsWithDiff(Map<GAV, Set<ProductArtifact>> ib) {
        return ib.entrySet().stream()
                .filter(e -> !e.getValue().isEmpty())
                .map(e -> toRestGAV2VersionProductsWithDiff(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private static RestGAV2VersionProductsWithDiff toRestGAV2VersionProductsWithDiff(GAV gav,
            Set<ProductArtifact> productArtifacts) {
        RestGAV2VersionProductsWithDiff ret = new RestGAV2VersionProductsWithDiff();
        ret.setGroupId(gav.getGroupId());
        ret.setArtifactId(gav.getArtifactId());
        ret.setVersion(gav.getVersion());
        ret.setGavProducts(toRestVersionProductsWithDiff(productArtifacts));
        return ret;
    }

    static Set<BuiltReport> toBuiltReport(Set<BuiltReportModule> builtReport) {
        return builtReport.stream()
                .map(Translate::toBuiltReport)
                .collect(Collectors.toSet());
    }

    private static BuiltReport toBuiltReport(BuiltReportModule b) {
        BuiltReport report = new BuiltReport();
        report.setArtifactId(b.getArtifactId());
        report.setGroupId(b.getGroupId());
        report.setVersion(b.getVersion());
        report.setBuiltVersion(b.getBuiltVersion());
        report.setAvailableVersions(b.getAvailableVersions());
        return report;
    }

    private static List<RestVersionProduct> toRestVersionProducts(Set<ProductArtifact> productArtifacts) {
        return productArtifacts.stream()
                .map(Translate::toRestVersionProduct)
                .collect(Collectors.toList());
    }

    private static Set<RestProductInput> toRestProductInputs(Set<Product> product) {
        return product.stream()
                .map(Translate::toRestProductInput)
                .collect(Collectors.toSet());
    }

    private static Set<RestGavProducts> toRestGAVProducts(Map<GAV, Set<Product>> whitelistedArtifacts) {
        return whitelistedArtifacts.entrySet().stream()
                .map(e -> new RestGavProducts(e.getKey(), toRestProductInputs(e.getValue())))
                .collect(Collectors.toSet());
    }
}
