package org.jboss.da.communication.pnc.impl;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AuthenticationException extends Exception {

    public AuthenticationException(String msg) {
        super(msg);
    }

    public AuthenticationException(String msg, Throwable ex) {
        super(msg, ex);
    }

}
