package org.jboss.da.communication.aprox;

import org.jboss.da.common.CommunicationException;
import org.jboss.da.common.json.DAConfig;
import org.jboss.da.common.util.Configuration;
import org.jboss.da.common.util.ConfigurationParseException;
import org.jboss.da.communication.aprox.impl.AproxConnectorImpl;
import org.jboss.da.communication.pom.api.PomAnalyzer;
import org.jboss.da.model.rest.GA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import java.util.List;
import java.util.logging.Level;

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

    private final Configuration config = initConfig();

    @Mock
    private PomAnalyzer pomAnalyzer;

    @InjectMocks
    private final AproxConnectorImpl dependencyTreeGenerator = new AproxConnectorImpl(config);

    private static final String REDHAT3 = "1.9.13.redhat-3";

    private static final String REDHAT2 = "1.9.13.redhat-2";

    private static final String REDHAT5 = "1.9.9.redhat-5";

    private static final GA GA = new GA("foo.bar", "baz");

    private static final String FOOBAR_MAVEN_METADATA = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<metadata>\n"
            + "  <groupId>foo.bar</groupId>\n"
            + "  <artifactId>baz</artifactId>\n"
            + "  <versioning>\n"
            + "    <latest>1.9.13.redhat-3</latest>\n"
            + "    <release>1.9.13.redhat-3</release>\n"
            + "    <versions>\n"
            + "      <version>1.9.9.redhat-5</version>\n"
            + "      <version>1.9.13.redhat-2</version>\n"
            + "      <version>1.9.13.redhat-3</version>\n"
            + "    </versions>\n"
            + "    <lastUpdated>20171203034828</lastUpdated>\n"
            + "  </versioning>\n"
            + "</metadata>";

    private static Configuration initConfig() {
        DAConfig cfg = new DAConfig();
        cfg.setAproxServer("http://localhost:8082");
        cfg.setAproxGroup("DA-TEST-GROUP");
        cfg.setAproxGroupPublic("DA-PUBLIC-TEST-GROUP");
        cfg.setAproxRequestTimeout(30000);
        Configuration config = Mockito.mock(Configuration.class);
        try {
            when(config.getConfig()).thenReturn(cfg);
        } catch (ConfigurationParseException ex) {
            throw new RuntimeException(ex);
        }
        return config;
    }

    @Test
    public void testGetVersionsOfGA() throws ConfigurationParseException, CommunicationException {
        stubFor(get(urlEqualTo("/api/group/DA-TEST-GROUP/foo/bar/baz/maven-metadata.xml"))
                .willReturn(
                        aResponse().withStatus(200).withHeader("Content-Type", "text/xml")
                                .withBody(FOOBAR_MAVEN_METADATA)));

        List<String> versionsOfGA = dependencyTreeGenerator.getVersionsOfGA(GA);

        //verify
        assertTrue("Unmatched requests: " + WireMock.findUnmatchedRequests(), WireMock
                .findUnmatchedRequests().isEmpty());
        assertEquals(3, versionsOfGA.size());
        assertTrue(versionsOfGA.contains(REDHAT5));
        assertTrue(versionsOfGA.contains(REDHAT2));
        assertTrue(versionsOfGA.contains(REDHAT3));
    }

    @Test
    public void testGetVersionsOfGASpecificRepository() throws ConfigurationParseException,
            CommunicationException {
        stubFor(get(urlEqualTo("/api/group/DA-TEST-GROUP2/foo/bar/baz/maven-metadata.xml"))
                .willReturn(
                        aResponse().withStatus(200).withHeader("Content-Type", "text/xml")
                                .withBody(FOOBAR_MAVEN_METADATA)));

        List<String> versionsOfGA = dependencyTreeGenerator.getVersionsOfGA(GA, "DA-TEST-GROUP2");

        //verify
        assertTrue("Unmatched requests: " + WireMock.findUnmatchedRequests(), WireMock
                .findUnmatchedRequests().isEmpty());
        assertEquals(3, versionsOfGA.size());
        assertTrue(versionsOfGA.contains(REDHAT5));
        assertTrue(versionsOfGA.contains(REDHAT2));
        assertTrue(versionsOfGA.contains(REDHAT3));
    }

}
