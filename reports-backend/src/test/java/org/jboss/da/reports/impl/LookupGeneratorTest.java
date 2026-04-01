package org.jboss.da.reports.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.jboss.da.common.CommunicationException;
import org.jboss.da.common.config.Configuration;
import org.jboss.da.listings.api.service.BlackArtifactService;
import org.jboss.da.lookup.model.MavenLookupResult;
import org.jboss.da.lookup.model.NPMLookupResult;
import org.jboss.da.model.rest.GA;
import org.jboss.da.model.rest.GAV;
import org.jboss.da.model.rest.NPMPackage;
import org.jboss.da.products.api.MavenArtifact;
import org.jboss.da.products.api.NPMArtifact;
import org.jboss.da.products.impl.AggregatedProductProvider;
import org.jboss.da.products.impl.PncProductProvider;
import org.jboss.da.products.impl.RepositoryProductProvider;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.BuildCategory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LookupGeneratorTest {

    public static final String PERSISTENT = "persistent";

    @Mock
    private Configuration configuration;

    @Mock
    private RepositoryProductProvider repositoryProductProvider;

    @Mock
    private PncProductProvider pncProductProvider;

    @Mock
    private AggregatedProductProvider aggProductProvider;

    @Mock
    private BlackArtifactService blackArtifactService;

    @InjectMocks
    private LookupGeneratorImpl lookupGenerator;

    private AutoCloseable object;

    @BeforeEach
    void setUp() {
        Configuration.LookupMode mode = mock(Configuration.LookupMode.class);
        when(mode.name()).thenReturn(PERSISTENT);
        when(mode.suffixes()).thenReturn(List.of("redhat"));
        when(mode.incrementSuffix()).thenReturn("redhat");
        when(mode.buildCategories()).thenReturn(List.of(BuildCategory.STANDARD));
        when(mode.artifactQualities())
                .thenReturn(
                        Arrays.asList(
                                ArtifactQuality.NEW,
                                ArtifactQuality.VERIFIED,
                                ArtifactQuality.TESTED));
        when(configuration.lookupModes()).thenReturn(Collections.singletonList(mode));

        lookupGenerator = new LookupGeneratorImpl(configuration);
        object = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void finish() throws Exception {
        object.close();
    }

    @Test
    public void testMaven() throws CommunicationException {
        GAV gav1 = new GAV("foo.bar", "baz", "1.0.0");
        preparePnc(gav1.getGA(), Arrays.asList("1.0.0.redhat-1", "1.2.3.redhat-4", "1.0.0.redhat-2"));
        GAV gav2 = new GAV("foo.bar", "buz", "2.3.4");
        preparePnc(gav2.getGA(), Arrays.asList("1.0.0.redhat-1", "1.2.3.redhat-4", "1.0.0.redhat-2"));
        Set<GAV> gavs = new HashSet<>();
        gavs.add(gav1);
        gavs.add(gav2);

        Set<MavenLookupResult> results = lookupGenerator.lookupBestMatchMaven(gavs, PERSISTENT, false);

        assertEquals(2, results.size());
        MavenLookupResult result1 = results.stream().filter(r -> gav1.equals(r.getGav())).findFirst().get();
        MavenLookupResult result2 = results.stream().filter(r -> gav2.equals(r.getGav())).findFirst().get();
        assertEquals("1.0.0.redhat-2", result1.getBestMatchVersion());
        assertNull(result2.getBestMatchVersion());
    }

    @Test
    public void testNpm() throws CommunicationException {
        NPMPackage gav1 = new NPMPackage("foo-bar", "1.0.0");
        preparePnc(gav1.getName(), Arrays.asList("1.0.0.redhat-1", "1.2.3.redhat-4", "1.0.0.redhat-2"));
        NPMPackage gav2 = new NPMPackage("foo-baz", "2.3.4");
        preparePnc(gav2.getName(), Arrays.asList("1.0.0.redhat-1", "1.2.3.redhat-4", "1.0.0.redhat-2"));
        Set<NPMPackage> gavs = new HashSet<>();
        gavs.add(gav1);
        gavs.add(gav2);

        Set<NPMLookupResult> results = lookupGenerator.lookupBestMatchNPM(gavs, PERSISTENT);

        assertEquals(2, results.size());
        NPMLookupResult result1 = results.stream().filter(r -> gav1.equals(r.getNpmPackage())).findFirst().get();
        NPMLookupResult result2 = results.stream().filter(r -> gav2.equals(r.getNpmPackage())).findFirst().get();
        assertEquals("1.0.0.redhat-2", result1.getBestMatchVersion());
        assertNull(result2.getBestMatchVersion());
    }

    private void preparePnc(GA ga, List<String> versions) {
        when(pncProductProvider.getAllVersions(eq(new MavenArtifact(new GAV(ga, "0.0.0")))))
                .thenReturn(CompletableFuture.completedFuture(new HashSet<>(versions)));
    }

    private void preparePnc(String name, List<String> versions) {
        when(pncProductProvider.getAllVersions(eq(new NPMArtifact(name, "0.0.0"))))
                .thenReturn(CompletableFuture.completedFuture(new HashSet<>(versions)));
    }
}
