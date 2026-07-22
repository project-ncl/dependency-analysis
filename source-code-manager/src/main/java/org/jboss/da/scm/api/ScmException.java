package org.jboss.da.scm.api;

/**
 * Thrown when an operation with an SCM repository fails.
 */
public class ScmException extends Exception {

    public ScmException(String message) {
        super(message);
    }

    public ScmException(String message, Throwable cause) {
        super(message, cause);
    }
}