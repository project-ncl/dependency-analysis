package org.jboss.da.common.websocket;

import javax.websocket.Session;

public interface WebsocketEndpointHandler {

    public void onMessage(Session session, String msg);

    /**
     * Provides the methods the instance can handle.
     * 
     * @param methods
     */
    public void setMethods(Methods methods);

}
