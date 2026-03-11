package org.jboss.da.reports.impl;

import org.jboss.da.common.CommunicationException;
import org.jboss.da.common.json.DAConfig;
import org.jboss.da.common.json.LookupMode;
import org.jboss.da.common.util.Configuration;
import org.jboss.da.common.util.ConfigurationParseException;
import org.jboss.da.common.logging.UserLog;
import org.jboss.da.communication.indy.api.IndyConnector;
import org.jboss.da.communication.indy.model.GAVDependencyTree;
import org.jboss.da.listings.api.service.BlackArtifactService;
import org.jboss.da.listings.model.ProductSupportStatus;
import org.jboss.da.model.rest.GA;
import org.jboss.da.model.rest.GAV;
import org.jboss.da.products.api.Artifact;
import org.jboss.da.products.api.ArtifactType;
import org.jboss.da.products.api.MavenArtifact;
import org.jboss.da.products.api.Product;
import org.jboss.da.products.api.ProductArtifacts;
import org.jboss.da.products.impl.AggregatedProductProvider;
import org.jboss.da.products.impl.PncProductProvider;
import org.jboss.da.products.impl.RepositoryProductProvider;
import org.jboss.da.reports.backend.api.DependencyTreeGenerator;
import org.jboss.da.reports.backend.impl.DependencyTreeGeneratorImpl;
import org.jboss.da.reports.model.request.LookupGAVsRequest;
import org.jboss.da.reports.model.response.LookupReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@ExtendWith(MockitoExtension.class)
public class ReportsGeneratorImplTest {

    @Mock
    private IndyConnector indyClient;

    @Mock
    private BlackArtifactService blackArtifactService;

    @Mock
    private AggregatedProductProvider productProvider;

    @Mock
    private RepositoryProductProvider repoProductProvider;

    @Mock
    private PncProductProvider pncProductProvider;

    private Configuration config;

    @Mock
    @UserLog
    private Logger userLog;

    @InjectMocks
    private final DependencyTreeGenerator dependencyTreeGenerator = Mockito.spy(DependencyTreeGeneratorImpl.class);

    @InjectMocks
    private ReportsGeneratorImpl generator;

    private final GAV daGAV = new GAV("org.jboss", "dependency-analysis", "1.0.1");

    private final String version = "0.1.0";

    private final GAV daCoreGAV = new GAV("org.jboss.da", "core", version);

    private final String bestMatchVersion = version + ".redhat-1";

    private final List<String> daCoreVersionsBest = Arrays
            .asList("1.1.1.redhat-2", "1.2.3.redhat-1", "1.3.4.redhat-3", "1.3.5.redhat-1", bestMatchVersion);

    private final GAVDependencyTree daCoreNoDT = new GAVDependencyTree(daCoreGAV, new HashSet<>());

    private final GAV daUtilGAV = new GAV("org.jboss.da", "util", version);

    private final GAV daCommonGAV = new GAV("org.jboss.da", "common", version);

    private final GAVDependencyTree daUtilDT = new GAVDependencyTree(daUtilGAV, new HashSet<>());

    private final GAVDependencyTree daCommonDT = new GAVDependencyTree(daCommonGAV, new HashSet<>());

    private final GAVDependencyTree daCoreDT = new GAVDependencyTree(
            daCoreGAV,
            new HashSet<>(Arrays.asList(daUtilDT, daCommonDT)));

    private final Product productEAP = new Product("EAP", "7.0", ProductSupportStatus.UNKNOWN);

    public ReportsGeneratorImplTest() throws ConfigurationParseException {
        config = mock(Configuration.class);
        DAConfig daConfig = new DAConfig();
        LookupMode mode = new LookupMode();
        mode.setName("PERSISTENT");
        mode.setSuffixes(List.of("redhat"));
        daConfig.setModes(Collections.singletonList(mode));
        when(config.getConfig()).thenReturn(daConfig);
        generator = new ReportsGeneratorImpl(config);
    }

    @BeforeEach
    public void initMock() throws ReflectiveOperationException {
        injectMethod("userLog", generator, userLog, ReportsGeneratorImpl.class);
    }

    private void injectMethod(String fieldName, Object to, Object what, Class clazz)
            throws NoSuchFieldException, IllegalAccessException {
        Field f = clazz.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(to, what);
    }

