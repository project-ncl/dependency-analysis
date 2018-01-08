package org.jboss.da.reports.impl;

import static org.jboss.da.listings.model.ProductSupportStatus.SUPERSEDED;
import static org.jboss.da.listings.model.ProductSupportStatus.SUPPORTED;

import org.apache.maven.scm.ScmException;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.common.version.VersionParser;
import org.jboss.da.communication.aprox.FindGAVDependencyException;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.communication.pom.api.PomAnalyzer;
import org.jboss.da.communication.scm.api.SCMConnector;
import org.jboss.da.listings.api.dao.ProductDAO;
import org.jboss.da.listings.api.dao.ProductVersionDAO;
import org.jboss.da.listings.api.model.ProductVersion;
import org.jboss.da.listings.api.service.BlackArtifactService;
import org.jboss.da.listings.api.service.ProductVersionService;
import org.jboss.da.listings.model.rest.RestProductInput;
import org.jboss.da.model.rest.GA;
import org.jboss.da.model.rest.GAV;
import org.jboss.da.model.rest.VersionComparator;
import org.jboss.da.products.api.Artifact;
import org.jboss.da.products.api.Product;
import static org.jboss.da.products.api.Product.UNKNOWN;
import org.jboss.da.products.api.ProductArtifacts;
import org.jboss.da.products.impl.AggregatedProductProvider;
import org.jboss.da.reports.api.AdvancedArtifactReport;
import org.jboss.da.reports.api.AlignmentReportModule;
import org.jboss.da.reports.api.ArtifactReport;
import org.jboss.da.reports.api.BuiltReportModule;
import org.jboss.da.reports.api.ProductArtifact;
import org.jboss.da.reports.api.ReportsGenerator;
import org.jboss.da.reports.api.VersionLookupResult;
import org.jboss.da.reports.backend.api.DependencyTreeGenerator;
import org.jboss.da.reports.backend.api.VersionFinder;
import org.jboss.da.reports.model.api.SCMLocator;
import org.jboss.da.reports.model.rest.GAVRequest;
import org.jboss.da.reports.model.rest.LookupGAVsRequest;
import org.jboss.da.reports.model.rest.LookupReport;
import org.jboss.da.reports.model.rest.SCMReportRequest;
import org.jboss.da.scm.api.SCM;
import org.jboss.da.scm.api.SCMType;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The implementation of reports, which provides information about
 * built/not built artifacts/blacklisted artifacts
 *
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@ApplicationScoped
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
    private ProductVersionDAO productVersionDao;

    @Inject
    private ProductDAO productDao;

    @Inject
    private ProductVersionService productVersionService;

    @Inject
    private SCMConnector scmConnector;

    @Inject
    private AggregatedProductProvider productProvider;

    @Override
    public Optional<ArtifactReport> getReportFromSCM(SCMReportRequest scml) throws ScmException,
            PomAnalysisException, CommunicationException {
        if (scml == null)
            throw new IllegalArgumentException("SCM information can't be null");

        Set<Product> products = getProducts(scml.getProductNames(), scml.getProductVersionIds());
        GAVDependencyTree dt = dependencyTreeGenerator.getDependencyTree(scml.getScml());

        return createReport(dt, products);
    }

    @Override
    public ArtifactReport getReport(GAVRequest gavRequest) throws CommunicationException,
            FindGAVDependencyException {
        if (gavRequest == null)
            throw new IllegalArgumentException("GAV can't be null");

        Set<Product> products = getProducts(gavRequest.getProductNames(),
                gavRequest.getProductVersionIds());
        GAVDependencyTree dt = dependencyTreeGenerator.getDependencyTree(gavRequest.asGavObject());

        return createReport(dt, products).get();
    }

    private Optional<ArtifactReport> createReport(GAVDependencyTree dt, Set<Product> products)
            throws CommunicationException {
        ArtifactReport report = new ArtifactReport(dt.getGav());

        Set<GAVDependencyTree> nodesVisited = new HashSet<>();
        nodesVisited.add(dt);
        addDependencyReports(report, dt.getDependencies(), nodesVisited);

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        traverseAndFill(report, products, futures);

        joinFutures(futures);

        return Optional.of(report);
    }

    private void addDependencyReports(ArtifactReport ar, Set<GAVDependencyTree> dependencyTree,
            Set<GAVDependencyTree> nodesVisited) throws CommunicationException {
        for (GAVDependencyTree dt : dependencyTree) {

            ArtifactReport dar = new ArtifactReport(dt.getGav());

            // if dt hasn't been visited yet, add dependencies of dt in the report
            if (!nodesVisited.contains(dt))
                addDependencyReports(dar, dt.getDependencies(), nodesVisited);

            ar.addDependency(dar);
            nodesVisited.add(dt);
        }
    }

    private void traverseAndFill(ArtifactReport report, Set<Product> products,
            List<CompletableFuture<Void>> futures) {
        futures.add(fillArtifactReport(report, products));
        for (ArtifactReport dep : report.getDependencies()) {
            traverseAndFill(dep, products, futures);
        }
    }

    private CompletableFuture<Void> fillArtifactReport(ArtifactReport report, Set<Product> products) {
        GAV gav = report.getGav();

        CompletableFuture<Set<ProductArtifacts>> artifacts = productProvider.getArtifacts(gav
                .getGA());
        artifacts = filterProductArtifacts(products, artifacts);
        
        report.setBlacklisted(blackArtifactService.isArtifactPresent(gav));

        CompletableFuture<Void> fillVersions = versionFinder.getVersionsFor(gav, artifacts)
                .thenAccept(v -> {
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

    @Override
    public Optional<AdvancedArtifactReport> getAdvancedReportFromSCM(SCMReportRequest request)
            throws ScmException, PomAnalysisException, CommunicationException {

        SCMLocator scml = request.getScml();
        Set<Product> products = getProducts(request.getProductNames(), request.getProductVersionIds());

        GAVDependencyTree dt = dependencyTreeGenerator.getDependencyTree(scml);
        Optional<ArtifactReport> artifactReport = createReport(dt, products);
        // TODO: hardcoded to git
        // hopefully we'll get the cached cloned folder for this repo
        File repoFolder = scmManager.cloneRepository(SCMType.GIT, scml.getScmUrl(),
                scml.getRevision());
        return artifactReport.map(r -> generateAdvancedArtifactReport(r, repoFolder));
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
        VersionParser parser = new VersionParser();

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
                if (parser.isSuffixedVersion(dep.getVersion()))
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
                    if (!dep.getAvailableVersions().isEmpty()) {
                        Set<String> versions = new TreeSet<>(
                                new VersionComparator(gav.getVersion()));
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
    public Set<AlignmentReportModule> getAligmentReport(SCMLocator scml,
            boolean useUnknownProduct, Set<Long> productIds) throws ScmException,
            PomAnalysisException, CommunicationException {
        Map<GA, Set<GAV>> dependenciesOfModules = scmConnector.getDependenciesOfModules(
                scml.getScmUrl(), scml.getRevision(), scml.getPomPath(), scml.getRepositories());
        Set<Product> products = getProducts(Collections.emptySet(), productIds);

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
                        useUnknownProduct, products, productProvider.getArtifacts(gav.getGA()));
                CompletableFuture<VersionLookupResult> versions = versionFinder.getVersionsFor(gav,
                        artifacts);

                CompletableFuture<Set<ProductArtifact>> built = artifacts
                        .thenCombine(versions, (a, v) -> mapProducts(getBuilt(a,v), gav));
                intr.put(gav, built);

                CompletableFuture<Set<ProductArtifact>> different = artifacts
                        .thenCombine(versions, (a, v) -> mapProducts(getBuiltDifferent(a,v), gav));
                diff.put(gav, different);
            }

            CompletableFuture<Void> intrDone = copyCompletedMap(intr, internallyBuilt);
            CompletableFuture<Void> diffDone = copyCompletedMap(diff, differentVersion);

            futures.add(CompletableFuture.allOf(intrDone, diffDone)
                    .thenAccept(x -> fillNotBuilt(blacklisted, internallyBuilt,
                            differentVersion, notBuilt)));
        }
        joinFutures(futures);
        return ret;
    }

    private static Set<ProductArtifacts> getBuiltDifferent(Set<ProductArtifacts> a,
            VersionLookupResult v) {
        Optional<String> bmv = v.getBestMatchVersion();
        if (bmv.isPresent()) {
            return Collections.emptySet();
        } else {
            return a;
        }
    }

    private static Set<ProductArtifacts> getBuilt(Set<ProductArtifacts> a, VersionLookupResult v) {
        return v.getBestMatchVersion().map(b -> AggregatedProductProvider
                .filterArtifacts(a, x -> b.equals(x.getGav().getVersion())))
                .orElse(Collections.emptySet());
    }

    private CompletableFuture<Set<ProductArtifacts>> filterProducts(boolean useUnknownProduct,
            Set<Product> products, CompletableFuture<Set<ProductArtifacts>> artifacts) {

        Predicate<Product> pred = (x) -> true;
        if (products.isEmpty()) {
            pred = pred.and(p -> p.getStatus() == SUPPORTED || p.getStatus() == SUPERSEDED);
            if(useUnknownProduct){
                pred = pred.or(p -> UNKNOWN.equals(p));
            }
        }

        artifacts = filterProductArtifacts(products, artifacts, pred);

        return artifacts;
    }

    private static Set<ProductArtifact> mapProducts(Set<ProductArtifacts> products, GAV gav) {
        return products.stream()
                .flatMap(e -> e.getArtifacts().stream()
                        .map(x -> toProductArtifact(e.getProduct(), x, gav.getVersion())))
                .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(ProductArtifact::getArtifact))));
    }

    /**
     * Fills notBuilt set with GAVs that are neither blacklisted, internally built nor built in
     * different version.
     */
    private void fillNotBuilt(Set<GAV> blacklisted, Map<GAV, Set<ProductArtifact>> internallyBuilt,
            Map<GAV, Set<ProductArtifact>> differentVersion, Set<GAV> notBuilt) {
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
     * Returns future, that completes when all input futures are completed and copied to the result
     * map.
     */
    private CompletableFuture<Void> copyCompletedMap(
            Map<GAV, CompletableFuture<Set<ProductArtifact>>> futures,
            Map<GAV, Set<ProductArtifact>> result) {
        CompletableFuture<Void> diffDone = CompletableFuture.allOf(
                futures.values().toArray(new CompletableFuture[futures.size()]))
                .thenAccept(x -> {
                    for (Map.Entry<GAV, CompletableFuture<Set<ProductArtifact>>> e : futures.entrySet()) {
                        result.put(e.getKey(), e.getValue().join());
                    }
                });
        return diffDone;
    }

    @Override
    public Set<BuiltReportModule> getBuiltReport(SCMLocator scml) throws ScmException,
            PomAnalysisException, CommunicationException {
        Map<GA, Set<GAV>> dependenciesOfModules = scmConnector.getDependenciesOfModules(
                scml.getScmUrl(), scml.getRevision(), scml.getPomPath(), scml.getRepositories());
        Set<CompletableFuture<BuiltReportModule>> builtSet = new HashSet<>();
        for (Map.Entry<GA, Set<GAV>> e : dependenciesOfModules.entrySet()) {
            for (GAV gav : e.getValue()) {
                CompletableFuture<Set<ProductArtifacts>> artifacts = productProvider.getArtifacts(gav.getGA());
                builtSet.add(versionFinder.getVersionsFor(gav, artifacts)
                        .thenApply(v -> toBuiltReportModule(gav, v)));
            }
        }
        return joinFutures(builtSet);
    }

    private BuiltReportModule toBuiltReportModule(GAV gav, VersionLookupResult vlr) {
        BuiltReportModule report = new BuiltReportModule(gav);
        report.setAvailableVersions(vlr.getAvailableVersions());
        vlr.getBestMatchVersion().ifPresent(bmv -> report.setBuiltVersion(bmv));
        return report;
    }

    @Override
    public List<LookupReport> getLookupReportsForGavs(LookupGAVsRequest request)
            throws CommunicationException{

        /** Get set of GAs */
        Set<GA> uniqueGAs = request.getGavs().stream().map(GAV::getGA).collect(Collectors.toSet());

        Map<GA, CompletableFuture<Set<ProductArtifacts>>> gaProductArtifactsMap = getProductArtifactsPerGA(request, uniqueGAs);

        return createLookupReports(request, gaProductArtifactsMap);
        
    }

    private Map<GA, CompletableFuture<Set<ProductArtifacts>>> getProductArtifactsPerGA(
            LookupGAVsRequest request, Set<GA> uniqueGAs) throws CommunicationException {
        Set<Product> products = getProducts(request.getProductNames(),
                request.getProductVersionIds());
        final String repositoryGroup = request.getRepositoryGroup();

        Map<GA, CompletableFuture<Set<ProductArtifacts>>> gaProductArtifactsMap = new HashMap<>();
        for (GA ga : uniqueGAs) {
            CompletableFuture<Set<ProductArtifacts>> artifacts;
            if (repositoryGroup == null) {
                artifacts = productProvider.getArtifacts(ga);
            } else {
                artifacts = productProvider.getArtifactsFromRepository(ga, repositoryGroup);
            }
            artifacts = filterProductArtifacts(products, artifacts);

            gaProductArtifactsMap.put(ga, artifacts);
        }

        return gaProductArtifactsMap;
    }

    private List<LookupReport> createLookupReports(LookupGAVsRequest request, Map<GA, CompletableFuture<Set<ProductArtifacts>>> gaProductArtifactsMap) throws CommunicationException {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        List<LookupReport> reports = new ArrayList<>();
        request.getGavs().stream()
                .distinct()
                .forEach((gav) -> {
            LookupReport lr = new LookupReport(gav);
            reports.add(lr);

            CompletableFuture<Set<ProductArtifacts>> artifacts = gaProductArtifactsMap.get(gav.getGA());

            futures.add(versionFinder.getVersionsFor(gav, artifacts).thenAccept(v -> {
                lr.setAvailableVersions(v.getAvailableVersions());
                lr.setBestMatchVersion(v.getBestMatchVersion().orElse(null));
            }));

            futures.add(artifacts.thenAccept(pas -> {
                lr.setWhitelisted(toWhitelisted(pas));
            }));
        });

        joinFutures(futures);

        return reports;
    }

    private void joinFutures(List<CompletableFuture<Void>> futures) throws CommunicationException {
        try {
            futures.stream().forEach(r -> r.join());
        } catch(CompletionException ex){
            if(ex.getCause() instanceof CommunicationException){
                throw (CommunicationException) ex.getCause();
            }
            throw ex;
        }
    }

    private <T> Set<T> joinFutures(Set<CompletableFuture<T>> futures) throws CommunicationException {
        try {
            return futures.stream().map(r -> r.join()).collect(Collectors.toSet());
        } catch(CompletionException ex){
            if(ex.getCause() instanceof CommunicationException){
                throw (CommunicationException) ex.getCause();
            }
            throw ex;
        }
    }

    private Set<Product> getProducts(Set<String> productNames, Set<Long> productVersionIds) {
        Set<ProductVersion> productVersions = new HashSet<>();
        StringBuilder errorMsg = new StringBuilder();

        if(productNames != null && !productNames.isEmpty()){
            List<org.jboss.da.listings.api.model.Product> productsByName = productDao.findAllWithNames(new ArrayList<>(productNames));
            if(productNames.size() == productsByName.size()){
                for(String productName : productNames){
                    List<ProductVersion> prodVers = productVersionService.getAllForProduct(productName);
                    productVersions.addAll(prodVers);
                }
            } else {
                // Error
                Set<String> unexistingProductNames = new HashSet<>(productNames);
                productsByName.stream().forEach(x -> unexistingProductNames.remove(x.getName()));
                errorMsg.append("Product names do not exist: ");
                errorMsg.append(joinMissing(unexistingProductNames));
            }
        }

        if(productVersionIds != null && !productVersionIds.isEmpty()){
            List<ProductVersion> prodVersionsById = productVersionDao.findAllWithIds(new ArrayList<>(productVersionIds));
            if(productVersionIds.size() == prodVersionsById.size()){
                productVersions.addAll(prodVersionsById);
            } else {
                // Error
                Set<Long> unexistingProductVersionIds = new HashSet<>(productVersionIds);
                prodVersionsById.stream().forEach(x -> unexistingProductVersionIds.remove(x.getId()));
                errorMsg.append("Product Versions do not exist: ");
                errorMsg.append(joinMissing(unexistingProductVersionIds));
            }
        }

        if(errorMsg.length() > 0){
            throw new IllegalArgumentException(errorMsg.toString());
        }

        return productVersions.stream()
                .map(ReportsGeneratorImpl::toProduct)
                .collect(Collectors.toSet());
    }

    private <T> String joinMissing(Collection<T> invalidItems) {
        return invalidItems.stream()
                .map(Object::toString)
                .collect(Collectors.joining(",", "[", "];"));
    }

    private static Product toProduct(ProductVersion pv) {
        return new Product(pv.getProduct().getName(), pv.getProductVersion());
    }

    private static ProductArtifact toProductArtifact(Product p, Artifact a, String origVersion) {
        ProductArtifact ret = new ProductArtifact();
        ret.setArtifact(a.getGav());
        ret.setProductName(p.getName());
        ret.setProductVersion(p.getVersion());
        ret.setSupportStatus(p.getStatus());
        ret.setDifferenceType(VersionComparator.difference(origVersion, a.getGav().getVersion())
                .toString());
        return ret;
    }

    private CompletableFuture<Set<ProductArtifacts>> filterProductArtifacts(Set<Product> products,
            CompletableFuture<Set<ProductArtifacts>> artifacts) {
        return filterProductArtifacts(products, artifacts, x -> true);
    }

    private CompletableFuture<Set<ProductArtifacts>> filterProductArtifacts(Set<Product> products,
            CompletableFuture<Set<ProductArtifacts>> artifacts, Predicate<Product> pred) {
        if(!products.isEmpty()){
            pred = pred.and(p -> products.contains(p));
        }
        artifacts = AggregatedProductProvider.filterProducts(artifacts, pred);
        artifacts = filterBuiltArtifacts(artifacts);
        return artifacts;
    }

    private CompletableFuture<Set<ProductArtifacts>> filterBuiltArtifacts(CompletableFuture<Set<ProductArtifacts>> artifacts) {
        return artifacts.thenApply(as -> AggregatedProductProvider.filterArtifacts(as,
                a -> !blackArtifactService.isArtifactPresent(a.getGav())));
    }

    private static List<RestProductInput> toWhitelisted(Set<ProductArtifacts> whitelisted) {
        return whitelisted
                .stream()
                .map(pa -> pa.getProduct())
                .filter(p -> !UNKNOWN.equals(p))
                .map(p -> new RestProductInput(p.getName(), p.getVersion(), p.getStatus()))
                .collect(Collectors.toList());
    }
}
