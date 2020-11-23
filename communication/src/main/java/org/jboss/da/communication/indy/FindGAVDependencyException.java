package org.jboss.da.communication.indy;

/**
 * This exception is thrown when the GAV we want to analyze for dependencies cannot be found on the Indy server
 */
public class FindGAVDependencyException extends Exception {

    public FindGAVDependencyException(String message) {
        super(message);
    }

    public FindGAVDependencyException(String message, Throwable cause) {
        super(message + ": " + cause.getMessage(), cause);
    }

    public FindGAVDependencyException(Throwable cause) {
        super(cause);
    }
}
