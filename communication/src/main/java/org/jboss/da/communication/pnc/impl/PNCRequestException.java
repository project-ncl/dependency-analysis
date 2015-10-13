package org.jboss.da.communication.pnc.impl;

import lombok.ToString;

/**
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
@ToString
public class PNCRequestException extends Exception {

    public PNCRequestException(String msg) {
        super(msg);
    }

    public PNCRequestException(String msg, Throwable ex) {
        super(msg, ex);
    }
}
