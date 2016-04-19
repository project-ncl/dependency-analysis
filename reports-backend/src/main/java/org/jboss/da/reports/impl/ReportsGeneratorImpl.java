package org.jboss.da.reports.impl;

import org.apache.maven.scm.ScmException;
import org.jboss.da.common.version.VersionParser;
import org.jboss.da.communication.aprox.FindGAVDependencyException;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.communication.pom.api.PomAnalyzer;
import org.jboss.da.communication.scm.api.SCMConnector;
import org.jboss.da.listings.api.model.ProductVersion;
import org.jboss.da.listings.api.model.ProductVersionArtifactRelationship;
import static org.jboss.da.listings.model.ProductSupportStatus.SUPERSEDED;
import static org.jboss.da.listings.model.ProductSupportStatus.SUPPORTED;
import static org.jboss.da.listings.model.ProductSupportStatus.UNKNOWN;
import org.jboss.da.listings.api.service.BlackArtifactService;
import org.jboss.da.listings.api.service.ProductVersionService;
import org.jboss.da.model.rest.GA;
import org.jboss.da.model.rest.GAV;
import org.jboss.da.reports.api.AdvancedArtifactReport;
import org.jboss.da.reports.api.AlignmentReportModule;
import org.jboss.da.reports.api.ArtifactReport;
import org.jboss.da.reports.api.Product;
import org.jboss.da.reports.api.ProductArtifact;
import org.jboss.da.reports.api.ReportsGenerator;
import org.jboss.da.reports.api.SCMLocator;
import org.jboss.da.reports.api.VersionLookupResult;
import org.jboss.da.reports.backend.api.VersionFinder;
import org.jboss.da.scm.api.SCM;
import org.jboss.da.scm.api.SCMType;
import org.slf4j.Logger;

import javax.inject.Inject;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.jboss.da.reports.backend.api.DependencyTreeGenerator;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The implementation of reports, which provides information about
 * built/not built artifacts/blacklisted artifacts
 *
 * @author Jakub Bartecek <jbartece@redhat.com>
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
public class ReportsGeneratorImpl implements ReportsGenerator {

    @Inject
    private Logger log;

    @Inject
    private VersionFinder versionFinder;

    @Inject
    private BlackArtifactService blackArtifactService;

    @Inject
    private DependencyTreeGenerator dependencyTreeGenerator;

    @Inject
    private SCM scmManager;

    @Inject
    private PomAnalyzer pomAnalyzer;

    @Inject
    private ProductVersionService productVersionService;

    @Inject
    private SCMConnector scmConnector;

    @Override
    public Optional<ArtifactReport> getReportFromSCM(SCMLocator scml) throws ScmException,
            PomAnalysisException, CommunicationException {
        if (scml == null)
            throw new IllegalArgumentException("SCM information can't be null");

        GAVDependencyTree dt = dependencyTreeGenerator.getDependencyTree(scml);

        VersionLookupResult result = versionFinder.lookupBuiltVersions(dt.getGav());
        ArtifactReport report = toArtifactReport(dt.getGav(), result);

        Set<GAVDependencyTree> nodesVisited = new HashSet<>();
        nodesVisited.add(dt);
        addDependencyReports(report, dt.getDependencies(), nodesVisited);

        return Optional.of(report);
    }

    @Override
    public Optional<AdvancedArtifactReport> getAdvancedReportFromSCM(SCMLocator scml) throws ScmException, PomAnalysisException, CommunicationException {
        Optional<ArtifactReport> artifactReport = getReportFromSCM(scml);
        // TODO: hardcoded to git
        // hopefully we'll get the cached cloned folder for this repo
        File repoFolder = scmManager.cloneRepository(SCMType.GIT, scml.getScmUrl(), scml.getRevision());
        return artifactReport.map(r -> generateAdvancedArtifactReport(r, repoFolder));
    }

