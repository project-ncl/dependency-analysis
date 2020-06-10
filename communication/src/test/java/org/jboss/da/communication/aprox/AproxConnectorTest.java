package org.jboss.da.communication.aprox;

import org.jboss.da.common.CommunicationException;
import org.jboss.da.common.json.DAConfig;
import org.jboss.da.common.json.GlobalConfig;
import org.jboss.da.common.util.Configuration;
import org.jboss.da.common.util.ConfigurationParseException;
import org.jboss.da.common.util.UserLog;
import org.jboss.da.communication.aprox.impl.AproxConnectorImpl;
import org.jboss.da.communication.aprox.impl.MetadataFileParser;
import org.jboss.da.communication.pom.api.PomAnalyzer;
import org.jboss.da.model.rest.GA;
import org.jboss.pnc.pncmetrics.MetricsConfiguration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.Mockito.when;

import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@RunWith(MockitoJUnitRunner.class)
public class AproxConnectorTest {

    @Rule
    public WireMockRule wireMockRule = (new WireMockRule(8082));

    @Mock
    private Logger log;

    @Mock
    @UserLog
    private Logger userLog;

    private final Configuration config = initConfig();

    @Mock
    private PomAnalyzer pomAnalyzer;

    @Mock
    private MetricsConfiguration metricsConfiguration;

    @Spy
    private ObjectMapper mapper = new ObjectMapper();

    @Spy
    @InjectMocks
    private MetadataFileParser parser = new MetadataFileParser();

    @InjectMocks
    private final AproxConnectorImpl aproxConnector = new AproxConnectorImpl(config);

    private static final String REDHAT3 = "1.9.13.redhat-3";

    private static final String REDHAT2 = "1.9.13.redhat-2";

    private static final String REDHAT5 = "1.9.9.redhat-5";

    private static final GA GA = new GA("foo.bar", "baz");

    private static final String FOOBAR_MAVEN_METADATA = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<metadata>\n"
            + "  <groupId>foo.bar</groupId>\n" + "  <artifactId>baz</artifactId>\n" + "  <versioning>\n"
            + "    <latest>1.9.13.redhat-3</latest>\n" + "    <release>1.9.13.redhat-3</release>\n" + "    <versions>\n"
            + "      <version>1.9.9.redhat-5</version>\n" + "      <version>1.9.13.redhat-2</version>\n"
            + "      <version>1.9.13.redhat-3</version>\n" + "    </versions>\n"
            + "    <lastUpdated>20171203034828</lastUpdated>\n" + "  </versioning>\n" + "</metadata>";

    private static Configuration initConfig() {
        GlobalConfig globalCfg = new GlobalConfig();
        globalCfg.setIndyUrl("http://localhost:8082");

        DAConfig cfg = new DAConfig();
        cfg.setIndyGroup("DA-TEST-GROUP");
        cfg.setIndyGroupPublic("DA-PUBLIC-TEST-GROUP");
        cfg.setIndyRequestTimeout(30000);
        Configuration config = Mockito.mock(Configuration.class);
        try {
            when(config.getGlobalConfig()).thenReturn(globalCfg);
            when(config.getConfig()).thenReturn(cfg);
        } catch (ConfigurationParseException ex) {
            throw new RuntimeException(ex);
        }
        return config;
    }

    @Test
    public void testGetVersionsOfGA() throws ConfigurationParseException, CommunicationException {
        stubFor(
                get(urlEqualTo("/api/content/maven/group/DA-TEST-GROUP/foo/bar/baz/maven-metadata.xml")).willReturn(
                        aResponse().withStatus(200)
                                .withHeader("Content-Type", "text/xml")
                                .withBody(FOOBAR_MAVEN_METADATA)));

        List<String> versionsOfGA = aproxConnector.getVersionsOfGA(GA);

        // verify
        assertTrue(
                "Unmatched requests: " + WireMock.findUnmatchedRequests(),
                WireMock.findUnmatchedRequests().isEmpty());
        assertEquals(3, versionsOfGA.size());
        assertTrue(versionsOfGA.contains(REDHAT5));
        assertTrue(versionsOfGA.contains(REDHAT2));
        assertTrue(versionsOfGA.contains(REDHAT3));
    }

    @Test
    public void testGetVersionsOfGASpecificRepository() throws ConfigurationParseException, CommunicationException {
        stubFor(
                get(urlEqualTo("/api/content/maven/group/DA-TEST-GROUP2/foo/bar/baz/maven-metadata.xml")).willReturn(
                        aResponse().withStatus(200)
                                .withHeader("Content-Type", "text/xml")
                                .withBody(FOOBAR_MAVEN_METADATA)));

        List<String> versionsOfGA = aproxConnector.getVersionsOfGA(GA, "DA-TEST-GROUP2");

        // verify
        assertTrue(
                "Unmatched requests: " + WireMock.findUnmatchedRequests(),
                WireMock.findUnmatchedRequests().isEmpty());
        assertEquals(3, versionsOfGA.size());
        assertTrue(versionsOfGA.contains(REDHAT5));
        assertTrue(versionsOfGA.contains(REDHAT2));
        assertTrue(versionsOfGA.contains(REDHAT3));
    }

    @Test
    public void testGetVersionsOfNpm() throws ConfigurationParseException, CommunicationException {
        stubFor(
                get(urlEqualTo("/api/content/npm/group/DA-TEST-GROUP/jquery/package.json")).willReturn(
                        aResponse().withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBodyFile("jquery-package.json")));

        List<String> versionsOfGA = aproxConnector.getVersionsOfNpm("jquery");

        // verify
        assertTrue(
                "Unmatched requests: " + WireMock.findUnmatchedRequests(),
                WireMock.findUnmatchedRequests().isEmpty());
        assertEquals(9, versionsOfGA.size());
        assertTrue(versionsOfGA.contains("1.12.1"));
        assertTrue(versionsOfGA.contains("1.5.1"));
        assertTrue(versionsOfGA.contains("1.6.2"));
        assertTrue(versionsOfGA.contains("2.2.3"));
        assertTrue(versionsOfGA.contains("3.0.0"));
        assertTrue(versionsOfGA.contains("3.0.0-alpha1"));
        assertTrue(versionsOfGA.contains("3.0.0-beta1"));
        assertTrue(versionsOfGA.contains("3.0.0-rc1"));
        assertTrue(versionsOfGA.contains("3.1.0"));
    }
}
