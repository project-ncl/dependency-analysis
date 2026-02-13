package org.jboss.da.reports.impl;

import lombok.Data;

import org.apache.commons.lang.BooleanUtils;
import org.apache.maven.scm.ScmException;
import org.codehaus.plexus.util.StringUtils;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.common.json.LookupMode;
import org.jboss.da.common.util.Configuration;
import org.jboss.da.common.util.ConfigurationParseException;
import org.jboss.da.common.util.UserLog;
import org.jboss.da.common.version.SuffixedVersion;
import org.jboss.da.common.version.VersionAnalyzer;
import org.jboss.da.common.version.VersionComparator;
import org.jboss.da.common.version.VersionParser;
import org.jboss.da.communication.indy.model.GAVDependencyTree;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.communication.pom.api.PomAnalyzer;
import org.jboss.da.communication.scm.api.SCMConnector;
import org.jboss.da.listings.api.service.BlackArtifactService;
import org.jboss.da.listings.model.rest.RestProductInput;
import org.jboss.da.model.rest.GA;
import org.jboss.da.model.rest.GAV;
import org.jboss.da.model.rest.NPMPackage;
import org.jboss.da.products.api.Artifact;
import org.jboss.da.products.api.MavenArtifact;
import org.jboss.da.products.api.NPMArtifact;
import org.jboss.da.products.api.Product;
import org.jboss.da.products.api.ProductArtifacts;
import org.jboss.da.products.api.ProductProvider;
import org.jboss.da.products.impl.AggregatedProductProvider;
import org.jboss.da.products.impl.PncProductProvider;
import org.jboss.da.products.impl.PncProductProvider.Pnc;
import org.jboss.da.products.impl.RepositoryProductProvider;
import org.jboss.da.products.impl.RepositoryProductProvider.Repository;
import org.jboss.da.reports.api.AdvancedArtifactReport;
import org.jboss.da.reports.api.AlignmentReportModule;
import org.jboss.da.reports.api.ArtifactReport;
import org.jboss.da.reports.api.BuiltReportModule;
import org.jboss.da.reports.api.ProductArtifact;
import org.jboss.da.reports.api.ReportsGenerator;
import org.jboss.da.reports.backend.api.DependencyTreeGenerator;
import org.jboss.da.reports.backend.impl.ProductAdapter;
import org.jboss.da.reports.model.api.SCMLocator;
import org.jboss.da.reports.model.request.LookupGAVsRequest;
import org.jboss.da.reports.model.request.LookupNPMRequest;
import org.jboss.da.reports.model.request.SCMReportRequest;
import org.jboss.da.reports.model.request.VersionsNPMRequest;
import org.jboss.da.reports.model.response.LookupReport;
import org.jboss.da.reports.model.response.NPMLookupReport;
import org.jboss.da.reports.model.response.NPMVersionsReport;
import org.jboss.da.scm.api.SCM;
import org.jboss.da.scm.api.SCMType;
import org.jboss.pnc.api.dependencyanalyzer.dto.QualifiedVersion;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.BuildCategory;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.ValidationException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.jboss.da.listings.model.ProductSupportStatus.SUPERSEDED;
import static org.jboss.da.listings.model.ProductSupportStatus.SUPPORTED;
import static org.jboss.da.products.api.Product.UNKNOWN;

