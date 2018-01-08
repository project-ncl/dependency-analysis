package org.jboss.da.products.impl;

import org.jboss.da.listings.model.ProductSupportStatus;
import org.jboss.da.model.rest.GA;
import org.jboss.da.products.api.Artifact;
import org.jboss.da.products.api.Product;
import org.jboss.da.products.api.ProductArtifacts;
import org.jboss.da.products.api.ProductProvider;
import org.jboss.da.products.impl.DatabaseProductProvider.Database;
import org.jboss.da.products.impl.RepositoryProductProvider.Repository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
//@RequestScoped // Should be Request scoped, but is Dependent until websockets support scopes
public class AggregatedProductProvider implements ProductProvider {

    public static Set<ProductArtifacts> filterArtifacts(Set<ProductArtifacts> artifacts,
            Predicate<? super Artifact> predicate) {
        return artifacts.stream()
                .map(a -> new ProductArtifacts(a.getProduct(),
                                a.getArtifacts().stream()
                                .filter(predicate)
                                .collect(Collectors.toSet())))
                .filter(a -> !a.getArtifacts().isEmpty())
                .collect(Collectors.toSet());
    }

    public static CompletableFuture<Set<ProductArtifacts>> filterProducts(
            CompletableFuture<Set<ProductArtifacts>> artifacts,
            Predicate<? super Product> predicate) {
        return artifacts.thenApply(as -> as.stream()
                .filter(a -> predicate.test(a.getProduct()))
                .collect(Collectors.toSet()));
    }

    // TODO: filter unknown products, so that when there is artifact both in unknown product and in
    // known product, it's removed from the unknown

    @Inject
    @Database
    ProductProvider databaseProductProvider;

    @Inject
    @Repository
    RepositoryProductProvider repositoryProductProvider;

    @Override
    public CompletableFuture<Set<Product>> getAllProducts() {
        return aggregate(ProductProvider::getAllProducts, new SetCollector<>());
    }

    @Override
    public CompletableFuture<Set<Product>> getProductsByName(final String name) {
        return aggregate(x -> x.getProductsByName(name), new SetCollector<>());
    }

    @Override
    public CompletableFuture<Set<Product>> getProductsByStatus(final ProductSupportStatus status) {
        return aggregate(x -> x.getProductsByStatus(status), new SetCollector<>());
    }

    @Override
    public CompletableFuture<Set<Artifact>> getArtifacts(Product product) {
        return aggregate(x -> x.getArtifacts(product), new SetCollector<>());
    }

    @Override
    public CompletableFuture<Set<ProductArtifacts>> getArtifacts(GA ga) {
        return aggregate(x -> x.getArtifacts(ga), new ProductArtifactsCollector());
    }

    @Override
    public CompletableFuture<Set<ProductArtifacts>> getArtifactsFromRepository(GA ga, String repository) {
        return aggregate(x -> x.getArtifactsFromRepository(ga, repository), new ProductArtifactsCollector());
    }

    @Override
    public CompletableFuture<Set<ProductArtifacts>> getArtifacts(GA ga, ProductSupportStatus status) {
        return aggregate(x -> x.getArtifacts(ga, status), new ProductArtifactsCollector());
    }

    @Override
    public CompletableFuture<Map<Product, Set<String>>> getVersions(GA ga) {
        return aggregate(x -> x.getVersions(ga), new MapCol<>(AggregatedProductProvider::combineSets));
    }

    public void setVersionSuffix(String suffix) {
        repositoryProductProvider.setVersionSuffix(suffix);
    }

    private <R> CompletableFuture<R> aggregate(Function<ProductProvider, Future<R>> getter, Collector<? super R, ?, R> collector){
        final List<Future<R>> results = new ArrayList<>();

        results.add(getter.apply(databaseProductProvider));
        results.add(getter.apply(repositoryProductProvider));

        CompletableFuture<R> ret = new CompletableFuture<>();
        // After all are completed, accumulate the results together
        CompletableFuture.runAsync(() -> {
            try {
                List<R> resultList = new ArrayList<>();
                for (Future<R> r : results) {
                    resultList.add(r.get());
                }
                ret.complete(resultList.stream().collect(collector));
            } catch (ExecutionException | RuntimeException ex) {
                ret.completeExceptionally(ex);
            } catch (InterruptedException ex) {
                ret.completeExceptionally(ex);
                Thread.currentThread().interrupt();
            }
        });

        return ret;
    }

    private static <R> Set<R> combineSets(Set<R> x, Set<R> y) {
        Set<R> r = new HashSet<>(x);
        r.addAll(y);
        return r;
    }

    private static class ProductArtifactsCollector
            implements
            Collector<Set<ProductArtifacts>, HashMap<Product, ProductArtifacts>, Set<ProductArtifacts>> {

        @Override
        public Supplier<HashMap<Product, ProductArtifacts>> supplier() {
            return () -> new HashMap<>();
        }

        @Override
        public BiConsumer<HashMap<Product, ProductArtifacts>, Set<ProductArtifacts>> accumulator() {
            return (h, m) -> {
                for(ProductArtifacts pa : m){
                    h.merge(pa.getProduct(), pa, (a, b) -> new ProductArtifacts(pa.getProduct(), combineSets(a.getArtifacts(), b.getArtifacts())));
                }
            };
        }

        @Override
        public BinaryOperator<HashMap<Product, ProductArtifacts>> combiner() {
            return (h, m) -> {
                for(Map.Entry<Product, ProductArtifacts> e : m.entrySet()){
                    h.merge(e.getKey(), e.getValue(), (a, b) -> new ProductArtifacts(e.getKey(), combineSets(a.getArtifacts(), b.getArtifacts())));
                }
                return h;
            };
        }

        @Override
        public Function<HashMap<Product, ProductArtifacts>, Set<ProductArtifacts>> finisher() {
            return m -> new HashSet<>(m.values());
        }

        @Override
        public Set<Characteristics> characteristics() {
            return EnumSet.of(Collector.Characteristics.UNORDERED);
        }
    }

    private static abstract class AbstractColector<R> implements Collector<R, R, R> {

        @Override
        public BinaryOperator<R> combiner() {
            return (h, s) -> {accumulator().accept(h, s); return h;};
        }

        @Override
        public Function<R, R> finisher() {
            return Function.identity();
        }

        @Override
        public Set<Collector.Characteristics> characteristics() {
            return EnumSet.of(Collector.Characteristics.IDENTITY_FINISH,
                    Collector.Characteristics.UNORDERED);
        }

    }

    private static class MapCol<R, S> extends AbstractColector<Map<R, S>> {

        private final BiFunction<? super S, ? super S, ? extends S> remappingFunction;

        public MapCol(BiFunction<? super S, ? super S, ? extends S> remappingFunction) {
            this.remappingFunction = remappingFunction;
        }

        @Override
        public Supplier<Map<R, S>> supplier() {
            return () -> new HashMap<>();
        }

        @Override
        public BiConsumer<Map<R, S>, Map<R, S>> accumulator() {
            return (h, m) -> {
                for (Map.Entry<R, S> e : m.entrySet()) {
                    h.merge(e.getKey(), e.getValue(), remappingFunction);
                }
            };
        }
    }

    private static class SetCollector<R> extends AbstractColector<Set<R>> {

        @Override
        public Supplier<Set<R>> supplier() {
            return () -> new HashSet<>();
        }

        @Override
        public BiConsumer<Set<R>, Set<R>> accumulator() {
            return (h, s) -> h.addAll(s);
        }
    }
}
