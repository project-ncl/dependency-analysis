package org.jboss.da.rest.facade;

import org.apache.maven.scm.ScmException;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.listings.model.rest.RestProductInput;
import org.jboss.da.model.rest.GAV;
import org.jboss.da.reports.api.AlignmentReportModule;
import org.jboss.da.reports.api.BuiltReportModule;
import org.jboss.da.reports.api.ProductArtifact;
import org.jboss.da.reports.api.ReportsGenerator;
import org.jboss.da.reports.model.api.SCMLocator;
import org.jboss.da.reports.model.rest.AlignReport;
import org.jboss.da.reports.model.rest.AlignReportRequest;
import org.jboss.da.reports.model.rest.BuiltReport;
import org.jboss.da.reports.model.rest.BuiltReportRequest;
import org.jboss.da.reports.model.rest.RestGA2GAVs;
import org.jboss.da.reports.model.rest.RestGA2RestGAV2VersionProducts;
import org.jboss.da.reports.model.rest.RestGAV2VersionProducts;
import org.jboss.da.reports.model.rest.RestVersionProduct;

import javax.inject.Inject;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jboss.da.model.rest.VersionComparator;
import org.jboss.da.reports.model.rest.RestGA2RestGAV2VersionProductsWithDiff;
import org.jboss.da.reports.model.rest.RestGAV2VersionProductsWithDiff;
import org.jboss.da.reports.model.rest.RestVersionProductWithDifference;

public class ReportsFacade {

    @Inject
    private ReportsGenerator reportsGenerator;

    public Set<BuiltReport> builtReport(BuiltReportRequest request) throws ScmException,
            PomAnalysisException, CommunicationException {
        String pomPath = getPomPath(request.getPomPath());
        SCMLocator locator = SCMLocator.generic(request.getScmUrl(), request.getRevision(),
                pomPath, request.getAdditionalRepos());

        Set<BuiltReportModule> builtReport = reportsGenerator.getBuiltReport(locator);
        return toBuiltReport(builtReport);
    }

    public AlignReport alignReport(AlignReportRequest request) throws ScmException,
            PomAnalysisException {
        String pomPath = getPomPath(request.getPomPath());
        SCMLocator locator = SCMLocator.generic(request.getScmUrl(), request.getRevision(),
                pomPath, request.getAdditionalRepos());

        Set<AlignmentReportModule> aligmentReport = reportsGenerator.getAligmentReport(locator,
                request.isSearchUnknownProducts(), request.getProducts());
        return toAlignReport(aligmentReport);
    }

    private String getPomPath(String requestPomPath) {
        if (requestPomPath == null || requestPomPath.isEmpty()) {
            return "pom.xml";
        }
        return requestPomPath;
    }

    private Set<BuiltReport> toBuiltReport(Set<BuiltReportModule> builtReport) {
        Set<BuiltReport> result = new HashSet<>();
        for (BuiltReportModule b : builtReport) {
            BuiltReport report = new BuiltReport();
            report.setArtifactId(b.getArtifactId());
            report.setGroupId(b.getGroupId());
            report.setVersion(b.getVersion());
            report.setBuiltVersion(b.getBuiltVersion());
            report.setAvailableVersions(b.getAvailableVersions());
            result.add(report);
        }
        return result;
    }

    private AlignReport toAlignReport(Set<AlignmentReportModule> aligmentReport) {
        AlignReport ret = new AlignReport();

        Set<RestGA2RestGAV2VersionProducts> internallyBuilt = ret.getInternallyBuilt();
        Set<RestGA2RestGAV2VersionProductsWithDiff> builtInDifferentVersion = ret
                .getBuiltInDifferentVersion();
        Set<RestGA2GAVs> notBuilt = ret.getNotBuilt();
        Set<RestGA2GAVs> blacklisted = ret.getBlacklisted();

        for (AlignmentReportModule module : aligmentReport) {
            Set<RestGAV2VersionProducts> ib = toRestGAV2VersionProducts(module.getInternallyBuilt());
            Set<RestGAV2VersionProductsWithDiff> dv = toRestGAV2VersionProductsWithDiff(module
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

    private Set<RestGAV2VersionProducts> toRestGAV2VersionProducts(Map<GAV, Set<ProductArtifact>> ib) {
        return ib.entrySet().stream()
                .filter(e -> !e.getValue().isEmpty())
                .map(e -> toRestGAV2VersionProducts(e.getKey(), e.getValue()))
                .collect(Collectors.toSet());
    }

    private Set<RestGAV2VersionProductsWithDiff> toRestGAV2VersionProductsWithDiff(Map<GAV, Set<ProductArtifact>> ib) {
        return ib.entrySet().stream()
                .filter(e -> !e.getValue().isEmpty())
                .map(e -> toRestGAV2VersionProductsWithDiff(e.getKey(), e.getValue()))
                .collect(Collectors.toSet());

    }

    private RestGAV2VersionProducts toRestGAV2VersionProducts(GAV gav,
            Set<ProductArtifact> productArtifacts) {
        RestGAV2VersionProducts ret = new RestGAV2VersionProducts();
        ret.setGroupId(gav.getGroupId());
        ret.setArtifactId(gav.getArtifactId());
        ret.setVersion(gav.getVersion());
        ret.setGavProducts(toRestVersionProducts(productArtifacts));
        return ret;
    }

    private RestGAV2VersionProductsWithDiff toRestGAV2VersionProductsWithDiff(GAV gav,
            Set<ProductArtifact> productArtifacts) {
        RestGAV2VersionProductsWithDiff ret = new RestGAV2VersionProductsWithDiff();
        ret.setGroupId(gav.getGroupId());
        ret.setArtifactId(gav.getArtifactId());
        ret.setVersion(gav.getVersion());
        ret.setGavProducts(toRestVersionProductsWithDiff(productArtifacts));
        return ret;
    }

    private Set<RestVersionProduct> toRestVersionProducts(Set<ProductArtifact> productArtifacts) {
        return productArtifacts.stream()
                .map(x -> toRestVersionProduct(x))
                .collect(Collectors.toSet());
    }

    private Set<RestVersionProductWithDifference> toRestVersionProductsWithDiff(Set<ProductArtifact> productArtifacts) {
        return productArtifacts.stream()
                .map(x -> toRestVersionProductWithDifference(x))
                .collect(Collectors.toSet());
    }

    private RestVersionProduct toRestVersionProduct(ProductArtifact productArtifact) {
        RestVersionProduct ret = new RestVersionProduct();
        ret.setVersion(productArtifact.getArtifact().getVersion());
        RestProductInput rpi = new RestProductInput(productArtifact.getProductName(),
                productArtifact.getProductVersion(), productArtifact.getSupportStatus());
        ret.setProduct(rpi);
        return ret;
    }

    private RestVersionProductWithDifference toRestVersionProductWithDifference(
            ProductArtifact productArtifact) {
        RestVersionProductWithDifference ret = new RestVersionProductWithDifference();
        ret.setVersion(productArtifact.getArtifact().getVersion());
        RestProductInput rpi = new RestProductInput(productArtifact.getProductName(),
                productArtifact.getProductVersion(), productArtifact.getSupportStatus());
        ret.setProduct(rpi);
        ret.setDifferenceType(productArtifact.getDifferenceType());
        return ret;
    }
}