    @Override
    public Optional<ArtifactReport> getReport(GAV gav, List<Product> products) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<AlignmentReportModule> getAligmentReport(SCMLocator scml,
            boolean useUnknownProducts, Set<Long> productIds) throws ScmException,
            PomAnalysisException {
        Map<GA, Set<GAV>> dependenciesOfModules = scmConnector.getDependenciesOfModules(
                scml.getScmUrl(), scml.getRevision(), scml.getPomPath(), scml.getRepositories());

        Set<AlignmentReportModule> ret = new HashSet<>();
        for (Map.Entry<GA, Set<GAV>> e : dependenciesOfModules.entrySet()) {
            AlignmentReportModule module = new AlignmentReportModule(e.getKey());
            Map<GAV, Set<ProductArtifact>> internallyBuilt = module.getInternallyBuilt();
            Map<GAV, Set<ProductArtifact>> differentVersion = module.getDifferentVersion();
            Set<GAV> notBuilt = module.getNotBuilt();
            Set<GAV> blacklisted = module.getBlacklisted();

            for (GAV gav : e.getValue()) {
                boolean bl = blackArtifactService.getArtifact(gav).isPresent();

                if (bl) {
                    blacklisted.add(gav);
                }

                Set<ProductArtifact> built;
                if (!bl) {
                    built = getBuiltInProducts(gav, productIds, useUnknownProducts);
                } else {
                    built = Collections.emptySet();
                }
                internallyBuilt.put(gav, built);

                Set<ProductArtifact> different;
                if (!bl && built.isEmpty()) {
                    different = getDifferentInProducts(gav, productIds, useUnknownProducts);
                } else {
                    different = Collections.emptySet();
                }
                differentVersion.put(gav, different);

                if (!bl && built.isEmpty() && different.isEmpty()) {
                    notBuilt.add(gav);
                }
            }
            ret.add(module);
        }
        return ret;
    }

    private Set<ProductArtifact> getBuiltInProducts(GAV gav, Set<Long> productIds,
            boolean useUnknownProducts) {
        Stream<ProductVersionArtifactRelationship> internallyStream = productVersionService
                .getProductVersionsWithArtifactByGAV(gav.getGroupId(), gav.getArtifactId(),
                        gav.getVersion()).stream();

        Set<ProductArtifact> built = filterAndMapProducts(productIds, internallyStream);

        if (useUnknownProducts) {
            try {
                Optional<String> bmv = versionFinder.getBestMatchVersionFor(gav);

                if (bmv.isPresent()) {
                    GAV bmgav = new GAV(gav.getGA(), bmv.get());
                    ProductArtifact pa = new ProductArtifact("Unknown", "Unknown", UNKNOWN, bmgav);
                    built.add(pa);
                }
            } catch (CommunicationException ex) {
                log.warn("Failed to get best match versions for " + gav);
            }
        }
        return built;
    }

    private Set<ProductArtifact> getDifferentInProducts(GAV gav, Set<Long> productIds,
            boolean useUnknownProducts) {
        Stream<ProductVersionArtifactRelationship> differentStream = productVersionService
                .getProductVersionsWithArtifactsByGA(gav.getGroupId(), gav.getArtifactId())
                .stream();

        Set<ProductArtifact> different = filterAndMapProducts(productIds, differentStream);

        if (useUnknownProducts) {
            try {
                List<String> bmvs = versionFinder.getBuiltVersionsFor(gav);

                for (String bmv : bmvs) {
                    GAV bmgav = new GAV(gav.getGA(), bmv);
                    ProductArtifact pa = new ProductArtifact("Unknown", "Unknown", UNKNOWN, bmgav);
                    different.add(pa);
                }
            } catch (CommunicationException ex) {
                log.warn("Failed to get best match versions for " + gav);
            }
        }

        return different;
    }

    private Set<ProductArtifact> filterAndMapProducts(Set<Long> productIds, Stream<ProductVersionArtifactRelationship> internallyStream) {
        if(productIds.isEmpty()){ // All SUPPORTED or SUPERSEDED
            internallyStream = internallyStream.filter(p ->
                    p.getProductVersion().getSupport() == SUPPORTED ||
                    p.getProductVersion().getSupport() == SUPERSEDED);
        }else{ // Specified
            internallyStream = internallyStream.filter(p -> productIds.contains(p.getProductVersion().getId()));
        }

        return internallyStream
                .map(x -> toProductArtifact(x))
                .collect(Collectors.toCollection(HashSet::new));
    }

    @Override
    public ArtifactReport getReport(GAV gav) throws CommunicationException,
            FindGAVDependencyException {
        if (gav == null)
            throw new IllegalArgumentException("GAV can't be null");

        GAVDependencyTree dt = dependencyTreeGenerator.getDependencyTree(gav);

        Set<GAVDependencyTree> nodesVisited = new HashSet<>();
        VersionLookupResult result = versionFinder.lookupBuiltVersions(gav);
        ArtifactReport report = toArtifactReport(gav, result);

        nodesVisited.add(dt);
        addDependencyReports(report, dt.getDependencies(), nodesVisited);

        return report;
    }

