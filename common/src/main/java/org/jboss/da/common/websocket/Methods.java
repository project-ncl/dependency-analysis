package org.jboss.da.common.websocket;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
public interface Methods {

    boolean contains(String method);

    Method<?, ?, ?> get(String method);

}