    private void prepareProductProvider(List<String> versions, List<Product> whitelisted, GAV gav) {
        final Set<Artifact> artifacts = versions.stream()
                .map(v -> new MavenArtifact(new GAV(gav.getGA(), v)))
                .collect(Collectors.toSet());

        Set<ProductArtifacts> prodArts = new HashSet<>();
        prodArts.add(new ProductArtifacts(Product.UNKNOWN, artifacts));
        for (Product w : whitelisted) {
            prodArts.add(new ProductArtifacts(w, artifacts));
        }

        when(productProvider.getArtifacts(matchingGAV(gav))).thenReturn(CompletableFuture.completedFuture(prodArts));
    }

    private Set<ProductArtifacts> toProductArtifacts(GA ga, List<String> versions) {
        Set<Artifact> artifacts = versions.stream()
                .map(v -> new MavenArtifact(new GAV(ga, v)))
                .collect(Collectors.toSet());
        return Collections.singleton(new ProductArtifacts(Product.UNKNOWN, artifacts));
    }

    private void prepare(List<Product> whitelisted, List<String> versions) {
        lenient().when(productProvider.getArtifacts(matchingGAV(daCoreGAV)))
                .thenReturn(CompletableFuture.completedFuture(toProductArtifacts(daCoreGAV.getGA(), versions)));

        prepareProductProvider(versions, whitelisted, daCoreGAV);
        lenient().when(blackArtifactService.isArtifactPresent(daCoreGAV)).thenReturn(true);

        DAConfig daConfig = new DAConfig();
        daConfig.setIndyGroup("DA");
        daConfig.setIndyGroup("DA-temporary-builds");
        try {
            when(config.getConfig()).thenReturn(daConfig);
        } catch (ConfigurationParseException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Test the distinct on a stream in #getLookupReportsForGavs works correctly
     */
    @Test
    public void testDistinctOnGavsStream() {
        // Given
        List<GAV> gavs = new ArrayList<>();
        gavs.add(new GAV("org", "test", "1.0"));
        gavs.add(new GAV("org", "test", "1.1"));
        gavs.add(new GAV("org", "test", "1.0"));

        gavs.add(new GAV("org2", "test2", "2.0"));
        gavs.add(new GAV("org2", "test2", "2.2"));
        gavs.add(new GAV("org2", "test2", "2.0"));
        LookupGAVsRequest request = new LookupGAVsRequest(gavs);

        // When
        List<GAV> uniqueGAVs = new ArrayList<>();
        uniqueGAVs.add(new GAV("org", "test", "1.0"));
        uniqueGAVs.add(new GAV("org", "test", "1.1"));
        uniqueGAVs.add(new GAV("org2", "test2", "2.0"));
        uniqueGAVs.add(new GAV("org2", "test2", "2.2"));

        List<GAV> distinctList = request.getGavs().stream().distinct().collect(Collectors.toList());

        // Then
        assertEquals(uniqueGAVs.size(), distinctList.size());
        assertEquals(uniqueGAVs, distinctList);
    }

    @Test
    public void testBlacklistedLookupReport() throws CommunicationException {
        prepare(Collections.emptyList(), daCoreVersionsBest);
        LookupGAVsRequest lgr = new LookupGAVsRequest(
                Collections.emptySet(),
                Collections.emptySet(),
                null,
                true,
                "PERSISTENT",
                null,
                Collections.singletonList(daCoreGAV));

        List<LookupReport> lookupReportsForGavs = generator.getLookupReportsForGavs(lgr);

        assertNotNull(lookupReportsForGavs);
        assertEquals(1, lookupReportsForGavs.size());
        LookupReport lookupReport = lookupReportsForGavs.get(0);
        assertTrue(lookupReport.isBlacklisted());
    }

    private static Artifact matchingGAV(GAV gav) {
        return argThat(new IsArtifactWithSameNameAndTypeAs(new MavenArtifact(gav)));
    }

    private static class IsArtifactWithSameNameAndTypeAs implements ArgumentMatcher<Artifact> {

        private final String name;

        private final ArtifactType type;

        public IsArtifactWithSameNameAndTypeAs(Artifact artifact) {
            this.name = Objects.requireNonNull(artifact.getName());
            this.type = Objects.requireNonNull(artifact.getType());
        }

        @Override
        public boolean matches(Artifact argument) {
            if (argument != null) {
                return name.equals(argument.getName()) && type == argument.getType();
            }
            return false;
        }
    }
}
