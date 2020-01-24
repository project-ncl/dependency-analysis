package org.jboss.da.communication.repository.api;

import org.jboss.da.common.CommunicationException;

/**
 * Exception thrown when there is error in communication with artifact repository.
 * 
 * @author Honza Brázdil &lt;janinko.g@gmail.com&gt;
 */
public class RepositoryException extends CommunicationException {

    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

}
