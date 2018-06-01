package org.jboss.da.rest.websocket;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public interface Methods {

    boolean contains(String method);

    Method<?, ?, ?> get(String method);

}