    private ArtifactReport toArtifactReport(GAV gav, VersionLookupResult result) {
        ArtifactReport report = new ArtifactReport(gav);
        report.addAvailableVersions(result.getAvailableVersions());
        report.setBestMatchVersion(result.getBestMatchVersion());
        report.setBlacklisted(blackArtifactService.isArtifactPresent(gav));
        report.setWhitelisted(getWhitelistedProducts(gav));
        return report;
    }

    private void addDependencyReports(ArtifactReport ar, Set<GAVDependencyTree> dependencyTree,
            Set<GAVDependencyTree> nodesVisited) throws CommunicationException {
        for (GAVDependencyTree dt : dependencyTree) {

            VersionLookupResult result = versionFinder.lookupBuiltVersions(dt.getGav());

            ArtifactReport dar = toArtifactReport(dt.getGav(), result);

            // if dt hasn't been visited yet, add dependencies of dt in the report
            if (!nodesVisited.contains(dt))
                addDependencyReports(dar, dt.getDependencies(), nodesVisited);

            ar.addDependency(dar);
            nodesVisited.add(dt);
        }
    }

    private AdvancedArtifactReport generateAdvancedArtifactReport(ArtifactReport report,
            File repoFolder) {
        AdvancedArtifactReport advancedReport = new AdvancedArtifactReport();
        advancedReport.setArtifactReport(report);
        Set<GAV> modulesAnalyzed = new HashSet<>();

        // hopefully we'll get the folder already cloned from before
        populateAdvancedArtifactReportFields(advancedReport, report, modulesAnalyzed, repoFolder);
        return advancedReport;
    }

    private void populateAdvancedArtifactReportFields(AdvancedArtifactReport advancedReport,
            ArtifactReport report, Set<GAV> modulesAnalyzed, File repoFolder) {
        for (ArtifactReport dep : report.getDependencies()) {
            final GAV gav = dep.getGav();
            if (modulesAnalyzed.contains(gav)) {
                // if module already analyzed, skip
                continue;
            } else if (isDependencyAModule(repoFolder, dep)) {
                // if dependency is a module, but not yet analyzed
                modulesAnalyzed.add(gav);
                populateAdvancedArtifactReportFields(advancedReport, dep, modulesAnalyzed,
                        repoFolder);
            } else {
                // only generate populate advanced report with community GAVs
                if (VersionParser.isRedhatVersion(dep.getVersion()))
                    continue;

                // we have a top-level module dependency
                if (!dep.getWhitelisted().isEmpty())
                    advancedReport.addWhitelistedArtifact(gav, new HashSet<>(dep.getWhitelisted()));
                if (dep.isBlacklisted())
                    advancedReport.addBlacklistedArtifact(gav);

                if (dep.getBestMatchVersion().isPresent()) {
                    advancedReport.addCommunityGavWithBestMatchVersion(gav, dep
                            .getBestMatchVersion().get());
                } else {
                    Set<String> versions = getWhitelistedVersions(dep.getGroupId(),
                            dep.getArtifactId());
                    if (!dep.getAvailableVersions().isEmpty() || !versions.isEmpty()) {
                        versions.addAll(dep.getAvailableVersions());
                        advancedReport.addCommunityGavWithBuiltVersion(dep.getGav(), versions);
                    } else {
                        advancedReport.addCommunityGav(gav);
                    }
                }
            }
        }
    }

    private Set<String> getWhitelistedVersions(String groupId, String artifactId) {
        return productVersionService.getProductVersionsWithArtifactsByGA(groupId, artifactId)
                .stream()
                .map(rel -> rel.getArtifact().getVersion())
                .collect(Collectors.toCollection(() -> new HashSet<>()));
    }

    private boolean isDependencyAModule(File repoFolder, ArtifactReport dependency) {
        return pomAnalyzer.getPOMFileForGAV(repoFolder, dependency.getGav()).isPresent();
    }

    private List<ProductVersion> getWhitelistedProducts(GAV gav) {
        return productVersionService.getProductVersionsOfArtifact(gav.getGroupId(),
                gav.getArtifactId(), gav.getVersion());
    }

    private ProductArtifact toProductArtifact(ProductVersionArtifactRelationship pa) {
        ProductArtifact ret = new ProductArtifact();
        GAV gav = new GAV(pa.getArtifact().getGa().getGroupId(), pa.getArtifact().getGa()
                .getArtifactId(), pa.getArtifact().getVersion());
        ret.setArtifact(gav);
        ret.setProductName(pa.getProductVersion().getProduct().getName());
        ret.setProductVersion(pa.getProductVersion().getProductVersion());
        ret.setSupportStatus(pa.getProductVersion().getSupport());
        return ret;
    }

}
