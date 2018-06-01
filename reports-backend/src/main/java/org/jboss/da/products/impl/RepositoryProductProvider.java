package org.jboss.da.products.impl;

import org.jboss.da.common.CommunicationException;
import org.jboss.da.common.version.VersionParser;
import org.jboss.da.communication.aprox.api.AproxConnector;
import org.jboss.da.listings.model.ProductSupportStatus;
import static org.jboss.da.listings.model.ProductSupportStatus.UNKNOWN;
import org.jboss.da.model.rest.GA;
import org.jboss.da.model.rest.GAV;
import org.jboss.da.products.api.Artifact;
import org.jboss.da.products.api.Product;
import org.jboss.da.products.api.ProductArtifacts;
import org.jboss.da.products.api.ProductProvider;
import org.jboss.da.products.impl.RepositoryProductProvider.Repository;
import org.slf4j.Logger;

import javax.annotation.Resource;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import javax.inject.Qualifier;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.enterprise.context.RequestScoped;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Repository
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@RequestScoped
public class RepositoryProductProvider implements ProductProvider {

    @Inject
    private Logger log;

    @Inject
    private AproxConnector aproxConnector;

    @Resource
    private ManagedExecutorService executorService;

    private VersionParser versionParser = new VersionParser(VersionParser.DEFAULT_SUFFIX);

    /**
     * Sets the suffix that distinguish product artifacts in the repository.
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
    public CompletableFuture<Set<ProductArtifacts>> getArtifacts(GA ga) {
        return supplyAsync(() -> getArtifacts0(ga));
    }

    @Override
    public CompletableFuture<Set<ProductArtifacts>> getArtifactsFromRepository(GA ga,
            String repository) {
        return supplyAsync(() -> getArtifacts0(ga, repository));
    }

    @Override
    public CompletableFuture<Set<ProductArtifacts>> getArtifacts(GA ga, ProductSupportStatus status) {
        if (status != UNKNOWN) {
            return CompletableFuture.completedFuture(Collections.emptySet());
        }
        return supplyAsync(() -> getArtifacts0(ga));
    }

    @Override
    public CompletableFuture<Map<Product, Set<String>>> getVersions(GA ga) {
        CompletableFuture<Set<String>> versions = supplyAsync(
                () -> getVersionsStream(ga).collect(Collectors.toSet()));
        return versions.thenApply(rv -> Collections.singletonMap(Product.UNKNOWN,rv));
    }

    private Set<ProductArtifacts> getArtifacts0(GA ga) {
        Set<Artifact> allArtifacts = getVersionsStream(ga)
                .map(x -> new GAV(ga, x))
                .map(Artifact::new)
                .collect(Collectors.toSet());
        if (allArtifacts.isEmpty()) {
            return Collections.emptySet();
        }
        return Collections.singleton(new ProductArtifacts(Product.UNKNOWN, allArtifacts));
    }

    private Set<ProductArtifacts> getArtifacts0(GA ga, String repository) {
        Set<Artifact> allArtifacts = getVersionsStream(ga, repository)
                .map(x -> new GAV(ga, x))
                .map(Artifact::new)
                .collect(Collectors.toSet());
        if (allArtifacts.isEmpty()) {
            return Collections.emptySet();
        }
        return Collections.singleton(new ProductArtifacts(Product.UNKNOWN, allArtifacts));
    }

    private Stream<String> getVersionsStream(GA ga) {
        if (!ga.isValid()) {
            log.warn("Received nonvalid GA: " + ga);
            return Stream.empty();
        }
        try {
            return aproxConnector.getVersionsOfGA(ga).stream()
                    .filter(v -> versionParser.parse(v).isSuffixed())
                    .distinct();
        } catch (CommunicationException ex) {
            throw new ProductException(ex);
        }
    }

    private Stream<String> getVersionsStream(GA ga, String repository) {
        if (!ga.isValid()) {
            log.warn("Received nonvalid GA: " + ga);
            return Stream.empty();
        }
        try {
            return aproxConnector.getVersionsOfGA(ga, repository).stream()
                    .filter(v -> versionParser.parse(v).isSuffixed())
                    .distinct();
        } catch (CommunicationException ex) {
            throw new ProductException(ex);
        }
    }

    @Qualifier
    @Retention(RUNTIME)
    @Target({ TYPE, METHOD, FIELD, PARAMETER })
    public static @interface Repository {
    }
}
