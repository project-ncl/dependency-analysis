package org.jboss.da.communication.artifactory;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;

import java.net.URI;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jboss.da.common.CommunicationException;
import org.jboss.da.common.config.Configuration;
import org.jboss.da.common.logging.UserLog;
import org.jboss.da.communication.pom.api.PomAnalyzer;
import org.jboss.da.communication.repository.MetadataFileParser;
import org.jboss.da.communication.rt.ArtifactoryConnector;
import org.jboss.da.model.rest.GA;
import org.jboss.pnc.api.enums.RepositoryType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;

@ExtendWith(MockitoExtension.class)
@WireMockTest(httpPort = 8082)
public class ArtifactoryConnectorTest {

    @Mock
    private Logger log;

    @Mock
    @UserLog
    private Logger userLog;

    @Mock
    private Configuration.Artifactory rtConfig;

    @Mock
    private Configuration daConfiguration;

    @Mock
    private PomAnalyzer pomAnalyzer;

    @Spy
    private ObjectMapper mapper = new ObjectMapper();

    @InjectMocks
    private MetadataFileParser parser = Mockito.spy(MetadataFileParser.class);

    private ArtifactoryConnector artifactoryConnector;

    private AutoCloseable object;

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

    @BeforeEach
    void stubConfiguration() throws ReflectiveOperationException {
        lenient().when(daConfiguration.artifactory()).thenReturn(rtConfig);
        lenient().when(rtConfig.url()).thenReturn(Optional.of(URI.create("http://localhost:8082")));
        Map<RepositoryType, String> groups = Map.of(
                RepositoryType.MAVEN,
                "DA-MVN-TEST-GROUP",
                RepositoryType.NPM,
                "DA-NPM-TEST-GROUP");
        lenient().when(rtConfig.groups()).thenReturn(groups);
        lenient().when(rtConfig.accessToken()).thenReturn(Optional.of("ACCESS_TOKEN"));
        lenient().when(rtConfig.requestTimeout()).thenReturn(Duration.of(30000, ChronoUnit.MILLIS));
        lenient().when(rtConfig.requestRetries()).thenReturn(10);

        artifactoryConnector = new ArtifactoryConnector(log, userLog, daConfiguration, parser);
        object = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void finish() throws Exception {
        object.close();
    }

    @Test
    public void testGetVersionsOfGA() throws CommunicationException {
        stubFor(
                get(urlEqualTo("/artifactory/DA-MVN-TEST-GROUP/foo/bar/baz/maven-metadata.xml")).willReturn(
                        aResponse().withStatus(200)
                                .withHeader("Content-Type", "text/xml")
                                .withBody(FOOBAR_MAVEN_METADATA)));

        List<String> versionsOfGA = artifactoryConnector.getVersionsOfGA(GA);

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
                get(urlEqualTo("/artifactory/DA-NPM-TEST-GROUP/jquery/package.json")).willReturn(
                        aResponse().withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBodyFile("jquery-package.json")));

        List<String> versionsOfGA = artifactoryConnector.getVersionsOfNpm("jquery");

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
