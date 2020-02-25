package org.jboss.da.products.impl;

import org.jboss.da.listings.api.dao.ProductVersionDAO;
import org.jboss.da.listings.api.model.ProductVersion;
import org.jboss.da.listings.api.model.ProductVersionArtifactRelationship;
import org.jboss.da.listings.api.model.WhiteArtifact;
import org.jboss.da.listings.model.ProductSupportStatus;
import org.jboss.da.model.rest.GA;
import org.jboss.da.model.rest.GAV;
import org.jboss.da.products.api.Artifact;
import org.jboss.da.products.api.ArtifactType;
import org.jboss.da.products.api.MavenArtifact;
import org.jboss.da.products.api.Product;
import org.jboss.da.products.api.ProductArtifacts;
import org.jboss.da.products.api.ProductProvider;
import org.jboss.da.products.impl.DatabaseProductProvider.Database;

import javax.inject.Inject;
import javax.inject.Qualifier;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Database
public class DatabaseProductProvider implements ProductProvider {

    @Inject
    private ProductVersionDAO productVersionDAO;

    @Override
    public CompletableFuture<Set<Product>> getAllProducts() {
        return getProductsAsync(() -> productVersionDAO.findAll());
    }

    @Override
    public CompletableFuture<Set<Product>> getProductsByName(String name) {
        return getProductsAsync(() -> productVersionDAO.findProductVersionsWithProduct(name));
    }

    @Override
    public CompletableFuture<Set<Product>> getProductsByStatus(ProductSupportStatus status) {
        return getProductsAsync(() -> productVersionDAO.findProductVersionsWithArtifactsByStatus(status));
    }

    @Override
    public CompletableFuture<Set<Artifact>> getArtifacts(Product product) {
        return CompletableFuture.supplyAsync(() -> _getArtifacts(product));
    }

    private Set<Artifact> _getArtifacts(Product product) {
        return productVersionDAO.findProductVersion(product.getName(), product.getVersion())
                .map(ProductVersion::getWhiteArtifacts)
                .orElseGet(Collections::emptySet)
                .stream()
                .map(DatabaseProductProvider::toArtifact)
                .collect(Collectors.toSet());
    }

    @Override
    public CompletableFuture<Set<ProductArtifacts>> getArtifacts(Artifact artifact) {
        if (artifact.getType() != ArtifactType.MAVEN) {
            return CompletableFuture.completedFuture(Collections.emptySet());
        }
        GA ga = ((MavenArtifact) artifact).getGav().getGA();
        return CompletableFuture.supplyAsync(() -> getArtifacts(ga.getGroupId(), ga.getArtifactId(), Optional.empty()));
    }

    @Override
    public CompletableFuture<Set<ProductArtifacts>> getArtifacts(Artifact artifact, ProductSupportStatus status) {
        if (artifact.getType() != ArtifactType.MAVEN) {
            return CompletableFuture.completedFuture(Collections.emptySet());
        }
        GA ga = ((MavenArtifact) artifact).getGav().getGA();
        return CompletableFuture
                .supplyAsync(() -> getArtifacts(ga.getGroupId(), ga.getArtifactId(), Optional.of(status)));
    }

    @Override
    public CompletableFuture<Map<Product, Set<String>>> getVersions(Artifact artifact) {
        if (artifact.getType() != ArtifactType.MAVEN) {
            return CompletableFuture.completedFuture(Collections.emptyMap());
        }
        GA ga = ((MavenArtifact) artifact).getGav().getGA();
        return CompletableFuture.supplyAsync(
                () -> getArtifacts(ga.getGroupId(), ga.getArtifactId(), Optional.empty()).stream()
                        .collect(
                                Collectors.toMap(
                                        ProductArtifacts::getProduct,
                                        x -> x.getArtifacts()
                                                .stream()
                                                .map(Artifact::getVersion)
                                                .collect(Collectors.toSet()))));
    }

    private Set<ProductArtifacts> getArtifacts(
            final String groupId,
            final String artifactId,
            final Optional<ProductSupportStatus> st) {
        return productVersionDAO.findProductVersionsWithArtifactsByGAStatus(groupId, artifactId, st)
                .stream()
                .map(DatabaseProductProvider::toProductArtifacts)
                .collect(Collectors.toSet());
    }

    private CompletableFuture<Set<Product>> getProductsAsync(
            final Supplier<Collection<ProductVersion>> productsSupplier) {
        return CompletableFuture.supplyAsync(productsSupplier)
                .thenApply(pvs -> pvs.stream().map(DatabaseProductProvider::toProduct).collect(Collectors.toSet()));
    }

    private static Product toProduct(ProductVersion p) {
        return new Product(p.getProduct().getName(), p.getProductVersion(), p.getSupport());
    }

    private static ProductArtifacts toProductArtifacts(ProductVersionArtifactRelationship pvar) {
        return new ProductArtifacts(
                toProduct(pvar.getProductVersion()),
                Collections.singleton(toArtifact(pvar.getArtifact())));
    }

    private static Artifact toArtifact(WhiteArtifact a) {
        final org.jboss.da.listings.api.model.GA ga = a.getGa();
        return new MavenArtifact(new GAV(a.getGa().getGroupId(), ga.getArtifactId(), a.getVersion()));
    }

    @Qualifier
    @Retention(RUNTIME)
    @Target({ TYPE, METHOD, FIELD, PARAMETER })
    public static @interface Database {
    }
}
