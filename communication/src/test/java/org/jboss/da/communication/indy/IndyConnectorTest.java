package org.jboss.da.communication.indy;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;

import org.jboss.da.common.CommunicationException;
import org.jboss.da.common.json.DAConfig;
import org.jboss.da.common.json.GlobalConfig;
import org.jboss.da.common.logging.UserLog;
import org.jboss.da.common.util.Configuration;
import org.jboss.da.common.util.ConfigurationParseException;
import org.jboss.da.communication.indy.impl.IndyConnectorImpl;
import org.jboss.da.communication.indy.impl.MetadataFileParser;
import org.jboss.da.communication.pom.api.PomAnalyzer;
import org.jboss.da.model.rest.GA;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@ExtendWith(MockitoExtension.class)
@WireMockTest(httpPort = 8082)
public class IndyConnectorTest {

    @Mock
    private Logger log;

    @Mock
    @UserLog
    private Logger userLog;

    private final Configuration config = initConfig();

    @Mock
    private PomAnalyzer pomAnalyzer;

    @Spy
    private ObjectMapper mapper = new ObjectMapper();

    @InjectMocks
    private MetadataFileParser parser = Mockito.spy(MetadataFileParser.class);

    @InjectMocks
    private final IndyConnectorImpl indyConnector = new IndyConnectorImpl(config);

    private static final String REDHAT3 = "1.9.13.redhat-3";

    private static final String REDHAT2 = "1.9.13.redhat-2";

    private static final String REDHAT5 = "1.9.9.redhat-5";

    private static final GA GA = new GA("foo.bar", "baz");

    private static final String FOOBAR_MAVEN_METADATA = """
            <?xml version="1.0" encoding="UTF-8"?>
            <metadata>
              <groupId>foo.bar</groupId>
              <artifactId>baz</artifactId>
              <versioning>
                <latest>1.9.13.redhat-3</latest>
                <release>1.9.13.redhat-3</release>
                <versions>
                  <version>1.9.9.redhat-5</version>
                  <version>1.9.13.redhat-2</version>
                  <version>1.9.13.redhat-3</version>
                </versions>
                <lastUpdated>20171203034828</lastUpdated>
              </versioning>
            </metadata>""";

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
    public void testGetVersionsOfGA() throws CommunicationException {
        stubFor(
                get(urlEqualTo("/api/content/maven/group/DA-TEST-GROUP/foo/bar/baz/maven-metadata.xml")).willReturn(
                        aResponse().withStatus(200)
                                .withHeader("Content-Type", "text/xml")
                                .withBody(FOOBAR_MAVEN_METADATA)));

        List<String> versionsOfGA = indyConnector.getVersionsOfGA(GA);

        // verify
        assertTrue(
                WireMock.findUnmatchedRequests().isEmpty(),
                "Unmatched requests: " + WireMock.findUnmatchedRequests());
        assertEquals(3, versionsOfGA.size());
        assertTrue(versionsOfGA.contains(REDHAT5));
        assertTrue(versionsOfGA.contains(REDHAT2));
        assertTrue(versionsOfGA.contains(REDHAT3));
    }

    @Test
    public void testGetVersionsOfNpm() throws CommunicationException {
        stubFor(
                get(urlEqualTo("/api/content/npm/group/DA-TEST-GROUP/jquery/package.json")).willReturn(
                        aResponse().withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBodyFile("jquery-package.json")));

        List<String> versionsOfGA = indyConnector.getVersionsOfNpm("jquery");

        // verify
        assertTrue(
                WireMock.findUnmatchedRequests().isEmpty(),
                "Unmatched requests: " + WireMock.findUnmatchedRequests());
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
