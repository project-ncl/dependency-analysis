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
import org.jboss.da.listings.api.model.ProductVersionArtifactRelationship;
import org.jboss.da.listings.api.service.BlackArtifactService;
import org.jboss.da.listings.api.service.ProductVersionService;
import org.jboss.da.listings.model.rest.RestProductInput;
import org.jboss.da.model.rest.GA;
import org.jboss.da.model.rest.GAV;
import org.jboss.da.model.rest.VersionComparator;
import org.jboss.da.products.backend.api.Artifact;
import org.jboss.da.products.backend.api.Product;
import static org.jboss.da.products.backend.api.Product.UNKNOWN;
import org.jboss.da.products.backend.api.ProductArtifacts;
import org.jboss.da.products.backend.impl.AggregatedProductProvider;
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

import javax.inject.Inject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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

        Set<ProductVersion> relevantProductVersions = getProductVersions(scml.getProductNames(),
                scml.getProductVersionIds());

        return createScmReport(scml.getScml(), relevantProductVersions);
    }

    private Optional<ArtifactReport> createGavReport(GAVRequest gav,
            Set<ProductVersion> productVersions) throws CommunicationException,
            FindGAVDependencyException {

        GAVDependencyTree dt = dependencyTreeGenerator.getDependencyTree(gav.asGavObject());

        return createReport(dt, productVersions);
    }

    private Optional<ArtifactReport> createScmReport(SCMLocator scml,
            Set<ProductVersion> productVersions) throws ScmException, PomAnalysisException,
            CommunicationException {

        GAVDependencyTree dt = dependencyTreeGenerator.getDependencyTree(scml);

        return createReport(dt, productVersions);
    }

    private Optional<ArtifactReport> createReport(GAVDependencyTree dt,
            Set<ProductVersion> productVersions) throws CommunicationException {
        Set<Product> products = productVersions.stream().map(ReportsGeneratorImpl::toProduct).collect(Collectors.toSet());

        ArtifactReport report = new ArtifactReport(dt.getGav());

        Set<GAVDependencyTree> nodesVisited = new HashSet<>();
        nodesVisited.add(dt);
        addDependencyReports(report, dt.getDependencies(), nodesVisited);
        
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        traverseAndFill(report, products, futures);
        try{
            futures.stream().forEach(CompletableFuture::join);
        }catch(CompletionException ex){
            throw new CommunicationException(ex);
        }

        return Optional.of(report);
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
        return artifacts.thenAccept(pas -> {
            final List<String> versions = getVersions(pas, gav);
            List<Product> ps = pas.stream().map(pa -> pa.getProduct()).collect(Collectors.toList());
            report.setAvailableVersions(versions);
            report.setBestMatchVersion(versionFinder.getBestMatchVersionFor(gav, versions));
            report.setWhitelisted(ps);
        });
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

    private Set<Long> getProductVersionIds(Set<ProductVersion> relevantProductVersions) {
        return relevantProductVersions.stream()
                .map(x -> x.getId())
                .collect(Collectors.toSet());
    }

    private Set<ProductVersion> getProductVersions(Set<String> productNames, Set<Long> productVersionIds) {
        Set<ProductVersion> productVersions = new HashSet<>();
        StringBuilder errorMsg = new StringBuilder();
        
        if(productNames != null && !productNames.isEmpty()){
            List<org.jboss.da.listings.api.model.Product> productsByName = productDao.findAllWithNames(new ArrayList<>(productNames));
            if(productsByName!= null && (productNames.size() == productsByName.size())){
                for(String productName : productNames){
                    List<ProductVersion> prodVers = productVersionService.getAllForProduct(productName); 
                    productVersions.addAll(prodVers);
                }
            } else {
                // Error
                Set<String> inexistingProductNames = new HashSet<>(productNames);
                productsByName.stream().forEach(x -> inexistingProductNames.remove(x.getName()));
                errorMsg.append("Product names do not exist: ");
                appendErrors(errorMsg, inexistingProductNames); 
            }

        }
        
        if(productVersionIds != null && !productVersionIds.isEmpty()){
            List<ProductVersion> prodVersionsById = productVersionDao.findAllWithIds(new ArrayList<>(productVersionIds));
            if(prodVersionsById != null && (productVersionIds.size() == prodVersionsById.size())){
                productVersions.addAll(prodVersionsById);
            } else {
                // Error
                Set<Long> inexistingProductVersionIds = new HashSet<>(productVersionIds);
                prodVersionsById.stream().forEach(x -> inexistingProductVersionIds.remove(x.getId()));
                errorMsg.append("Product Versions do not exist: ");
                appendErrors(errorMsg, inexistingProductVersionIds); 
            }
        }
        
        if((productNames == null || productNames.isEmpty())  
           && 
           (productVersionIds == null || productVersionIds.isEmpty())){
            productVersions.addAll(productVersionService.getAll());
        }
        
        if(errorMsg.length() > 0){
            throw new IllegalArgumentException(errorMsg.toString());
        }
        
        return productVersions;
    }

    private <T> void appendErrors(StringBuilder errorMsg, Collection<T> invalidItems) {
        Iterator<T> i = invalidItems.iterator();
        errorMsg.append("[");
        while (i.hasNext()) {
            T item = i.next();
            errorMsg.append(item.toString());
            if (i.hasNext()) {
                errorMsg.append(", ");
            }
        }
        errorMsg.append("]");
        errorMsg.append(";");
    }

    @Override
    public Optional<AdvancedArtifactReport> getAdvancedReportFromSCM(SCMReportRequest request)
            throws ScmException, PomAnalysisException, CommunicationException {
        
        SCMLocator scml = request.getScml();
        Set<ProductVersion> relevantProductVersions = getProductVersions(request.getProductNames(), request.getProductVersionIds());
        Set<Long> relevantProductVersionIds = getProductVersionIds(relevantProductVersions);

        Optional<ArtifactReport> artifactReport = createScmReport(request.getScml(), relevantProductVersions);
        // TODO: hardcoded to git
        // hopefully we'll get the cached cloned folder for this repo
        File repoFolder = scmManager.cloneRepository(SCMType.GIT, scml.getScmUrl(),
                scml.getRevision());
        return artifactReport.map(r -> generateAdvancedArtifactReport(r, repoFolder, relevantProductVersionIds));
    }

    @Override
    public Set<AlignmentReportModule> getAligmentReport(SCMLocator scml,
            boolean useUnknownProduct, Set<Long> productIds) throws ScmException,
            PomAnalysisException, CommunicationException {
        Map<GA, Set<GAV>> dependenciesOfModules = scmConnector.getDependenciesOfModules(
                scml.getScmUrl(), scml.getRevision(), scml.getPomPath(), scml.getRepositories());

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

                CompletableFuture<Set<ProductArtifact>> built = filterProducts(useUnknownProduct,
                        productIds, productProvider.getArtifacts(gav));
                intr.put(gav, built);

                CompletableFuture<Set<ProductArtifact>> different = built
                        .thenCompose(b -> getBuiltDifferent(b, useUnknownProduct, productIds, gav));
                diff.put(gav, different);
            }

            CompletableFuture<Void> intrDone = copyCompletedMap(intr, internallyBuilt);
            CompletableFuture<Void> diffDone = copyCompletedMap(diff, differentVersion);

            futures.add(CompletableFuture.allOf(intrDone, diffDone)
                    .thenAccept(x -> fillNotBuilt(blacklisted, internallyBuilt,
                            differentVersion, notBuilt)));
        }
        try{
            futures.stream().forEach(f -> f.join());
        }catch(CompletionException ex){
            throw new CommunicationException(ex);
        }
        return ret;
    }

    private CompletableFuture<Set<ProductArtifact>> getBuiltDifferent(Set<ProductArtifact> built,
            boolean useUnknownProduct, Set<Long> productIds, GAV gav) {
        if (built.isEmpty()) {
            CompletableFuture<Set<ProductArtifact>> different = filterProducts(useUnknownProduct,
                    productIds, productProvider.getArtifacts(gav.getGA()));

            return different.thenApply(d -> {
                setAllVersionDifferences(gav, d);
                return d;
            });
        } else {
            return CompletableFuture.completedFuture(Collections.emptySet());
        }
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

    /**
     * This method sets all different versions from GAV to product
     * @param gav Compared GAV
     * @param different Set of different artifacts
     */
    private void setAllVersionDifferences(GAV gav, Set<ProductArtifact> different) {
        for (ProductArtifact productArtifact : different) {
            String type = VersionComparator.difference(gav.getVersion(),
                    productArtifact.getArtifact().getVersion()).toString();
            productArtifact.setDifferenceType(type);
        }
    }

    @Override
    public Set<BuiltReportModule> getBuiltReport(SCMLocator scml) throws ScmException,
            PomAnalysisException, CommunicationException {
        Map<GA, Set<GAV>> dependenciesOfModules = scmConnector.getDependenciesOfModules(
                scml.getScmUrl(), scml.getRevision(), scml.getPomPath(), scml.getRepositories());
        Set<BuiltReportModule> builtSet = new HashSet<>();
        for (Map.Entry<GA, Set<GAV>> e : dependenciesOfModules.entrySet()) {
            for (GAV gav : e.getValue()) {
                BuiltReportModule report = new BuiltReportModule(gav);
                VersionLookupResult versionLookup = versionFinder.lookupBuiltVersions(gav);
                versionLookup.getBestMatchVersion().ifPresent(bmv -> report.setBuiltVersion(bmv));
                report.setAvailableVersions(versionLookup.getAvailableVersions());
                builtSet.add(report);
            }
        }
        return builtSet;
    }

    private CompletableFuture<Set<ProductArtifact>> filterProducts(boolean useUnknownProduct,
            Set<Long> productIds, CompletableFuture<Set<ProductArtifacts>> artifacts) {
        Set<Product> products = idsToProducts(productIds);

        Predicate<Product> pred = (x) -> true;
        if (products.isEmpty()) {
            pred = pred.and(p -> p.getStatus() == SUPPORTED || p.getStatus() == SUPERSEDED);
            if(useUnknownProduct){
                pred = pred.or(p -> UNKNOWN.equals(p));
            }
        }

        artifacts = filterProductArtifacts(products, artifacts, pred);

        CompletableFuture<Set<ProductArtifact>> thenApply = artifacts.thenApply(m -> mapProducts(m));

        return thenApply;
    }

    private Set<ProductArtifact> mapProducts(Set<ProductArtifacts> products) {
        return products.stream()
                .flatMap(e -> e.getArtifacts().stream()
                        .map(x -> toProductArtifact(e.getProduct(), x)))
                .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(ProductArtifact::getArtifact))));
    }

    private Set<Product> idsToProducts(Set<Long> productIds) {
        Set<Product> products = productIds.stream()
                .map(id -> productVersionDao.read(id))
                .map(ReportsGeneratorImpl::toProduct)
                .collect(Collectors.toSet());
        return products;
    }

    @Override
    public ArtifactReport getReport(GAVRequest gavRequest) throws CommunicationException,
            FindGAVDependencyException {
        if (gavRequest == null)
            throw new IllegalArgumentException("GAV can't be null");

        Set<ProductVersion> relevantProductVersions = getProductVersions(
                gavRequest.getProductNames(), gavRequest.getProductVersionIds());

        Optional<ArtifactReport> artReport = createGavReport(gavRequest, relevantProductVersions);

        return artReport.get();
    }

    private AdvancedArtifactReport generateAdvancedArtifactReport(ArtifactReport report,
            File repoFolder, Set<Long> validProductVersionIds) {
        AdvancedArtifactReport advancedReport = new AdvancedArtifactReport();
        advancedReport.setArtifactReport(report);
        Set<GAV> modulesAnalyzed = new HashSet<>();

        // hopefully we'll get the folder already cloned from before
        populateAdvancedArtifactReportFields(advancedReport, report, modulesAnalyzed, repoFolder,
                validProductVersionIds);
        return advancedReport;
    }

    private void populateAdvancedArtifactReportFields(AdvancedArtifactReport advancedReport,
            ArtifactReport report, Set<GAV> modulesAnalyzed, File repoFolder,
            Set<Long> validProductVersionIds) {
        for (ArtifactReport dep : report.getDependencies()) {
            final GAV gav = dep.getGav();
            if (modulesAnalyzed.contains(gav)) {
                // if module already analyzed, skip
                continue;
            } else if (isDependencyAModule(repoFolder, dep)) {
                // if dependency is a module, but not yet analyzed
                modulesAnalyzed.add(gav);
                populateAdvancedArtifactReportFields(advancedReport, dep, modulesAnalyzed,
                        repoFolder, validProductVersionIds);
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
                    Set<String> versions = new TreeSet<>(new VersionComparator(gav.getVersion()));
                    versions.addAll(getWhitelistedVersions(dep.getGroupId(), dep.getArtifactId(),
                            validProductVersionIds));
                    versions.addAll(dep.getAvailableVersions());
                    if (!versions.isEmpty()) {
                        advancedReport.addCommunityGavWithBuiltVersion(dep.getGav(), versions);
                    } else {
                        advancedReport.addCommunityGav(gav);
                    }
                }
            }
        }
    }

    private Set<String> getWhitelistedVersions(String groupId, String artifactId, Set<Long> validProductVersionIds) {
        
        List<ProductVersionArtifactRelationship> prodVerRels = productVersionService.getProductVersionsWithArtifactsByGA(groupId, artifactId);
        
        // If values are present restrict results based on id
        if(validProductVersionIds != null && !validProductVersionIds.isEmpty()){
            prodVerRels = prodVerRels.stream()
                        .filter(rel -> validProductVersionIds.contains(rel.getProductVersion().getId()))
                        .collect(Collectors.toList());
        }
        
        return prodVerRels.stream()
                .map(rel -> rel.getArtifact().getVersion())
                .collect(Collectors.toCollection(() -> new HashSet<>()));
    }

    private boolean isDependencyAModule(File repoFolder, ArtifactReport dependency) {
        return pomAnalyzer.getPOMFileForGAV(repoFolder, dependency.getGav()).isPresent();
    }

    private ProductArtifact toProductArtifact(Product p, Artifact a) {
        ProductArtifact ret = new ProductArtifact();
        ret.setArtifact(a.getGav());
        ret.setProductName(p.getName());
        ret.setProductVersion(p.getVersion());
        ret.setSupportStatus(p.getStatus());
        return ret;
    }

    @Override
    public List<LookupReport> getLookupReportsForGavs(LookupGAVsRequest request)
            throws CommunicationException{
        Set<Product> products = 
                getProductVersions(request.getProductNames(), request.getProductVersionIds()).stream()
                .map(ReportsGeneratorImpl::toProduct)
                .collect(Collectors.toSet());

        List<CompletableFuture<LookupReport>> reports = new ArrayList<>();
        for(GAV gav : request.getGavs()){
            CompletableFuture<Set<ProductArtifacts>> artifacts = productProvider.getArtifacts(gav.getGA());
            artifacts = filterProductArtifacts(products, artifacts);
            
            reports.add(artifacts.thenApply(pas -> toLookupReport(pas, gav)));
        }

        return reports.stream()
                .map(r -> r.join())
                .collect(Collectors.toList());
    }

    private static Product toProduct(ProductVersion pv) {
        return new Product(pv.getProduct().getName(), pv.getProductVersion());
    }

    private LookupReport toLookupReport(Set<ProductArtifacts> pas, GAV gav) {
        List<String> versions = getVersions(pas, gav);
        Optional<String> bmv = versionFinder.getBestMatchVersionFor(gav, versions);
        LookupReport lookupReport = new LookupReport(gav, bmv.orElse(null), versions,
                blackArtifactService.isArtifactPresent(gav), toWhitelisted(pas));

        return lookupReport;
    }

    private List<String> getVersions(Set<ProductArtifacts> pas, GAV gav) {
        List<String> versions = pas.stream()
                .flatMap(as -> as.getArtifacts().stream())
                .map(a -> a.getGav().getVersion())
                .distinct()
                .sorted(new VersionComparator(gav.getVersion()))
                .collect(Collectors.toList());
        return versions;
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
