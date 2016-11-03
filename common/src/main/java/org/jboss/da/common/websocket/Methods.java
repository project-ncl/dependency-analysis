package org.jboss.da.common.websocket;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public interface Methods {

    boolean contains(String method);

    Method<?, ?, ?> get(String method);

}
