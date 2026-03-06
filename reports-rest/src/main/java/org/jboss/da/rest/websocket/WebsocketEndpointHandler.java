package org.jboss.da.rest.websocket;

import jakarta.websocket.Session;

public interface WebsocketEndpointHandler {

    void onMessage(Session session, String msg);

    /**
     * Provides the methods the instance can handle.
     *
     * @param methods
     */
    void setMethods(Methods methods);

}
