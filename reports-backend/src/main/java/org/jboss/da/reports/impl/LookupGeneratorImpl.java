package org.jboss.da.reports.impl;

import org.jboss.da.common.CommunicationException;
import org.jboss.da.common.ScopedStrategy;
import org.jboss.da.common.ScopedGAVStrategy;
import org.jboss.da.common.json.LookupMode;
import org.jboss.da.common.util.Configuration;
import org.jboss.da.common.util.ConfigurationParseException;
import org.jboss.da.listings.api.service.BlackArtifactService;
import org.jboss.da.lookup.model.MavenLatestResult;
import org.jboss.da.lookup.model.MavenLookupResult;
import org.jboss.da.lookup.model.MavenVersionsResult;
import org.jboss.da.lookup.model.NPMLookupResult;
import org.jboss.da.lookup.model.NPMVersionsResult;
import org.jboss.pnc.api.constants.versions.VersionDistanceRule;
import org.jboss.pnc.api.constants.versions.VersionFilter;
import org.jboss.da.model.rest.Strategy;
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
import org.jboss.pnc.api.dependencyanalyzer.dto.QualifiedVersion;
import org.jboss.pnc.common.alignment.ranking.AlignmentPredicate;
import org.jboss.pnc.common.alignment.ranking.AlignmentRanking;
import org.jboss.pnc.common.alignment.ranking.VersionStrategy;
import org.jboss.pnc.common.alignment.ranking.exception.ValidationException;
import org.jboss.pnc.common.alignment.ranking.tokenizer.QualifierToken;
import org.jboss.pnc.common.alignment.ranking.tokenizer.Token;
import org.jboss.pnc.common.version.VersionAnalyzer;
import org.jboss.pnc.dto.requests.QValue;
import org.jboss.pnc.enums.ArtifactQuality;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
    public Set<MavenLookupResult> lookupBestMatchMaven(
            Set<GAV> gavs,
            String mode,
            boolean brewPullActive,
            Set<Strategy> strategies) throws CommunicationException, ValidationException {
        LookupMode lookupMode = getMode(mode, false);
        Set<ScopedGAVStrategy> compiledStrategies = compileMavenStrategies(strategies);

        ProductProvider productProvider = setupProductProvider(
                brewPullActive,
                lookupMode,
                extractQualifiers(compiledStrategies));
        Map<GA, CompletableFuture<Set<QualifiedVersion>>> productArtifacts = getArtifactVersions(
                productProvider,
                gavs,
                true);
        return createLookupResult(gavs, lookupMode, productArtifacts, compiledStrategies);
    }

    private static Set<ScopedGAVStrategy> compileMavenStrategies(Set<Strategy> strategies) {
        if (strategies == null) {
            return Collections.emptySet();
        }
        return strategies.stream().map(ScopedGAVStrategy::from).collect(Collectors.toSet());
    }

    private static Set<QValue> extractQualifiers(Set<? extends ScopedStrategy<?>> strategies) {
        Set<Token> allTokens = new HashSet<>();
        for (var strategy : strategies) {
            AlignmentRanking ranks = strategy.getRanks();
            AlignmentPredicate allow = strategy.getAllowList();
            AlignmentPredicate deny = strategy.getDenyList();
            allTokens.addAll(ranks.getRanksAsTokens().stream().flatMap(Collection::stream).collect(Collectors.toSet()));
            allTokens.addAll(allow.getTokens());
            allTokens.addAll(deny.getTokens());
        }
        return allTokens.stream()
                .filter(token -> token instanceof QualifierToken)
                .map(token -> (QualifierToken) token)
                .map(qToken -> QValue.valueOf(qToken.toString()))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<MavenVersionsResult> lookupVersionsMaven(
            Set<GAV> gavs,
            VersionFilter vf,
            VersionDistanceRule distanceRule,
            String mode,
            boolean brewPullActive,
            boolean includeBad) throws CommunicationException {
        LookupMode lookupMode = getMode(mode, includeBad);
        ProductProvider productProvider = setupProductProvider(brewPullActive, lookupMode, Set.of());
        Map<GA, CompletableFuture<Set<QualifiedVersion>>> productArtifacts = getArtifactVersions(
                productProvider,
                gavs,
                !includeBad);
        return createVersionsResult(gavs, lookupMode, vf, distanceRule, productArtifacts);
    }

    @Override
    public Set<MavenLatestResult> lookupLatestMaven(Set<GAV> gavs, String mode) throws CommunicationException {
        LookupMode lookupMode = getMode(mode, true);
        ProductProvider productProvider = setupProductProvider(true, lookupMode, Set.of());
        Map<GA, CompletableFuture<Set<QualifiedVersion>>> productArtifacts = getArtifactVersions(
                productProvider,
                gavs,
                false);
        return createLatestResult(gavs, lookupMode, productArtifacts);
    }

    @Override
    public Set<NPMLookupResult> lookupBestMatchNPM(Set<NPMPackage> packages, String mode)
            throws CommunicationException {
        LookupMode lookupMode = getMode(mode, false);
        pncProductProvider.setLookupMode(lookupMode);
        Map<String, CompletableFuture<Set<QualifiedVersion>>> productArtifacts = getArtifactVersions(packages);
        return createLookupResultNpm(packages, lookupMode, productArtifacts);
    }

    @Override
    public Set<NPMVersionsResult> lookupVersionsNPM(
            Set<NPMPackage> packages,
            VersionFilter vf,
            VersionDistanceRule distanceRule,
            String mode,
            boolean includeBad) throws CommunicationException {
        LookupMode lookupMode = getMode(mode, includeBad);
        pncProductProvider.setLookupMode(lookupMode);
        Map<String, CompletableFuture<Set<QualifiedVersion>>> productArtifacts = getArtifactVersions(packages);
        return createVersionsResultNpm(packages, lookupMode, vf, distanceRule, productArtifacts);
    }

    private ProductProvider setupProductProvider(boolean brewPullActive, LookupMode mode, Set<QValue> qualifiers) {
        pncProductProvider.setLookupMode(mode);
        pncProductProvider.setQualifiers(qualifiers);
        if (brewPullActive) {
            repositoryProductProvider.setLookupMode(mode);
            return aggProductProvider;
        } else {
            return pncProductProvider;
        }
    }

    private Map<GA, CompletableFuture<Set<QualifiedVersion>>> getArtifactVersions(
            ProductProvider productProvider,
            Set<GAV> gavs,
            boolean filterBlacklisted) {
        Map<GA, CompletableFuture<Set<QualifiedVersion>>> ret = new HashMap<>();
        Set<GA> distinctGAs = gavs.stream().map(GAV::getGA).collect(Collectors.toSet());
        Set<GAV> cache = blackArtifactService.prefetchGAs(distinctGAs);
        for (GA ga : distinctGAs) {
            MavenArtifact mavenArtifact = new MavenArtifact(new GAV(ga, "0.0.0"));
            CompletableFuture<Set<QualifiedVersion>> versions = productProvider.getAllVersions(mavenArtifact);
            if (filterBlacklisted) {
                versions = filterBlacklistedArtifacts(versions, cache, ga);
            }
            ret.put(ga, versions);
        }
        return ret;
    }

    private Map<String, CompletableFuture<Set<QualifiedVersion>>> getArtifactVersions(Set<NPMPackage> packages) {
        return packages.stream()
                .map(NPMPackage::getName)
                .distinct()
                .map(name -> new NPMArtifact(name, "0.0.0"))
                .collect(Collectors.toMap(Artifact::getName, pncProductProvider::getAllVersions));
    }

    private CompletableFuture<Set<QualifiedVersion>> filterBlacklistedArtifacts(
            CompletableFuture<Set<QualifiedVersion>> versions,
            Set<GAV> cache,
            GA ga) {
        Predicate<QualifiedVersion> isNotBlacklisted = version -> !blackArtifactService
                .isBlocklisted(cache, ga, version);
        return versions.thenApply(v -> v.stream().filter(isNotBlacklisted).collect(Collectors.toSet()));
    }

    private Set<MavenLookupResult> createLookupResult(
            Set<GAV> gavs,
            LookupMode mode,
            Map<GA, CompletableFuture<Set<QualifiedVersion>>> artifactsMap,
            Set<ScopedGAVStrategy> compiledStrategies) throws CommunicationException {

        Map<ScopedStrategy<GAV>, VersionAnalyzer> vas = compiledStrategies.stream()
                .collect(Collectors.toMap(Function.identity(), con -> new VersionAnalyzer(mode.getSuffixes(), con)));
        VersionAnalyzer def = new VersionAnalyzer(mode.getSuffixes(), VersionStrategy.none());

        Set<CompletableFuture<MavenLookupResult>> futures = gavs.stream()
                .map(
                        gav -> artifactsMap.get(gav.getGA())
                                .thenApply(pas -> getLookupResult(chooseVa(gav, vas, def), gav, pas)))
                .collect(Collectors.toSet());

        return joinFutures(futures);
    }

    /**
     * Returns VersionAnalyzer with the closest match based on artifact. If map is empty or there was no match
     * (matchSignificance is 0), return a default VersionAnalyzer.
     *
     * @param artifact an artifact (GAV or NPM)
     * @param vas Map of Strategies and VAs
     * @param def default VA to return
     * @return closest VA or default VA on empty map or no match
     * @param <T> GAV or NPM
     */
    private <T> VersionAnalyzer chooseVa(T artifact, Map<ScopedStrategy<T>, VersionAnalyzer> vas, VersionAnalyzer def) {
        var max = vas.keySet().stream().max((cc1, cc2) -> {
            int sim1 = cc1.matchSignificance(artifact);
            int sim2 = cc2.matchSignificance(artifact);
            return Integer.compare(sim1, sim2);
        });

        if (max.map(k -> k.matchSignificance(artifact)).orElse(0) != 0) {
            return vas.get(max.get());
        }

        return def;
    }

    private Set<MavenVersionsResult> createVersionsResult(
            Set<GAV> gavs,
            LookupMode mode,
            VersionFilter vf,
            VersionDistanceRule distanceRule,
            Map<GA, CompletableFuture<Set<QualifiedVersion>>> artifactsMap) throws CommunicationException {

        VersionAnalyzer va = new VersionAnalyzer(mode.getSuffixes(), distanceRule);

        Set<CompletableFuture<MavenVersionsResult>> futures = gavs.stream()
                .map(
                        gav -> artifactsMap.get(gav.getGA())
                                .thenApply(
                                        pas -> pas.stream()
                                                .map(QualifiedVersion::getVersion)
                                                .collect(Collectors.toSet()))
                                .thenApply(pas -> getMatchingVersions(va, vf, gav, pas)))
                .collect(Collectors.toSet());

        return joinFutures(futures);
    }

    private Set<MavenLatestResult> createLatestResult(
            Set<GAV> gavs,
            LookupMode mode,
            Map<GA, CompletableFuture<Set<QualifiedVersion>>> artifactsMap) throws CommunicationException {

        VersionAnalyzer va = new VersionAnalyzer(Collections.singletonList(mode.getIncrementSuffix()));

        Set<CompletableFuture<MavenLatestResult>> futures = gavs.stream()
                .map(gav -> artifactsMap.get(gav.getGA()).thenApply(pas -> getLatestResult(va, gav, pas)))
                .collect(Collectors.toSet());

        return joinFutures(futures);
    }

    private Set<NPMLookupResult> createLookupResultNpm(
            Set<NPMPackage> packages,
            LookupMode mode,
            Map<String, CompletableFuture<Set<QualifiedVersion>>> artifactsMap) throws CommunicationException {
        VersionAnalyzer va = new VersionAnalyzer(mode.getSuffixes());

        Set<CompletableFuture<NPMLookupResult>> futures = packages.stream()
                .map(pkg -> artifactsMap.get(pkg.getName()).thenApply(f -> getLookupResult(va, pkg, f)))
                .collect(Collectors.toSet());

        return joinFutures(futures);
    }

    private Set<NPMVersionsResult> createVersionsResultNpm(
            Set<NPMPackage> packages,
            LookupMode mode,
            VersionFilter vf,
            VersionDistanceRule distanceRule,
            Map<String, CompletableFuture<Set<QualifiedVersion>>> artifactsMap) throws CommunicationException {
        VersionAnalyzer va = new VersionAnalyzer(mode.getSuffixes(), distanceRule);

        Set<CompletableFuture<NPMVersionsResult>> futures = packages.stream()
                .map(
                        pkg -> artifactsMap.get(pkg.getName())
                                .thenApply(
                                        f -> f.stream().map(QualifiedVersion::getVersion).collect(Collectors.toSet()))
                                .thenApply(f -> getMatchingVersions(va, vf, pkg, f)))
                .collect(Collectors.toSet());

        return joinFutures(futures);
    }

    private MavenLookupResult getLookupResult(VersionAnalyzer va, GAV gav, Set<QualifiedVersion> versions) {
        Optional<String> bmv = va.findBiggestMatchingVersion(gav.getVersion(), versions);
        return new MavenLookupResult(gav, bmv.orElse(null));
    }

    private MavenLatestResult getLatestResult(VersionAnalyzer va, GAV gav, Set<QualifiedVersion> versions) {
        Optional<String> bmv = va.findBiggestMatchingVersion(gav.getVersion(), versions);
        return new MavenLatestResult(gav, bmv.orElse(null));
    }

    private MavenVersionsResult getMatchingVersions(
            VersionAnalyzer va,
            VersionFilter vf,
            GAV gav,
            Set<String> versions) {
        List<String> availableVersions = va.filterVersions(gav.getVersion(), vf, versions);
        return new MavenVersionsResult(gav, availableVersions);
    }

    private NPMVersionsResult getMatchingVersions(
            VersionAnalyzer va,
            VersionFilter vf,
            NPMPackage pkg,
            Set<String> versions) {
        List<String> availableVersions = va.filterVersions(pkg.getVersion(), vf, versions);
        return new NPMVersionsResult(pkg, availableVersions);
    }

    private NPMLookupResult getLookupResult(VersionAnalyzer va, NPMPackage pkg, Set<QualifiedVersion> versions) {
        Optional<String> bmv = va.findBiggestMatchingVersion(pkg.getVersion(), versions);
        return new NPMLookupResult(pkg, bmv.orElse(null));
    }

    private LookupMode getMode(String mode, boolean includeAll) {
        LookupMode lookupMode = modes.get(mode);
        if (lookupMode == null) {
            throw new IllegalArgumentException("Unknown lookup mode " + mode);
        }
        if (includeAll) {
            lookupMode = lookupMode.toBuilder().artifactQualities(EnumSet.allOf(ArtifactQuality.class)).build();
        }
        return lookupMode;
    }

}
