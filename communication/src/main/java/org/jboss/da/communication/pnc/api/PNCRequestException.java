package org.jboss.da.communication.pnc.api;

import org.jboss.da.common.CommunicationException;

/**
 * Exception thrown when there is error in communication with PNC.
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public class PNCRequestException extends CommunicationException {

    public PNCRequestException(String message) {
        super(message);
    }

    public PNCRequestException(String message, Throwable cause) {
        super(message, cause);
    }

}
