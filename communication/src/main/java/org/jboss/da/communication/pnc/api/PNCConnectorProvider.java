package org.jboss.da.communication.pnc.api;

import org.jboss.da.common.CommunicationException;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
public interface PNCConnectorProvider {

    PNCAuthConnector getAuthConnector(String token) throws CommunicationException;

    PNCConnector getConnector();

}
