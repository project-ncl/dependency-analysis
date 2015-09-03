package org.jboss.da.common.util;

/**
 * Created by dcheung on 30/06/15.
 */
public class ConfigurationParseException extends Exception {

    public ConfigurationParseException(Throwable cause) {
        super(cause);
    }

    public ConfigurationParseException(String msg) {
        super(msg);
    }
}
