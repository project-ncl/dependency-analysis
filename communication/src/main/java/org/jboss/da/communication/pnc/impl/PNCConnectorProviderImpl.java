package org.jboss.da.communication.pnc.impl;

import org.jboss.da.communication.pnc.api.PNCConnectorProvider;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.common.util.Configuration;
import org.jboss.da.common.util.ConfigurationParseException;
import org.jboss.da.communication.auth.AuthenticatorService;
import org.jboss.da.communication.pnc.api.PNCAuthConnector;
import org.jboss.da.communication.pnc.api.PNCConnector;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@ApplicationScoped
public class PNCConnectorProviderImpl implements PNCConnectorProvider {

    private static final int POOL_SIZE = 10;

    private static final String PNC_REST_PATH = "/pnc-rest/rest/";

    @Inject
    private AuthenticatorService auth;

    private final ResteasyWebTarget target;

    @Inject
    public PNCConnectorProviderImpl(Configuration config) throws ConfigurationParseException {
        String pncBaseUrl = config.getConfig().getPncServer() + PNC_REST_PATH;
        ResteasyClient client = new ResteasyClientBuilder().connectionPoolSize(POOL_SIZE).build();
        target = client.target(pncBaseUrl);
    }

    @Override
    public PNCConnector getConnector() {
        return new PNCConnectorImpl(target);
    }

    @Override
    public PNCAuthConnector getAuthConnector(String token) throws CommunicationException {
        if (token == null) {
            return new PNCConnectorImpl(target, auth.accessToken()
                    .orElseThrow(() -> new CommunicationException("Current user not authenticated.")));
        }
        return new PNCConnectorImpl(target, token);
    }
}
