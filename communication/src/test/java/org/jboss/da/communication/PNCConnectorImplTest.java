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

    private static final String TOKEN = "magic_token";

    private static final String PNC_BASE_URL = "http://10.10.10.10:8080";

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
    public void shouldGenerateRightUriForRequest() throws Exception {
        ClientRequest req = pncConnectorImpl.getClient("gabriella", "");
        assertEquals(PNC_BASE_URL + "/pnc-rest/rest/gabriella", req.getUri());
    }

    @Test
    public void shouldAddAuthenticationTokenToHeader() throws Exception {
        ClientRequest req = pncConnectorImpl.getClient("testme", TOKEN);

        MultivaluedMap<String, String> headers = req.getHeaders();
        assertTrue(headers.containsKey("Authorization"));
        assertEquals("Bearer " + TOKEN, headers.getFirst("Authorization"));
    }
}
