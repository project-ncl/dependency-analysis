package org.jboss.da.reports.impl;

import org.jboss.da.common.CommunicationException;
import org.jboss.da.common.json.LookupMode;
import org.jboss.da.common.util.Configuration;
import org.jboss.da.common.util.ConfigurationParseException;
import org.jboss.da.common.version.VersionAnalyzer;
import org.jboss.da.listings.api.service.BlackArtifactService;
import org.jboss.da.lookup.model.MavenLookupResult;
import org.jboss.da.lookup.model.NPMLookupResult;
import org.jboss.da.model.rest.GA;
import org.jboss.da.model.rest.GAV;
import org.jboss.da.model.rest.NPMPackage;
import org.jboss.da.products.api.Artifact;
import org.jboss.da.products.api.MavenArtifact;
import org.jboss.da.products.api.NPMArtifact;
import org.jboss.da.products.api.ProductProvider;
import org.jboss.da.products.impl.AggregatedProductProvider;
import org.jboss.da.products.impl.PncProductProvider;
import org.jboss.da.products.impl.RepositoryProductProvider;
import org.jboss.da.reports.api.LookupGenerator;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.jboss.da.reports.impl.FuturesUtil.joinFutures;

@ApplicationScoped
public class LookupGeneratorImpl implements LookupGenerator {

    @Inject
    @RepositoryProductProvider.Repository
    private RepositoryProductProvider repositoryProductProvider;

    @Inject
    @PncProductProvider.Pnc
    private PncProductProvider pncProductProvider;

    @Inject
    private AggregatedProductProvider aggProductProvider;

    @Inject
    private BlackArtifactService blackArtifactService;

    private Map<String, LookupMode> modes;

    @Inject
    public LookupGeneratorImpl(Configuration config) throws ConfigurationParseException {
        modes = config.getConfig()
                .getModes()
                .stream()
                .collect(Collectors.toMap(LookupMode::getName, Function.identity()));
    }

    @Override
    public Set<MavenLookupResult> lookupBestMatchMaven(Set<GAV> gavs, String mode, boolean brewPullActive)
            throws CommunicationException {
        LookupMode lookupMode = getMode(mode);
        ProductProvider productProvider = setupProductProvider(brewPullActive, lookupMode);
        Map<GA, CompletableFuture<Set<String>>> productArtifacts = getProductArtifacts(productProvider, gavs);
        return createLookupResult(gavs, lookupMode, productArtifacts);
    }

    @Override
    public Set<NPMLookupResult> lookupBestMatchNPM(Set<NPMPackage> packages, String mode)
            throws CommunicationException {
        LookupMode lookupMode = getMode(mode);
        pncProductProvider.setLookupMode(lookupMode);
        Map<String, CompletableFuture<Set<String>>> productArtifacts = getProductArtifacts(packages);
        return createLookupResultNpm(packages, lookupMode, productArtifacts);
    }

    private ProductProvider setupProductProvider(boolean brewPullActive, LookupMode mode) {
        pncProductProvider.setLookupMode(mode);
        if (brewPullActive) {
            repositoryProductProvider.setLookupMode(mode);
            return aggProductProvider;
        } else {
            return pncProductProvider;
        }
    }

    private Map<GA, CompletableFuture<Set<String>>> getProductArtifacts(
            ProductProvider productProvider,
            Set<GAV> gavs) {
        return gavs.stream()
                .map(GAV::getGA)
                .distinct()
                .map(ga -> new MavenArtifact(new GAV(ga, "0.0.0")))
                .collect(
                        Collectors.toMap(
                                a -> a.getGav().getGA(),
                                a -> filterBlacklistedArtifacts(
                                        productProvider.getAllVersions(a),
                                        a.getGav().getGA())));
    }

    private Map<String, CompletableFuture<Set<String>>> getProductArtifacts(Set<NPMPackage> packages) {
        return packages.stream()
                .map(NPMPackage::getName)
                .distinct()
                .map(name -> new NPMArtifact(name, "0.0.0"))
                .collect(Collectors.toMap(Artifact::getName, pncProductProvider::getAllVersions));
    }

    private CompletableFuture<Set<String>> filterBlacklistedArtifacts(CompletableFuture<Set<String>> versions, GA ga) {
        Predicate<String> isNotBlacklisted = version -> {
            GAV gav = new GAV(ga, version);
            return !blackArtifactService.isArtifactPresent(gav);
        };
        return versions.thenApply(v -> v.stream().filter(isNotBlacklisted).collect(Collectors.toSet()));
    }

    private Set<MavenLookupResult> createLookupResult(
            Set<GAV> gavs,
            LookupMode mode,
            Map<GA, CompletableFuture<Set<String>>> artifactsMap) throws CommunicationException {

        VersionAnalyzer va = new VersionAnalyzer(mode.getSuffixes());

        Set<CompletableFuture<MavenLookupResult>> futures = gavs.stream()
                .map(gav -> artifactsMap.get(gav.getGA()).thenApply(pas -> getLookupResult(va, gav, pas)))
                .collect(Collectors.toSet());

        return joinFutures(futures);
    }

    private Set<NPMLookupResult> createLookupResultNpm(
            Set<NPMPackage> packages,
            LookupMode mode,
            Map<String, CompletableFuture<Set<String>>> artifactsMap) throws CommunicationException {
        VersionAnalyzer va = new VersionAnalyzer(mode.getSuffixes());

        Set<CompletableFuture<NPMLookupResult>> futures = packages.stream()
                .map(pkg -> artifactsMap.get(pkg.getName()).thenApply(f -> getLookupResult(va, pkg, f)))
                .collect(Collectors.toSet());

        return joinFutures(futures);
    }

    private MavenLookupResult getLookupResult(VersionAnalyzer va, GAV gav, Set<String> versions) {
        Optional<String> bmv = va.findBiggestMatchingVersion(gav.getVersion(), versions);
        return new MavenLookupResult(gav, bmv.orElse(null));
    }

    private NPMLookupResult getLookupResult(VersionAnalyzer va, NPMPackage pkg, Set<String> versions) {
        Optional<String> bmv = va.findBiggestMatchingVersion(pkg.getVersion(), versions);
        return new NPMLookupResult(pkg, bmv.orElse(null));
    }

    private LookupMode getMode(String mode) {
        LookupMode lookupMode = modes.get(mode);
        if (lookupMode == null) {
            throw new IllegalArgumentException("Unknown lookup mode " + mode);
        }
        return lookupMode;
    }

}
