package org.jboss.da.communication.pnc.api;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public interface PNCConnectorProvider {

    /**
     * Return PNC Connector that supports authenticated operations. If no token is given, token
     * of current user will be used.
     * @param token Authentication token to use or null.
     * @return PNC Connector
     * @throws IllegalStateException When no token was given and there is no current users token
     */
    PNCAuthConnector getAuthConnector(String token);

    PNCConnector getConnector();

}
