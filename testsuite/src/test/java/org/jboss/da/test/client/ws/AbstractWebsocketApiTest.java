package org.jboss.da.test.client.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.CloseReason;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;
import org.apache.commons.io.FileUtils;
import org.jboss.da.test.client.AbstractClientApiTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractWebsocketApiTest extends AbstractClientApiTest {

    protected final String webSocketUrl;

    protected JSONRPCWebsocketEndpoint endpoint;

    protected Session session;

    public AbstractWebsocketApiTest() {
        this.webSocketUrl = readWebsocketApiUrl();
    }

    @BeforeEach
    public void setup() throws URISyntaxException, DeploymentException, IOException {
        URI uri = new URI(webSocketUrl);
        endpoint = new JSONRPCWebsocketEndpoint();
        ClientEndpointConfig cec = ClientEndpointConfig.Builder.create().build();
        session = ContainerProvider.getWebSocketContainer().connectToServer(endpoint, cec, uri);
    }

    @AfterEach
    public void cleanup() throws InterruptedException, IOException {
        if (endpoint != null) {
            endpoint.close(5, TimeUnit.SECONDS);
        }
        if (session != null) {
            session.close();
        }
    }

    private String readWebsocketApiUrl() {
        return readConfigurationValue("testsuite.websocketApiUrl", "ws://" + hostUrl + "/" + getContextRoot() + "/ws");
    }

    protected JSONRPC2Response assertResponseForRequest(String path, String requestFile, String method)
            throws IOException, InterruptedException, TimeoutException {
        File jsonRequestFile = getJsonRequestFile(path, requestFile);
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> parameters = mapper.readValue(jsonRequestFile, Map.class);
        JSONRPC2Request jsonRequest = new JSONRPC2Request(method, parameters, 1);

        JSONRPC2Response response = endpoint.sendRequest(jsonRequest);
        String responseString = mapper.writeValueAsString(response.getResult());

        assertTrue(response.indicatesSuccess());
        File expectedResponseFile = getJsonResponseFile(path, requestFile);
        assertEqualsJson(
                FileUtils.readFileToString(expectedResponseFile, Charset.defaultCharset()).trim(),
                responseString.trim());
        return response;
    }

    protected static class JSONRPCWebsocketEndpoint extends Endpoint {

        private final ConcurrentMap<Object, JSONRPC2Response> responses = new ConcurrentHashMap<>();

        private final CountDownLatch closeLatch = new CountDownLatch(1);

        private final AtomicLong sequence = new AtomicLong();

        private jakarta.websocket.Session session;

        @Override
        public void onOpen(jakarta.websocket.Session session, EndpointConfig config) {
            System.out.printf("Got connect: %s%n", session);
            this.session = session;
            session.addMessageHandler(String.class, (String message) -> {
                System.out.printf("Got message: " + message);
                try {
                    JSONRPC2Response parse = JSONRPC2Response.parse(message);
                    responses.put(parse.getID(), parse);
                } catch (JSONRPC2ParseException ex) {
                    Logger.getLogger(AbstractWebsocketApiTest.class.getName())
                            .log(Level.SEVERE, "Unknown message: " + message, ex);
                }
            });

        }

        public boolean close(int duration, TimeUnit unit) throws InterruptedException, IOException {
            System.out.println("Closing endpoint");
            if (session != null) {
                session.close();
            }
            return this.closeLatch.await(duration, unit);
        }

        @Override
        public void onClose(jakarta.websocket.Session session, CloseReason closeReason) {
            System.out.printf("Connection closed: - %s%n", closeReason);
            this.session = null;
            this.closeLatch.countDown(); // trigger latch
        }

        public JSONRPC2Response sendRequest(JSONRPC2Request request)
                throws InterruptedException, TimeoutException, IOException {
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
