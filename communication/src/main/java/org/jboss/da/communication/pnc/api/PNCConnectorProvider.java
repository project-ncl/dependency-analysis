package org.jboss.da.communication.pnc.api;

import org.jboss.da.common.CommunicationException;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public interface PNCConnectorProvider {

    PNCAuthConnector getAuthConnector(String token) throws CommunicationException;

    PNCConnector getConnector();

}
