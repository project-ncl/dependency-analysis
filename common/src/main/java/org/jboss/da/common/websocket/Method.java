package org.jboss.da.common.websocket;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 * 
 * @param <T> parameter type
 * @param <S> return type after method execution
 * @param <R> Json output type (e.g. Map, Set, ..)
 */

public interface Method<T, S, R> {

    public String getName();

    public Class<T> getParameterClass();

    public Class<R> getJsonOutputClass();

    public S execute(T params) throws Exception;
}
