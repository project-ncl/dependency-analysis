package org.jboss.da.communication.pnc.api;

/**
 * 
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
public class PNCRequestException extends Exception {

    public PNCRequestException(String msg) {
        super(msg);
    }

    public PNCRequestException(String msg, Throwable ex) {
        super(msg, ex);
    }
}
