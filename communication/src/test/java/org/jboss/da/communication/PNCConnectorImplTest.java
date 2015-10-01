package org.jboss.da.communication;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import org.jboss.da.common.json.DAConfig;
import org.jboss.da.common.util.Configuration;
import org.jboss.da.common.util.ConfigurationParseException;
import org.jboss.da.communication.pnc.authentication.PNCAuthentication;
import org.jboss.da.communication.pnc.impl.PNCConnectorImpl;
import org.jboss.resteasy.client.ClientRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.MultivaluedMap;

@RunWith(MockitoJUnitRunner.class)
public class PNCConnectorImplTest {

    private final DAConfig daConfig = new DAConfig();

    private final String token = "magic_token";

    private final String PNC_BASE_URL = "http://10.19.208.25:8180";

    @Spy
    private Configuration configuration = new Configuration();

    @Mock
    private PNCAuthentication pncAuthenticate;

    @InjectMocks
    private PNCConnectorImpl pncConnectorImpl;

    public PNCConnectorImplTest() throws ConfigurationParseException {
        MockitoAnnotations.initMocks(this);
        daConfig.setPncServer(PNC_BASE_URL);

        when(configuration.getConfig()).thenReturn(daConfig);

        pncConnectorImpl = new PNCConnectorImpl(configuration);
    }

    @Test
    public void shouldGenerateRightUriBasedOnPncServerAndEndpoint() throws Exception {
        ClientRequest req = pncConnectorImpl.getClient("matin");
        assertEquals(PNC_BASE_URL + "/pnc-rest/rest/matin", req.getUri());
    }

    @Test
    public void shouldGenerateRightUriBasedOnPncServerAndEndpointAuthenticated() throws Exception {
        ClientRequest req = pncConnectorImpl.getAuthenticatedClient("gabriella");
        assertEquals(PNC_BASE_URL + "/pnc-rest/rest/gabriella", req.getUri());
    }

    @Test
    public void shouldAddAuthenticationTokenToHeaderForAuthenticatedEndpoint() throws Exception {
        when(pncAuthenticate.authenticate()).thenReturn(token);

        ClientRequest req = pncConnectorImpl.getAuthenticatedClient("testme");

        MultivaluedMap<String, String> headers = req.getHeaders();
        assertTrue(headers.containsKey("Authorization"));
        assertEquals("Bearer " + token, headers.getFirst("Authorization"));
    }
}