/**
 * The implementation of reports, which provides information about built/not built artifacts/blacklisted artifacts
 *
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@ApplicationScoped
public class ReportsGeneratorImpl implements ReportsGenerator {

    public static final String DEFAULT_SUFFIX = "redhat";

    @Inject
    private Logger log;

    @Inject
    @UserLog
    private Logger userLog;

    @Inject
    private BlackArtifactService blackArtifactService;

    @Inject
    private DependencyTreeGenerator dependencyTreeGenerator;

    @Inject
    private SCM scmManager;

    @Inject
    private PomAnalyzer pomAnalyzer;

    @Inject
    private SCMConnector scmConnector;

    @Inject
    private AggregatedProductProvider aggProductProvider;

    @Inject
    @Pnc
    private PncProductProvider pncProductProvider;

    @Inject
    @Repository
    private RepositoryProductProvider repositoryProductProvider;

    @Inject
    private ProductAdapter productAdapter;

    private Map<String, LookupMode> modes;

    @Inject
    public ReportsGeneratorImpl(Configuration config) throws ConfigurationParseException {
        modes = config.getConfig()
                .getModes()
                .stream()
                .collect(Collectors.toMap(LookupMode::getName, Function.identity()));
    }

    @Override
    public Optional<ArtifactReport> getReportFromSCM(SCMReportRequest scml)
            throws ScmException, PomAnalysisException, CommunicationException {
        if (scml == null) {
            throw new IllegalArgumentException("SCM information can't be null");
        }

        Set<Product> products = productAdapter.toProducts(scml.getProductNames(), scml.getProductVersionIds());
        GAVDependencyTree dt = dependencyTreeGenerator.getDependencyTree(scml.getScml());

        return createReport(dt, products);
    }

    private Optional<ArtifactReport> createReport(GAVDependencyTree dt, Set<Product> products)
            throws CommunicationException {
        ArtifactReport report = new ArtifactReport(dt.getGav());

        Set<GAVDependencyTree> nodesVisited = new HashSet<>();
        nodesVisited.add(dt);
        addDependencyReports(report, dt.getDependencies(), nodesVisited);

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        traverseAndFill(report, products, futures);

        FuturesUtil.joinFutures(futures);

        return Optional.of(report);
    }

    private void addDependencyReports(
            ArtifactReport ar,
            Set<GAVDependencyTree> dependencyTree,
            Set<GAVDependencyTree> nodesVisited) throws CommunicationException {
        for (GAVDependencyTree dt : dependencyTree) {

            ArtifactReport dar = new ArtifactReport(dt.getGav());

            // if dt hasn't been visited yet, add dependencies of dt in the report
            if (!nodesVisited.contains(dt)) {
                addDependencyReports(dar, dt.getDependencies(), nodesVisited);
            }

            ar.addDependency(dar);
            nodesVisited.add(dt);
        }
    }

    private void traverseAndFill(ArtifactReport report, Set<Product> products, List<CompletableFuture<Void>> futures) {
        futures.add(fillArtifactReport(report, products));
        for (ArtifactReport dep : report.getDependencies()) {
            traverseAndFill(dep, products, futures);
        }
    }

    private CompletableFuture<Void> fillArtifactReport(ArtifactReport report, Set<Product> products) {
        GAV gav = report.getGav();

        CompletableFuture<Set<ProductArtifacts>> artifacts = aggProductProvider.getArtifacts(new MavenArtifact(gav));
        artifacts = filterProductArtifacts(products, artifacts);

        report.setBlacklisted(blackArtifactService.isArtifactPresent(gav));
        List<String> suffixes = Collections.singletonList(DEFAULT_SUFFIX);

        CompletableFuture<Void> fillVersions = analyzeVersions(suffixes, gav.getVersion(), artifacts).thenAccept(v -> {
            report.setAvailableVersions(v.getAvailableVersions());
            report.setBestMatchVersion(v.getBestMatchVersion());
        });

        CompletableFuture<Void> fillWhitelist = artifacts.thenAccept(pas -> {
            List<Product> whiteProducts = pas.stream()
                    .map(pa -> pa.getProduct())
                    .filter(p -> !UNKNOWN.equals(p))
                    .collect(Collectors.toList());
            report.setWhitelisted(whiteProducts);
        });

        return CompletableFuture.allOf(fillVersions, fillWhitelist);
    }

    private CompletableFuture<VersionAnalysisResult> analyzeVersions(
            List<String> suffixes,
            String version,
            CompletableFuture<Set<ProductArtifacts>> availableArtifacts) {
        VersionAnalyzer va = new VersionAnalyzer(suffixes);
        return availableArtifacts.thenApply(pas -> {
            List<String> versions = pas.stream()
                    .flatMap(as -> as.getArtifacts().stream())
                    .map(Artifact::getVersion)
                    .collect(Collectors.toList());

            Optional<String> bmv = va.findBiggestMatchingVersion(
                    version,
                    versions.stream().map(QualifiedVersion::new).collect(Collectors.toList()));
            List<String> sortedVersions = va.sortVersions(version, versions);

            return new VersionAnalysisResult(bmv, sortedVersions);
        });
    }

    @Override
    public Optional<AdvancedArtifactReport> getAdvancedReportFromSCM(SCMReportRequest request)
            throws ScmException, PomAnalysisException, CommunicationException {

        SCMLocator scml = request.getScml();
        Set<Product> products = productAdapter.toProducts(request.getProductNames(), request.getProductVersionIds());

        GAVDependencyTree dt = dependencyTreeGenerator.getDependencyTree(scml);
        Optional<ArtifactReport> artifactReport = createReport(dt, products);
        // TODO: hardcoded to git
        // hopefully we'll get the cached cloned folder for this repo
        File repoFolder = scmManager.cloneRepository(SCMType.GIT, scml.getScmUrl(), scml.getRevision());
        return artifactReport.map(r -> generateAdvancedArtifactReport(r, repoFolder));
    }

    private AdvancedArtifactReport generateAdvancedArtifactReport(ArtifactReport report, File repoFolder) {
        AdvancedArtifactReport advancedReport = new AdvancedArtifactReport();
        advancedReport.setArtifactReport(report);
        Set<GAV> modulesAnalyzed = new HashSet<>();

        // hopefully we'll get the folder already cloned from before
        populateAdvancedArtifactReportFields(advancedReport, report, modulesAnalyzed, repoFolder);
        return advancedReport;
    }

    private void populateAdvancedArtifactReportFields(
            AdvancedArtifactReport advancedReport,
            ArtifactReport report,
            Set<GAV> modulesAnalyzed,
            File repoFolder) {
        VersionParser parser = new VersionParser(DEFAULT_SUFFIX);

        for (ArtifactReport dep : report.getDependencies()) {
            final GAV gav = dep.getGav();
            if (modulesAnalyzed.contains(gav)) {
                // if module already analyzed, skip
                continue;
            } else if (isDependencyAModule(repoFolder, dep)) {
                // if dependency is a module, but not yet analyzed
                modulesAnalyzed.add(gav);
                populateAdvancedArtifactReportFields(advancedReport, dep, modulesAnalyzed, repoFolder);
            } else {
                // only generate populate advanced report with community GAVs
                if (parser.parse(dep.getVersion()).isSuffixed()) {
                    continue;
                }

                // we have a top-level module dependency
                if (!dep.getWhitelisted().isEmpty()) {
                    advancedReport.addWhitelistedArtifact(gav, new HashSet<>(dep.getWhitelisted()));
                }
                if (dep.isBlacklisted()) {
                    advancedReport.addBlacklistedArtifact(gav);
                }

                if (dep.getBestMatchVersion().isPresent()) {
                    advancedReport.addCommunityGavWithBestMatchVersion(gav, dep.getBestMatchVersion().get());
                } else {
                    if (!dep.getAvailableVersions().isEmpty()) {
                        Set<String> versions = new TreeSet<>(new VersionComparator(gav.getVersion(), parser));
                        versions.addAll(dep.getAvailableVersions());
                        advancedReport.addCommunityGavWithBuiltVersion(dep.getGav(), versions);
                    } else {
                        advancedReport.addCommunityGav(gav);
                    }
                }
            }
        }
    }

    private boolean isDependencyAModule(File repoFolder, ArtifactReport dependency) {
        return pomAnalyzer.getPOMFileForGAV(repoFolder, dependency.getGav()).isPresent();
    }

    @Override
    public Set<AlignmentReportModule> getAligmentReport(
            SCMLocator scml,
            boolean useUnknownProduct,
            Set<Long> productIds) throws ScmException, PomAnalysisException, CommunicationException {
        VersionParser versionParser = new VersionParser(DEFAULT_SUFFIX);
        Map<GA, Set<GAV>> dependenciesOfModules = scmConnector.getDependenciesOfModules(
                scml.getScmUrl(),
                scml.getRevision(),
                scml.getPomPath(),
                scml.getRepositories());
        Set<Product> products = productAdapter.toProducts(Collections.emptySet(), productIds);

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        Set<AlignmentReportModule> ret = new TreeSet<>(Comparator.comparing(x -> x.getModule()));
        for (Map.Entry<GA, Set<GAV>> e : dependenciesOfModules.entrySet()) {
            AlignmentReportModule module = new AlignmentReportModule(e.getKey());
            Map<GAV, Set<ProductArtifact>> internallyBuilt = module.getInternallyBuilt();
            Map<GAV, Set<ProductArtifact>> differentVersion = module.getDifferentVersion();
            Set<GAV> notBuilt = module.getNotBuilt();
            Set<GAV> blacklisted = module.getBlacklisted();
            ret.add(module);

            Map<GAV, CompletableFuture<Set<ProductArtifact>>> intr = new HashMap<>();
            Map<GAV, CompletableFuture<Set<ProductArtifact>>> diff = new HashMap<>();

            for (GAV gav : e.getValue()) {
                boolean bl = blackArtifactService.getArtifact(gav).isPresent();

                if (bl) {
                    blacklisted.add(gav);
                    internallyBuilt.put(gav, Collections.emptySet());
                    differentVersion.put(gav, Collections.emptySet());
                    continue;
                }

                CompletableFuture<Set<ProductArtifacts>> artifacts = filterProducts(
                        useUnknownProduct,
                        products,
                        aggProductProvider.getArtifacts(new MavenArtifact(gav)));
                CompletableFuture<VersionAnalysisResult> versions = analyzeVersions(
                        Collections.singletonList(DEFAULT_SUFFIX),
                        gav.getVersion(),
                        artifacts);

                CompletableFuture<Set<ProductArtifact>> built = artifacts
                        .thenCombine(versions, (a, v) -> mapProducts(getBuilt(a, v), gav, versionParser));
                intr.put(gav, built);

                CompletableFuture<Set<ProductArtifact>> different = artifacts
                        .thenCombine(versions, (a, v) -> mapProducts(getBuiltDifferent(a, v), gav, versionParser));
                diff.put(gav, different);
            }

            CompletableFuture<Void> intrDone = copyCompletedMap(intr, internallyBuilt);
            CompletableFuture<Void> diffDone = copyCompletedMap(diff, differentVersion);

            futures.add(
                    CompletableFuture.allOf(intrDone, diffDone)
                            .thenAccept(x -> fillNotBuilt(blacklisted, internallyBuilt, differentVersion, notBuilt)));
        }
        FuturesUtil.joinFutures(futures);
        return ret;
    }

    private static Set<ProductArtifacts> getBuiltDifferent(Set<ProductArtifacts> a, VersionAnalysisResult v) {
        Optional<String> bmv = v.getBestMatchVersion();
        if (bmv.isPresent()) {
            return Collections.emptySet();
        } else {
            return a;
        }
    }

    private static Set<ProductArtifacts> getBuilt(Set<ProductArtifacts> a, VersionAnalysisResult v) {
        return v.getBestMatchVersion()
                .map(b -> AggregatedProductProvider.filterArtifacts(a, x -> b.equals(x.getVersion())))
                .orElse(Collections.emptySet());
    }

    private CompletableFuture<Set<ProductArtifacts>> filterProducts(
            boolean useUnknownProduct,
            Set<Product> products,
            CompletableFuture<Set<ProductArtifacts>> artifacts) {

        Predicate<Product> pred = (x) -> true;
        if (products.isEmpty()) {
            pred = pred.and(p -> p.getStatus() == SUPPORTED || p.getStatus() == SUPERSEDED);
            if (useUnknownProduct) {
                pred = pred.or(p -> UNKNOWN.equals(p));
            }
        }

        artifacts = filterProductArtifacts(products, artifacts, pred);

        return artifacts;
    }

    private static Set<ProductArtifact> mapProducts(
            Set<ProductArtifacts> products,
            GAV gav,
            VersionParser versionParser) {
        VersionComparator comparator = new VersionComparator(versionParser);
        return products.stream()
                .flatMap(
                        e -> e.getArtifacts()
                                .stream()
                                .map(
                                        x -> toProductArtifact(
                                                e.getProduct(),
                                                (MavenArtifact) x,
                                                gav.getVersion(),
                                                comparator)))
                .collect(
                        Collectors
                                .toCollection(() -> new TreeSet<>(Comparator.comparing(ProductArtifact::getArtifact))));
    }

    /**
     * Fills notBuilt set with GAVs that are neither blacklisted, internally built nor built in different version.
     */
    private void fillNotBuilt(
            Set<GAV> blacklisted,
            Map<GAV, Set<ProductArtifact>> internallyBuilt,
            Map<GAV, Set<ProductArtifact>> differentVersion,
            Set<GAV> notBuilt) {
        for (Map.Entry<GAV, Set<ProductArtifact>> e : internallyBuilt.entrySet()) {
            GAV gav = e.getKey();
            Set<ProductArtifact> internally = e.getValue();
            Set<ProductArtifact> different = differentVersion.get(gav);

            if (!blacklisted.contains(gav) && internally.isEmpty() && different.isEmpty()) {
                notBuilt.add(gav);
            }
        }
    }

    /**
     * Returns future, that completes when all input futures are completed and copied to the result map.
     */
    private CompletableFuture<Void> copyCompletedMap(
            Map<GAV, CompletableFuture<Set<ProductArtifact>>> futures,
            Map<GAV, Set<ProductArtifact>> result) {
        CompletableFuture<Void> diffDone = CompletableFuture
                .allOf(futures.values().toArray(new CompletableFuture[futures.size()]))
                .thenAccept(x -> {
                    for (Map.Entry<GAV, CompletableFuture<Set<ProductArtifact>>> e : futures.entrySet()) {
                        result.put(e.getKey(), e.getValue().join());
                    }
                });
        return diffDone;
    }

    @Override
    public Set<BuiltReportModule> getBuiltReport(SCMLocator scml)
            throws ScmException, PomAnalysisException, CommunicationException {
        Map<GA, Set<GAV>> dependenciesOfModules = scmConnector.getDependenciesOfModules(
                scml.getScmUrl(),
                scml.getRevision(),
                scml.getPomPath(),
                scml.getRepositories());
        Set<CompletableFuture<BuiltReportModule>> builtSet = new HashSet<>();
        for (Map.Entry<GA, Set<GAV>> e : dependenciesOfModules.entrySet()) {
            for (GAV gav : e.getValue()) {
                CompletableFuture<Set<ProductArtifacts>> artifacts = aggProductProvider
                        .getArtifacts(new MavenArtifact(gav));
                artifacts = filterBuiltArtifacts(artifacts);
                builtSet.add(
                        analyzeVersions(Collections.singletonList(DEFAULT_SUFFIX), gav.getVersion(), artifacts)
                                .thenApply(v -> toBuiltReportModule(gav, v)));
            }
        }
        return FuturesUtil.joinFutures(builtSet);
    }

    private BuiltReportModule toBuiltReportModule(GAV gav, VersionAnalysisResult vlr) {
        BuiltReportModule report = new BuiltReportModule(gav);
        report.setAvailableVersions(vlr.getAvailableVersions());
        vlr.getBestMatchVersion().ifPresent(bmv -> report.setBuiltVersion(bmv));
        return report;
    }

    @Override
    public List<NPMLookupReport> getLookupReports(LookupNPMRequest request) throws CommunicationException {
        final String versionSuffix = request.getVersionSuffix();
        String mode = request.getMode();

        LookupMode lookupMode = getLookupMode(mode, versionSuffix);
        pncProductProvider.setLookupMode(lookupMode);

        Set<String> uniqueNames = request.getPackages().stream().map(x -> x.getName()).collect(Collectors.toSet());

        Map<String, CompletableFuture<Set<ProductArtifacts>>> artifactsMap = getProductArtifactsNPM(uniqueNames);

        return createLookupReports(request.getPackages(), versionSuffix, artifactsMap);
    }

    private Predicate<SuffixedVersion> majorMinorFilter(NPMPackage pkg) {
        SuffixedVersion version = VersionParser.parseUnsuffixed(pkg.getVersion());
        return (SuffixedVersion s) -> s.getMajor() == version.getMajor() && s.getMinor() == version.getMinor();
    }

    @Override
    public List<NPMVersionsReport> getVersionsReports(VersionsNPMRequest request) throws CommunicationException {
        final VersionsNPMRequest.VersionFilter versionFilter = request.getVersionFilter();

        Function<NPMPackage, Predicate<SuffixedVersion>> predicateProvider;
        switch (versionFilter) {
            case MAJOR_MINOR:
                predicateProvider = this::majorMinorFilter;
                break;
            default:
                throw new UnsupportedOperationException("Unknown filter " + versionFilter);
        }

        ProductProvider productProvider = setupProductProvider(false, null, request.getMode(), request.isIncludeAll());

        Set<String> uniqueNames = request.getPackages().stream().map(x -> x.getName()).collect(Collectors.toSet());

        Map<String, CompletableFuture<Set<String>>> artifactsMap = new HashMap<>();
        for (String name : uniqueNames) {
            CompletableFuture<Set<String>> artifacts = productProvider.getAllVersions(new NPMArtifact(name, "0.0.0"))
                    .thenApply(s -> s.stream().map(QualifiedVersion::getVersion).collect(Collectors.toSet()));

            artifactsMap.put(name, artifacts);
        }

        return createVersionsReports(request.getPackages(), artifactsMap, predicateProvider);
    }

    private Map<String, CompletableFuture<Set<ProductArtifacts>>> getProductArtifactsNPM(Set<String> packageNames) {
        Map<String, CompletableFuture<Set<ProductArtifacts>>> gaProductArtifactsMap = new HashMap<>();
        for (String name : packageNames) {
            CompletableFuture<Set<ProductArtifacts>> artifacts = pncProductProvider
                    .getArtifacts(new NPMArtifact(name, "0.0.0"));

            gaProductArtifactsMap.put(name, artifacts);
        }

        return gaProductArtifactsMap;
    }

    private List<NPMLookupReport> createLookupReports(
            List<NPMPackage> packages,
            String suffix,
            Map<String, CompletableFuture<Set<ProductArtifacts>>> artifactsMap) throws CommunicationException {
        List<CompletableFuture<NPMLookupReport>> futures = packages.stream().distinct().map((a) -> {

            CompletableFuture<Set<ProductArtifacts>> artifacts = artifactsMap.get(a.getName());
            CompletableFuture<VersionAnalysisResult> analyzedVersions = analyzeVersions(
                    getListOfSuffixes(suffix),
                    a.getVersion(),
                    artifacts);

            return analyzedVersions.thenApply(
                    (v) -> new NPMLookupReport(a, v.getBestMatchVersion().orElse(null), v.getAvailableVersions()));
        }).collect(Collectors.toList());

        return FuturesUtil.joinFutures(futures);
    }

    private List<NPMVersionsReport> createVersionsReports(
            List<NPMPackage> packages,
            Map<String, CompletableFuture<Set<String>>> artifactsMap,
            Function<NPMPackage, Predicate<SuffixedVersion>> predicateProvider) throws CommunicationException {
        List<CompletableFuture<NPMVersionsReport>> futures = packages.stream().distinct().map((a) -> {
            Predicate<SuffixedVersion> predicate = predicateProvider.apply(a);

            CompletableFuture<List<String>> versions = artifactsMap.get(a.getName())
                    .thenApply(
                            s -> s.stream()
                                    .map(VersionParser::parseUnsuffixed)
                                    .filter(predicate)
                                    .map(SuffixedVersion::getOriginalVersion)
                                    .sorted()
                                    .collect(Collectors.toList()));

            return versions.thenApply(v -> NPMVersionsReport.builder().npmPackage(a).availableVersions(v).build());
        }).collect(Collectors.toList());

        return FuturesUtil.joinFutures(futures);
    }

    @Override
    public List<LookupReport> getLookupReportsForGavs(LookupGAVsRequest request) throws CommunicationException {
        userLog.info("Starting lookup report for: " + request);

        /** Get set of GAs */
        Set<GA> uniqueGAs = request.getGavs().stream().map(GAV::getGA).collect(Collectors.toSet());

        Map<GA, CompletableFuture<Set<ProductArtifacts>>> gaProductArtifactsMap;
        ProductProvider productProvider = setupProductProvider(
                request.getBrewPullActive(),
                request.getVersionSuffix(),
                request.getMode());
        gaProductArtifactsMap = getProductArtifactsPerGA(productProvider, request, uniqueGAs);

        return createLookupReports(request, gaProductArtifactsMap);
    }

    private ProductProvider setupProductProvider(Boolean brewPullActive, String versionSuffix, String mode) {
        return setupProductProvider(brewPullActive, versionSuffix, mode, false);
    }

    private ProductProvider setupProductProvider(
            Boolean brewPullActive,
            final String versionSuffix,
            final String mode,
            final boolean includeAll) {
        LookupMode lookupMode = getLookupMode(mode, versionSuffix);
        if (includeAll) {
            lookupMode = lookupMode.toBuilder().artifactQualities(EnumSet.allOf(ArtifactQuality.class)).build();
        }
        pncProductProvider.setLookupMode(lookupMode);
        if (BooleanUtils.isNotFalse(brewPullActive)) {
            repositoryProductProvider.setLookupMode(lookupMode);
            return aggProductProvider;
        } else {
            return pncProductProvider;
        }
    }

    private Map<GA, CompletableFuture<Set<ProductArtifacts>>> getProductArtifactsPerGA(
            ProductProvider productProvider,
            LookupGAVsRequest request,
            Set<GA> uniqueGAs) throws CommunicationException {
        Set<Product> products = productAdapter.toProducts(request.getProductNames(), request.getProductVersionIds());

        Map<GA, CompletableFuture<Set<ProductArtifacts>>> gaProductArtifactsMap = new HashMap<>();
        for (GA ga : uniqueGAs) {
            CompletableFuture<Set<ProductArtifacts>> artifacts = productProvider
                    .getArtifacts(new MavenArtifact(new GAV(ga, "0.0.0")));
            artifacts = filterProductArtifacts(products, artifacts);

            gaProductArtifactsMap.put(ga, artifacts);
        }

        return gaProductArtifactsMap;
    }

    private List<LookupReport> createLookupReports(
            LookupGAVsRequest request,
            Map<GA, CompletableFuture<Set<ProductArtifacts>>> gaProductArtifactsMap) throws CommunicationException {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        List<LookupReport> reports = new ArrayList<>();

        List<String> suffixes = getListOfSuffixes(request.getVersionSuffix());

        request.getGavs().stream().distinct().forEach((gav) -> {
            LookupReport lr = new LookupReport(gav);
            reports.add(lr);

            CompletableFuture<Set<ProductArtifacts>> artifacts = gaProductArtifactsMap.get(gav.getGA());

            futures.add(analyzeVersions(suffixes, gav.getVersion(), artifacts).thenAccept(v -> {
                lr.setAvailableVersions(v.getAvailableVersions());
                lr.setBestMatchVersion(v.getBestMatchVersion().orElse(null));
            }));

            futures.add(artifacts.thenAccept(pas -> {
                lr.setWhitelisted(toWhitelisted(pas));
            }));
            lr.setBlacklisted(blackArtifactService.isArtifactPresent(gav));
        });

        FuturesUtil.joinFutures(futures);

        return reports;
    }

    private List<String> getListOfSuffixes(String suffix) {
        List<String> suffixes = new ArrayList<>(2);
        if (suffix != null && !suffix.isEmpty()) {
            suffixes.add(suffix);
        }
        suffixes.add(DEFAULT_SUFFIX);
        return suffixes;
    }

    private static ProductArtifact toProductArtifact(
            Product p,
            MavenArtifact a,
            String origVersion,
            VersionComparator comparator) {
        ProductArtifact ret = new ProductArtifact();
        ret.setArtifact(a.getGav());
        ret.setProductName(p.getName());
        ret.setProductVersion(p.getVersion());
        ret.setSupportStatus(p.getStatus());
        ret.setDifferenceType(comparator.difference(origVersion, a.getGav().getVersion()).toString());
        return ret;
    }

    private CompletableFuture<Set<ProductArtifacts>> filterProductArtifacts(
            Set<Product> products,
            CompletableFuture<Set<ProductArtifacts>> artifacts) {
        return filterProductArtifacts(products, artifacts, x -> true);
    }

    private CompletableFuture<Set<ProductArtifacts>> filterProductArtifacts(
            Set<Product> products,
            CompletableFuture<Set<ProductArtifacts>> artifacts,
            Predicate<Product> pred) {
        if (!products.isEmpty()) {
            pred = pred.and(p -> products.contains(p));
        }
        artifacts = AggregatedProductProvider.filterProducts(artifacts, pred);
        artifacts = filterBuiltArtifacts(artifacts);
        return artifacts;
    }

    private CompletableFuture<Set<ProductArtifacts>> filterBuiltArtifacts(
            CompletableFuture<Set<ProductArtifacts>> artifacts) {
        return artifacts.thenApply(
                as -> AggregatedProductProvider.filterArtifacts(
                        as,
                        a -> !blackArtifactService.isArtifactPresent(((MavenArtifact) a).getGav())));
    }

    private static List<RestProductInput> toWhitelisted(Set<ProductArtifacts> whitelisted) {
        return whitelisted.stream()
                .map(pa -> pa.getProduct())
                .filter(p -> !UNKNOWN.equals(p))
                .map(p -> new RestProductInput(p.getName(), p.getVersion(), p.getStatus()))
                .collect(Collectors.toList());
    }

    private LookupMode getLookupMode(String modeName, String suffix) {
        LookupMode mode;
        if (StringUtils.isEmpty(modeName)) {
            mode = new LookupMode();
            mode.setName("ON_THE_FLY_MODE");
            mode.getBuildCategories().add(BuildCategory.STANDARD);
            if (suffix != null && !suffix.isEmpty()) {
                mode.getSuffixes().add(suffix);
            }
            mode.getSuffixes().add(DEFAULT_SUFFIX);
            mode.getArtifactQualities().add(ArtifactQuality.NEW);
            mode.getArtifactQualities().add(ArtifactQuality.VERIFIED);
            mode.getArtifactQualities().add(ArtifactQuality.TESTED);
            if (suffix != null && suffix.contains("temporary")) {
                mode.getArtifactQualities().add(ArtifactQuality.TEMPORARY);
            }
        } else {
            if (!modes.containsKey(modeName)) {
                throw new ValidationException(
                        "Invalid mode name: " + modeName + ". Available modes are: " + modes.keySet());
            }
            mode = modes.get(modeName);
        }
        return mode;
    }

    @Data
    private static class VersionAnalysisResult {
        private final Optional<String> bestMatchVersion;
        private final List<String> availableVersions;

        public VersionAnalysisResult(Optional<String> bestMatchVersion, List<String> versions) {
            this.bestMatchVersion = bestMatchVersion;
            this.availableVersions = versions;
        }
    }
}
