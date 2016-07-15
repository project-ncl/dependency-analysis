package org.jboss.da.bc.ws;

import org.jboss.da.common.websocket.Methods;
import org.jboss.da.common.websocket.WebsocketEndpointHandler;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/ws")
public class BCWebsocketEndpoint {

    @Inject
    @BuildConfigurationWebsocketMethods
    private Methods methods;

    @Inject
    private WebsocketEndpointHandler endpointHandler;

    @PostConstruct
    private void init() {
        endpointHandler.setMethods(methods);
    }

    @OnMessage
    public void onMessage(Session session, String msg) {
        endpointHandler.onMessage(session, msg);
    }
}
