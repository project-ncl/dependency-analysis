package org.jboss.da.reports.impl;

import org.jboss.da.common.CommunicationException;
import org.jboss.da.common.json.DAConfig;
import org.jboss.da.common.json.LookupMode;
import org.jboss.da.common.util.Configuration;
import org.jboss.da.common.util.ConfigurationParseException;
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
import org.jboss.da.reports.api.LookupGenerator;
import org.jboss.pnc.api.dependencyanalyzer.dto.QualifiedVersion;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.jboss.pnc.api.dependencyanalyzer.dto.QualifiedVersion.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LookupGeneratorTest {

    public static final String PERSISTENT = "persistent";

    private Configuration config;

    @Mock
    private RepositoryProductProvider repositoryProductProvider;

    @Mock
    private PncProductProvider pncProductProvider;

    @Mock
    private AggregatedProductProvider aggProductProvider;

    @Mock
    private BlackArtifactService blackArtifactService;

    @InjectMocks
    private LookupGenerator lookupGenerator;

    public LookupGeneratorTest() throws ConfigurationParseException {
        config = mock(Configuration.class);
        DAConfig daConfig = new DAConfig();
        LookupMode mode = new LookupMode();
        mode.setName(PERSISTENT);
        mode.setSuffixes(Arrays.asList("redhat"));
        daConfig.setModes(Collections.singletonList(mode));
        when(config.getConfig()).thenReturn(daConfig);
        lookupGenerator = new LookupGeneratorImpl(config);
    }

    @BeforeClass
    public static void initMocks() throws ConfigurationParseException {
    }

    @Test
    public void testMaven() throws CommunicationException {
        GAV gav1 = new GAV("foo.bar", "baz", "1.0.0");
        preparePnc(gav1.getGA(), Arrays.asList(of("1.0.0.redhat-1"), of("1.2.3.redhat-4"), of("1.0.0.redhat-2")));
        GAV gav2 = new GAV("foo.bar", "buz", "2.3.4");
        preparePnc(gav2.getGA(), Arrays.asList(of("1.0.0.redhat-1"), of("1.2.3.redhat-4"), of("1.0.0.redhat-2")));
        Set<GAV> gavs = new HashSet<>();
        gavs.add(gav1);
        gavs.add(gav2);

        Set<MavenLookupResult> results = lookupGenerator.lookupBestMatchMaven(gavs, PERSISTENT, false, Set.of());

        assertEquals(2, results.size());
        MavenLookupResult result1 = results.stream().filter(r -> gav1.equals(r.getGav())).findFirst().get();
        MavenLookupResult result2 = results.stream().filter(r -> gav2.equals(r.getGav())).findFirst().get();
        assertEquals("1.0.0.redhat-2", result1.getBestMatchVersion());
        assertNull(result2.getBestMatchVersion());
    }

    @Test
    public void testNpm() throws CommunicationException {
        NPMPackage gav1 = new NPMPackage("foo-bar", "1.0.0");
        preparePnc(gav1.getName(), Arrays.asList(of("1.0.0.redhat-1"), of("1.2.3.redhat-4"), of("1.0.0.redhat-2")));
        NPMPackage gav2 = new NPMPackage("foo-baz", "2.3.4");
        preparePnc(gav2.getName(), Arrays.asList(of("1.0.0.redhat-1"), of("1.2.3.redhat-4"), of("1.0.0.redhat-2")));
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

    private void preparePnc(GA ga, List<QualifiedVersion> versions) {
        when(pncProductProvider.getAllVersions(eq(new MavenArtifact(new GAV(ga, "0.0.0")))))
                .thenReturn(CompletableFuture.completedFuture(new HashSet<>(versions)));
    }

    private void preparePnc(String name, List<QualifiedVersion> versions) {
        when(pncProductProvider.getAllVersions(eq(new NPMArtifact(name, "0.0.0"))))
                .thenReturn(CompletableFuture.completedFuture(new HashSet<>(versions)));
    }
}
