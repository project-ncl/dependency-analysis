package org.jboss.da.communcation.pnc.authentication;

import org.jboss.da.communcation.pnc.authentication.PNCAuthentication;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import java.io.IOException;

/**
 * Filter to add authorization header to all http requests to PNC
 */
public class PNCAuthFilter implements ClientRequestFilter {

    /**
     * It works by add the Authorization key, followed by "Bearer <token>"
     * to the http request
     *
     * @param requestContext
     * @throws IOException
     */
    public void filter(ClientRequestContext requestContext) throws IOException {
        String token = PNCAuthentication.authenticate();
        requestContext.getHeaders().add("Authorization", "Bearer " + token);
    }
}
