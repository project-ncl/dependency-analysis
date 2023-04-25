package org.jboss.da.products.impl;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;

import org.jboss.da.common.json.LookupMode;
import org.jboss.da.common.util.UserLog;
import org.jboss.da.common.version.VersionParser;
import org.jboss.da.listings.model.ProductSupportStatus;
import org.jboss.da.model.rest.GA;
import org.jboss.da.model.rest.GAV;
import org.jboss.da.products.api.Artifact;
import org.jboss.da.products.api.MavenArtifact;
import org.jboss.da.products.api.NPMArtifact;
import org.jboss.da.products.api.Product;
import org.jboss.da.products.api.ProductArtifacts;
import org.jboss.da.products.api.ProductProvider;
import org.jboss.pnc.api.dependencyanalyzer.dto.Version;
import org.jboss.pnc.dto.requests.QValue;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.BuildCategory;
import org.slf4j.Logger;

import static org.jboss.da.listings.model.ProductSupportStatus.UNKNOWN;
import static org.jboss.da.reports.impl.ReportsGeneratorImpl.DEFAULT_SUFFIX;

/**
 * A product provider reading artifacts from an external source without an information about products.
 *
 * @author <a href="mailto:pkocandr@redhat.com">Petr Kocandrle</a>
 */
public abstract class AbstractProductProvider implements ProductProvider {

    private static final LookupMode DEFAULT_MODE = new LookupMode();
    static {
        DEFAULT_MODE.setName("DEFAULT");
        DEFAULT_MODE.getBuildCategories().add(BuildCategory.STANDARD);
        DEFAULT_MODE.getArtifactQualities().add(ArtifactQuality.NEW);
        DEFAULT_MODE.getArtifactQualities().add(ArtifactQuality.VERIFIED);
        DEFAULT_MODE.getArtifactQualities().add(ArtifactQuality.TESTED);
        DEFAULT_MODE.getSuffixes().add(DEFAULT_SUFFIX);
    }

    @Inject
    protected Logger log;

    @Inject
    @UserLog
    protected Logger userLog;

    @Resource
    private ManagedExecutorService executorService;

    private VersionParser versionParser = new VersionParser(DEFAULT_SUFFIX);

    protected LookupMode mode = DEFAULT_MODE;

    /**
     * Sets the lookup configuration mode.
     *
     * @param mode The configuration of the lookup.
     */
    public void setLookupMode(LookupMode mode) {
        versionParser = new VersionParser(mode.getSuffixes());
        this.mode = mode;
    }

    private <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier) {
        return CompletableFuture.supplyAsync(supplier, executorService);
    }

    @Override
    public CompletableFuture<Set<Product>> getAllProducts() {
        return CompletableFuture.completedFuture(Collections.emptySet());
    }

    @Override
    public CompletableFuture<Set<Product>> getProductsByName(String name) {
        return CompletableFuture.completedFuture(Collections.emptySet());
    }

    @Override
    public CompletableFuture<Set<Product>> getProductsByStatus(ProductSupportStatus status) {
        return CompletableFuture.completedFuture(Collections.emptySet());
    }

    @Override
    public CompletableFuture<Set<Artifact>> getArtifacts(Product product) {
        return CompletableFuture.completedFuture(Collections.emptySet());
    }

    @Override
    public CompletableFuture<Set<ProductArtifacts>> getArtifacts(Artifact artifact) {
        return getArtifacts0(artifact);
    }

    @Override
    public CompletableFuture<Set<ProductArtifacts>> getArtifacts(Artifact artifact, ProductSupportStatus status) {
        if (status != UNKNOWN) {
            return CompletableFuture.completedFuture(Collections.emptySet());
        }
        return getArtifacts0(artifact);
    }

    @Override
    public CompletableFuture<Map<Product, Set<Version>>> getVersions(Artifact artifact) {
        switch (artifact.getType()) {
            case MAVEN: {
                GA ga = ((MavenArtifact) artifact).getGav().getGA();
                CompletableFuture<Set<Version>> versions = supplyAsync(
                        () -> getVersionsStreamMaven(ga).filter(v -> versionParser.parse(v.getVersion()).isSuffixed())
                                .distinct()
                                .collect(Collectors.toSet()));
                return versions.thenApply(rv -> Collections.singletonMap(Product.UNKNOWN, rv));
            }
            case NPM: {
                CompletableFuture<Set<Version>> versions = supplyAsync(
                        () -> getVersionsStreamNPM(artifact.getName())
                                .filter(v -> versionParser.parse(v.getVersion()).isSuffixed())
                                .distinct()
                                .collect(Collectors.toSet()));
                return versions.thenApply(rv -> Collections.singletonMap(Product.UNKNOWN, rv));
            }
            default: {
                return CompletableFuture.completedFuture(Collections.emptyMap());
            }
        }
    }

    @Override
    public CompletableFuture<Set<Version>> getAllVersions(Artifact artifact) {
        switch (artifact.getType()) {
            case MAVEN: {
                GA ga = ((MavenArtifact) artifact).getGav().getGA();
                return supplyAsync(() -> getVersionsStreamMaven(ga).collect(Collectors.toSet()));
            }
            case NPM: {
                return supplyAsync(() -> getVersionsStreamNPM(artifact.getName()).collect(Collectors.toSet()));
            }
            default: {
                return CompletableFuture.completedFuture(Collections.emptySet());
            }
        }
    }

    private CompletableFuture<Set<ProductArtifacts>> getArtifacts0(Artifact artifact) {
        switch (artifact.getType()) {
            case MAVEN: {
                GA ga = ((MavenArtifact) artifact).getGav().getGA();
                return supplyAsync(() -> getArtifactsMaven(ga));
            }
            case NPM: {
                return supplyAsync(() -> getArtifactsNPM(artifact.getName()));
            }
            default: {
                return CompletableFuture.completedFuture(Collections.emptySet());
            }
        }
    }

    private Set<ProductArtifacts> getArtifactsMaven(GA ga) {
        Set<Artifact> allArtifacts = getVersionsStreamMaven(ga).map(Version::getVersion)
                .filter(v -> versionParser.parse(v).isSuffixed())
                .distinct()
                .map(x -> new GAV(ga, x))
                .map(MavenArtifact::new)
                .collect(Collectors.toSet());
        if (allArtifacts.isEmpty()) {
            return Collections.emptySet();
        }
        return Collections.singleton(new ProductArtifacts(Product.UNKNOWN, allArtifacts));
    }

    private Set<ProductArtifacts> getArtifactsNPM(String name) {
        Set<Artifact> allArtifacts = getVersionsStreamNPM(name).filter(v -> versionParser.parse(v).isSuffixed())
                .distinct()
                .map(v -> new NPMArtifact(name, v.getVersion()))
                .collect(Collectors.toSet());
        if (allArtifacts.isEmpty()) {
            return Collections.emptySet();
        }
        return Collections.singleton(new ProductArtifacts(Product.UNKNOWN, allArtifacts));
    }

    abstract Stream<Version> getVersionsStreamMaven(GA ga);

    abstract Stream<Version> getVersionsStreamNPM(String name);

}
