package org.jboss.da.reports.impl;

import static org.jboss.da.listings.model.ProductSupportStatus.SUPERSEDED;
import static org.jboss.da.listings.model.ProductSupportStatus.SUPPORTED;
import static org.jboss.da.listings.model.ProductSupportStatus.UNKNOWN;

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
import org.jboss.da.listings.api.model.WhiteArtifact;
import org.jboss.da.listings.api.service.BlackArtifactService;
import org.jboss.da.listings.api.service.ProductVersionService;
import org.jboss.da.listings.model.rest.RestProductInput;
import org.jboss.da.model.rest.GA;
import org.jboss.da.model.rest.GAV;
import org.jboss.da.model.rest.VersionComparator;
import org.jboss.da.reports.api.AdvancedArtifactReport;
import org.jboss.da.reports.api.AlignmentReportModule;
import org.jboss.da.reports.api.ArtifactReport;
import org.jboss.da.reports.api.BuiltReportModule;
import org.jboss.da.reports.api.Product;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
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
    private ProductVersionDAO productVersionDao;

    @Inject
    private ProductDAO productDao;

    @Inject
    private ProductVersionService productVersionService;

    @Inject
    private SCMConnector scmConnector;

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
            Set<ProductVersion> productVersions, Set<Long> productVersionIds)
            throws CommunicationException, FindGAVDependencyException {

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

        VersionLookupResult result = versionFinder.lookupBuiltVersions(dt.getGav());
        ArtifactReport report = toArtifactReport(dt.getGav(), result);

        Set<GAVDependencyTree> nodesVisited = new HashSet<>();
        nodesVisited.add(dt);
        addDependencyReports(report, dt.getDependencies(), nodesVisited);
        addArtifactsFromWhiteList(report, productVersions);

        return Optional.of(report);
    }

    private void addArtifactsFromWhiteList(ArtifactReport report,
            Set<ProductVersion> productVersions) {
        Set<WhiteArtifact> whiteArtifactsAndOrigin = getWhiteArtifacts(productVersions);
        updateArtifact(report, whiteArtifactsAndOrigin);
    }

    private void updateArtifact(ArtifactReport ar, Set<WhiteArtifact> allArtifacts) {

        Set<ArtifactReport> dependencies = ar.getDependencies();

        // Process the current artifact

        GAV currentGav = ar.getGav();

        Set<String> versions = new TreeSet<>(new VersionComparator(currentGav.getVersion()));
        List<String> whitelistVersions = getAvailableWhitelistVersions(allArtifacts, currentGav);
        Optional<String> bestVersion = getBestMatchVersionFromWhitelist(ar.getBestMatchVersion(),
                currentGav, whitelistVersions);

        versions.addAll(whitelistVersions);
        versions.addAll(ar.getAvailableVersions());

        // Update available versions
        ar.setAvailableVersions(new ArrayList<>(versions));

        // Update best match version
        ar.setBestMatchVersion(bestVersion);

        // Recurse or end
        if (dependencies == null || dependencies.isEmpty()) {
            return;
        } else {
            for (ArtifactReport depArtifact : dependencies) {
                updateArtifact(depArtifact, allArtifacts);
            }
        }
    }

    private Optional<String> getBestMatchVersionFromWhitelist(Optional<String> currentBestVersion,
            GAV currentGav, List<String> availableVersionsFromWhitelist) {
        // Establish best match version
        Optional<String> bestWhiteVersion = versionFinder.getBestMatchVersionFor(currentGav,
                availableVersionsFromWhitelist);
        if (bestWhiteVersion != null && bestWhiteVersion.isPresent()) {
            if (currentBestVersion.isPresent()) {
                currentBestVersion = versionFinder.getBestMatchVersionFor(currentGav,
                        Arrays.asList(bestWhiteVersion.get(), currentBestVersion.get()));

            } else {
                currentBestVersion = bestWhiteVersion;
            }
        }
        return currentBestVersion;
    }

    private List<String> getAvailableWhitelistVersions(Set<WhiteArtifact> allArtifacts,
            GAV currentGav) {
        Set<WhiteArtifact> whiteArtifactsForCurrentGav = allArtifacts.stream()
                .filter(x -> {
                    return x.getGa().getArtifactId()
                            .equals(
                           currentGav.getGA().getArtifactId())
                            
                            &&
                           
                           x.getGa().getGroupId()
                            .equals(
                           currentGav.getGA().getGroupId());
                })
                .collect(Collectors.toSet());
                
        List<String> whiteVersionsForCurrentGav = whiteArtifactsForCurrentGav.stream()
                .map(x -> x.getVersion())
                .collect(Collectors.toList());
        
        List<String> availableVersionsFromWhitelist = versionFinder.getBuiltVersionsFor(currentGav, whiteVersionsForCurrentGav);
        return availableVersionsFromWhitelist;
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

    private Set<WhiteArtifact> getWhiteArtifacts(Set<ProductVersion> prodVers) {
        Set<WhiteArtifact> mapProdVersToArtifact = new HashSet<>();
        for (ProductVersion currentProdVer : prodVers) {
            mapProdVersToArtifact.addAll(currentProdVer.getWhiteArtifacts());
        }
        return mapProdVersToArtifact;
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
                setAllVersionDifferences(gav, different);

                if (!bl && built.isEmpty() && different.isEmpty()) {
                    notBuilt.add(gav);
                }
            }
            ret.add(module);
        }
        return ret;
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

    private Set<ProductArtifact> filterAndMapProducts(Set<Long> productIds,
            Stream<ProductVersionArtifactRelationship> internallyStream) {
        if (productIds.isEmpty()) { // All SUPPORTED or SUPERSEDED
            internallyStream = internallyStream.filter(p ->
                    p.getProductVersion().getSupport() == SUPPORTED ||
                            p.getProductVersion().getSupport() == SUPERSEDED);
        } else { // Specified
            internallyStream = internallyStream.filter(p -> productIds.contains(p
                    .getProductVersion().getId()));
        }

        return internallyStream
                .map(x -> toProductArtifact(x))
                .collect(Collectors.toCollection(HashSet::new));
    }

    @Override
    public ArtifactReport getReport(GAVRequest gavRequest) throws CommunicationException,
            FindGAVDependencyException {
        if (gavRequest == null)
            throw new IllegalArgumentException("GAV can't be null");

        Set<ProductVersion> relevantProductVersions = getProductVersions(
                gavRequest.getProductNames(), gavRequest.getProductVersionIds());
        Set<Long> relevantProductVersionIds = getProductVersionIds(relevantProductVersions);

        Optional<ArtifactReport> artReport = createGavReport(gavRequest, relevantProductVersions,
                relevantProductVersionIds);

        return artReport.get();
    }

    private ArtifactReport toArtifactReport(GAV gav, VersionLookupResult result) {
        ArtifactReport report = new ArtifactReport(gav);
        report.setAvailableVersions(result.getAvailableVersions());
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

    @Override
    public List<LookupReport> getLookupReportsForGavs(LookupGAVsRequest request) throws CommunicationException {
        
        // Use a concurrent set since we're using parallel stream to add stuff
        Set<LookupReport> reports = ConcurrentHashMap.newKeySet();

        boolean communicationSucceeded = request.getGavs().parallelStream().distinct().map((gav) -> {
            try {
                VersionLookupResult lookupResult = versionFinder.lookupBuiltVersions(gav);
                LookupReport lookupReport = toLookupReport(gav, lookupResult);
                reports.add(lookupReport);
                return true;
            } catch (CommunicationException ex) {
                log.error("Communication with remote repository failed", ex);
                return false;
            }
        }).allMatch(x -> {
            return x;
        });

        if(communicationSucceeded){
            Set<ProductVersion> relevantProductVersions = getProductVersions(request.getProductNames(), request.getProductVersionIds());
            Set<WhiteArtifact> whiteArtifacts = getWhiteArtifacts(relevantProductVersions);
            
            reports.stream().forEach(x -> 
                {
                    GAV currentGav = x.getGav();
                    List<String> availableVersionsFromWhitelist = getAvailableWhitelistVersions(whiteArtifacts, currentGav);
                    String bestVerStr = x.getBestMatchVersion();
                    Optional<String> bestVersion = getBestMatchVersionFromWhitelist(
                            bestVerStr == null ? Optional.empty() : Optional.of(bestVerStr), 
                            currentGav, availableVersionsFromWhitelist
                    );
                    
                    // Combine existing with whitelist versions
                    if(x.getAvailableVersions() != null){
                        availableVersionsFromWhitelist.addAll(x.getAvailableVersions());
                    }
                    x.setAvailableVersions(availableVersionsFromWhitelist);
                    x.setBestMatchVersion(bestVersion.orElse(null));
                }
             );
            
            return new ArrayList<>(reports);
        } else {
            throw new CommunicationException("Communication with remote repository failed");
        }
    }

    private LookupReport toLookupReport(GAV gav, VersionLookupResult lookupResult) {
        return new LookupReport(gav, lookupResult.getBestMatchVersion().orElse(null),
                lookupResult.getAvailableVersions(), blackArtifactService.isArtifactPresent(gav),
                toWhitelisted(getWhitelistedProducts(gav)));
    }

    private static List<RestProductInput> toWhitelisted(List<ProductVersion> whitelisted) {
        return whitelisted
                .stream()
                .map(pv -> new RestProductInput(pv.getProduct().getName(), pv.getProductVersion(),
                        pv.getSupport()))
                .collect(Collectors.toList());
    }
}
