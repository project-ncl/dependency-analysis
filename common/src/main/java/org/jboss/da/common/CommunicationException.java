package org.jboss.da.common;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public abstract class CommunicationException extends Exception {

    protected CommunicationException(String message) {
        super(message);
    }

    protected CommunicationException(String message, Throwable cause) {
        super(message + ": " + (cause.getMessage() == null ? cause.toString() : cause.getMessage()), cause);
    }

    protected CommunicationException(Throwable cause) {
        super(cause);
    }

}
