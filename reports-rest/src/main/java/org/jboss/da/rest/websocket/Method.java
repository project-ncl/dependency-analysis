package org.jboss.da.rest.websocket;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 *
 * @param <T> parameter type
 * @param <S> return type after method execution
 * @param <R> Json output type (e.g. Map, Set, ..)
 */

public interface Method<T, S, R> {

    String getName();

    Class<T> getParameterClass();

    Class<R> getJsonOutputClass();

    S execute(T params) throws Exception;
}
