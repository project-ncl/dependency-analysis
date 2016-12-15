package org.jboss.da.communication.pnc.api;

/**
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
public class PNCRequestException extends Exception {

    public PNCRequestException(String msg) {
        super(msg);
    }

    public PNCRequestException(String msg, Throwable ex) {
        super(msg + ": " + ex.getMessage(), ex);
    }
}
