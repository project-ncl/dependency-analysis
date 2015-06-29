package org.jboss.da.communcation.pnc.authentication;

import javax.inject.Inject;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import java.io.IOException;

/**
 * Filter to add authorization header to all http requests to PNCProducer
 */
public class PNCAuthFilter implements ClientRequestFilter {

    PNCAuthentication pncAuthenticate = new PNCAuthentication();
    /**
     * It works by add the Authorization key, followed by "Bearer <token>"
     * to the http request
     *
     * @param requestContext
     * @throws IOException
     */
    public void filter(ClientRequestContext requestContext) throws IOException {
        String token = pncAuthenticate.authenticate();
        requestContext.getHeaders().add("Authorization", "Bearer " + token);
    }
}
