package org.jboss.da.bc.ws;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
public interface Method<T, S> {

    public String getName();

    public Class<T> getParameterClass();

    public S execute(T params) throws Exception;
}
