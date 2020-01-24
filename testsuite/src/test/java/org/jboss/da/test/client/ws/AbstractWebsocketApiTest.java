package org.jboss.da.test.client.ws;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.jboss.da.test.client.AbstractClientApiTest;
import org.junit.After;
import org.junit.Before;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

@RunWith(Arquillian.class)
@RunAsClient
public abstract class AbstractWebsocketApiTest extends AbstractClientApiTest {

    protected final String webSocketUrl;

    protected JSONRPCWebsocketEndpoint endpoint;

    public AbstractWebsocketApiTest() {
        this.webSocketUrl = readWebsocketApiUrl();
    }

    @Before
    public void setup() throws URISyntaxException, DeploymentException, IOException {
        URI uri = new URI(webSocketUrl);

        endpoint = new JSONRPCWebsocketEndpoint();
        ClientEndpointConfig cec = ClientEndpointConfig.Builder.create().build();
        ContainerProvider.getWebSocketContainer().connectToServer(endpoint, cec, uri);
    }

    @After
    public void cleanup() throws InterruptedException, IOException {
        if (endpoint != null) {
            endpoint.close(5, TimeUnit.SECONDS);
        }
    }

    private String readWebsocketApiUrl() {
        return readConfigurationValue("testsuite.websocketApiUrl", "ws://" + hostUrl + "/" + getContextRoot() + "/ws");
    }

    protected JSONRPC2Response assertResponseForRequest(String path, String requestFile, String method)
            throws IOException, InterruptedException, ExecutionException, TimeoutException {
        File jsonRequestFile = getJsonRequestFile(path, requestFile);
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> parameters = mapper.readValue(jsonRequestFile, Map.class);
        JSONRPC2Request jsonRequest = new JSONRPC2Request(method, parameters, 1);

        JSONRPC2Response response = endpoint.sendRequest(jsonRequest);
        String responseString = mapper.writeValueAsString(response.getResult());

        assertTrue(response.indicatesSuccess());
        File expectedResponseFile = getJsonResponseFile(path, requestFile);
        assertEqualsJson(FileUtils.readFileToString(expectedResponseFile).trim(), responseString.trim());
        return response;
    }

    protected static class JSONRPCWebsocketEndpoint extends Endpoint {

        private final ConcurrentMap<Object, JSONRPC2Response> responses = new ConcurrentHashMap<>();

        private final CountDownLatch closeLatch = new CountDownLatch(1);

        private final AtomicLong sequence = new AtomicLong();

        private javax.websocket.Session session;

        @Override
        public void onOpen(javax.websocket.Session session, EndpointConfig config) {
            System.out.printf("Got connect: %s%n", session);
            this.session = session;
            session.addMessageHandler(String.class, (MessageHandler.Whole<String>) (String message) -> {
                System.out.printf("Got message: " + message);
                try {
                    JSONRPC2Response parse = JSONRPC2Response.parse(message);
                    responses.put(parse.getID(), parse);
                } catch (JSONRPC2ParseException ex) {
                    Logger.getLogger(AbstractWebsocketApiTest.class.getName()).log(Level.SEVERE, "Unknown message: " + message,
                            ex);
                }
            });

        }

        public boolean close(int duration, TimeUnit unit) throws InterruptedException, IOException {
            System.out.printf("Closing endpint");
            session.close();
            return this.closeLatch.await(duration, unit);
        }

        @Override
        public void onClose(javax.websocket.Session session, CloseReason closeReason) {
            System.out.printf("Connection closed: %d - %s%n", closeReason);
            this.session = null;
            this.closeLatch.countDown(); // trigger latch
        }

        public JSONRPC2Response sendRequest(JSONRPC2Request request)
                throws InterruptedException, ExecutionException, TimeoutException, IOException {
            System.out.println("Sending message");
            Long id = sequence.incrementAndGet();
            request.setID(id);
            session.getBasicRemote().sendText(request.toJSONString());
            System.out.println("Message sent");
            int timeout = 600;
            while (timeout-- > 0) {
                JSONRPC2Response resp = responses.get(id);
                if (resp != null) {
                    return resp;
                }
                Thread.sleep(1000);
            }
            throw new TimeoutException();
        }
    }
}
