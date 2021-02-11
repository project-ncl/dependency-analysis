package org.jboss.da.products.impl;

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
import org.slf4j.Logger;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.jboss.da.listings.model.ProductSupportStatus.UNKNOWN;
import static org.jboss.da.reports.impl.ReportsGeneratorImpl.DEFAULT_SUFFIX;

/**
 * A product provider reading artifacts from an external source without an information about products.
 *
 * @author <a href="mailto:pkocandr@redhat.com">Petr Kocandrle</a>
 */
public abstract class AbstractProductProvider implements ProductProvider {

    @Inject
    protected Logger log;

    @Inject
    @UserLog
    protected Logger userLog;

    @Resource
    private ManagedExecutorService executorService;

    private VersionParser versionParser = new VersionParser(DEFAULT_SUFFIX);

    /**
     * Sets the suffix that distinguish product artifacts in the repository.
     *
     * @param suffix Suffix of the product artifacts.
     */
    public void setVersionSuffix(String suffix) {
        versionParser = new VersionParser(suffix);
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
    public CompletableFuture<Map<Product, Set<String>>> getVersions(Artifact artifact) {
        switch (artifact.getType()) {
            case MAVEN: {
                GA ga = ((MavenArtifact) artifact).getGav().getGA();
                CompletableFuture<Set<String>> versions = supplyAsync(
                        () -> getVersionsStreamMaven(ga).filter(v -> versionParser.parse(v).isSuffixed())
                                .distinct()
                                .collect(Collectors.toSet()));
                return versions.thenApply(rv -> Collections.singletonMap(Product.UNKNOWN, rv));
            }
            case NPM: {
                CompletableFuture<Set<String>> versions = supplyAsync(
                        () -> getVersionsStreamNPM(artifact.getName()).filter(v -> versionParser.parse(v).isSuffixed())
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
    public CompletableFuture<Set<String>> getAllVersions(Artifact artifact) {
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
        Set<Artifact> allArtifacts = getVersionsStreamMaven(ga).filter(v -> versionParser.parse(v).isSuffixed())
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
                .map(v -> new NPMArtifact(name, v))
                .collect(Collectors.toSet());
        if (allArtifacts.isEmpty()) {
            return Collections.emptySet();
        }
        return Collections.singleton(new ProductArtifacts(Product.UNKNOWN, allArtifacts));
    }

    abstract Stream<String> getVersionsStreamMaven(GA ga);

    abstract Stream<String> getVersionsStreamNPM(String name);

}
