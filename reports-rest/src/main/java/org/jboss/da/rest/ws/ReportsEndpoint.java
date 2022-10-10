package org.jboss.da.rest.ws;

import org.jboss.da.common.logging.AuditLogger;
import org.jboss.da.rest.websocket.Methods;
import org.jboss.da.rest.websocket.WebsocketEndpointHandler;
import org.slf4j.MDC;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/ws")
public class ReportsEndpoint {

    @Inject
    @ReportsWebsocketMethods
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

    @OnOpen
    public void onOpen(Session session) {
        AuditLogger.LOG.info("Opened websocket communication with session id " + session.getId() + ".");
    }

    @OnClose
    public void onClose(Session session) {
        AuditLogger.LOG.info("Closed websocket communication with session id " + session.getId() + ".");
    }

}
