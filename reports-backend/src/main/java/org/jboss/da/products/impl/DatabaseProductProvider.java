package org.jboss.da.products.impl;

import org.jboss.da.listings.api.dao.ProductVersionDAO;
import org.jboss.da.listings.api.model.ProductVersion;
import org.jboss.da.listings.api.model.ProductVersionArtifactRelationship;
import org.jboss.da.listings.api.model.WhiteArtifact;
import org.jboss.da.listings.model.ProductSupportStatus;
import org.jboss.da.model.rest.GA;
import org.jboss.da.model.rest.GAV;
import org.jboss.da.products.api.Artifact;
import org.jboss.da.products.api.Product;
import org.jboss.da.products.api.ProductArtifacts;
import org.jboss.da.products.api.ProductProvider;
import org.jboss.da.products.impl.DatabaseProductProvider.Database;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
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
import java.util.stream.Collectors;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Stateless
@Database
public class DatabaseProductProvider implements ProductProvider {

    @Inject
    private ProductVersionDAO productVersionDAO;

    @Override
    @Asynchronous
    public CompletableFuture<Set<Product>> getAllProducts() {
        return toProducts(productVersionDAO.findAll());
    }

    @Override
    @Asynchronous
    public CompletableFuture<Set<Product>> getProductsByName(String name) {
        return toProducts(productVersionDAO.findProductVersionsWithProduct(name));
    }

    @Override
    @Asynchronous
    public CompletableFuture<Set<Product>> getProductsByStatus(ProductSupportStatus status) {
        return toProducts(productVersionDAO.findProductVersionsWithArtifactsByStatus(status));
    }

    @Override
    @Asynchronous
    public CompletableFuture<Set<Artifact>> getArtifacts(Product product) {
        Set<Artifact> ret = productVersionDAO.findProductVersion(product.getName(), product.getVersion())
                .map(ProductVersion::getWhiteArtifacts)
                .orElseGet(Collections::emptySet).stream()
                .map(DatabaseProductProvider::toArtifact)
                .collect(Collectors.toSet());

        return CompletableFuture.completedFuture(ret);
    }

    @Override
    @Asynchronous
    public CompletableFuture<Set<ProductArtifacts>> getArtifacts(GA ga) {
        Set<ProductArtifacts> ret = getArtifacts(ga.getGroupId(), ga.getArtifactId(),
                Optional.empty());

        return CompletableFuture.completedFuture(ret);
    }

    @Override
    @Asynchronous
    public CompletableFuture<Set<ProductArtifacts>> getArtifacts(GA ga, ProductSupportStatus status) {
        Set<ProductArtifacts> ret = getArtifacts(ga.getGroupId(), ga.getArtifactId(),
                Optional.of(status));

        return CompletableFuture.completedFuture(ret);
    }

    @Override
    @Asynchronous
    public CompletableFuture<Map<Product, Set<String>>> getVersions(GA ga) {
        Map<Product, Set<String>> ret = getArtifacts(ga.getGroupId(), ga.getArtifactId(), Optional.empty())
                .stream()
                .collect(Collectors.toMap(ProductArtifacts::getProduct,
                        x -> x.getArtifacts().stream()
                                .map(y -> y.getGav().getVersion())
                                .collect(Collectors.toSet())));
        
        return CompletableFuture.completedFuture(ret);
    }

    private Set<ProductArtifacts> getArtifacts(final String groupId, final String artifactId, final Optional<ProductSupportStatus> st) {
        return productVersionDAO.findProductVersionsWithArtifactsByGAStatus(groupId, artifactId, st).stream()
                .map(DatabaseProductProvider::toProductArtifacts)
                .collect(Collectors.toSet());
    }

    private CompletableFuture<Set<Product>> toProducts(final Collection<ProductVersion> products) {
        return CompletableFuture.completedFuture(products.stream()
                .map(DatabaseProductProvider::toProduct)
                .collect(Collectors.toSet()));
    }

    private static Product toProduct(ProductVersion p) {
        return new Product(p.getProduct().getName(), p.getProductVersion(), p.getSupport());
    }

    private static ProductArtifacts toProductArtifacts(ProductVersionArtifactRelationship pvar) {
        return new ProductArtifacts(toProduct(pvar.getProductVersion()),
                Collections.singleton(toArtifact(pvar.getArtifact())));
    }

    private static Artifact toArtifact(WhiteArtifact a) {
        final org.jboss.da.listings.api.model.GA ga = a.getGa();
        return new Artifact(new GAV(a.getGa().getGroupId(), ga.getArtifactId(), a.getVersion()));
    }

    @Qualifier
    @Retention(RUNTIME)
    @Target({ TYPE, METHOD, FIELD, PARAMETER })
    public static @interface Database {
    }
}
