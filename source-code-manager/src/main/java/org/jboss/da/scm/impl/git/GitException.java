package org.jboss.da.scm.impl.git;

/**
 * Thrown when a git command fails.
 */
public class GitException extends RuntimeException {

    public GitException(String message) {
        super(message);
    }

    public GitException(String message, Throwable cause) {
        super(message, cause);
    }
}
