package org.jboss.da.common;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public class CommunicationException extends Exception {

    public CommunicationException(String message) {
        super(message);
    }

    public CommunicationException(String message, Throwable cause) {
        super(message + ": " + cause.getMessage(), cause);
    }

    public CommunicationException(Throwable cause) {
        super(cause);
    }

}
