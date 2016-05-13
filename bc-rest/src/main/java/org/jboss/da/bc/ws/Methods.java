package org.jboss.da.bc.ws;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
public interface Methods {

    boolean contains(String method);

    Method get(String method);

}
