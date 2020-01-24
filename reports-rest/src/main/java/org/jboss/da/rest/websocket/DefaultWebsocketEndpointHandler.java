package org.jboss.da.rest.websocket;

import org.jboss.pnc.pncmetrics.MetricsConfiguration;
import org.jboss.weld.context.activator.ActivateRequestContext;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;

import java.io.IOException;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import static com.codahale.metrics.MetricRegistry.name;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public class DefaultWebsocketEndpointHandler implements WebsocketEndpointHandler {

    private static final String METRICS_ALL_RATE_KEY = "ws.all.rate";

    private static final String METRICS_ALL_TIMER_KEY = "ws.all.timer";

    private static final String METRICS_ALL_ERROR_KEY = "ws.all.errors";

    private static final String METRICS_KEY = "ws";

    private static final String METRICS_RATE_KEY = ".rate";

    private static final String METRICS_TIMER_KEY = ".timer";

    private static final String METRICS_ERROR_KEY = ".errors";

    @Inject
    private Logger log;

    @Inject
    private ObjectMapper mapper;

    @Inject
    private MetricsConfiguration metricsConfiguration;

    private Methods methods;

    @Override
    public void setMethods(Methods methods) {
        this.methods = methods;
    }

    @Override
    @ActivateRequestContext
    public void onMessage(Session session, String msg) {
        Basic basic = session.getBasicRemote();
        MetricRegistry registry = metricsConfiguration.getMetricRegistry();

        Timer allTimer = registry.timer(METRICS_ALL_TIMER_KEY);
        Meter allMeter = registry.meter(METRICS_ALL_RATE_KEY);
        Meter allErrors = registry.meter(METRICS_ALL_ERROR_KEY);

        Timer.Context allTimingContext = allTimer.time();
        allMeter.mark();

        try {
            JSONRPC2Request request = getRequest(basic, msg);
            if (request == null) {
                allErrors.mark();
                return;
            }

            String methodName = request.getMethod();
            String metricsName = METRICS_KEY + name(request.getClass().getSimpleName(), methodName);

            Timer timer = registry.timer(metricsName + METRICS_TIMER_KEY);
            Meter meter = registry.meter(metricsName + METRICS_RATE_KEY);
            Meter errors = registry.meter(metricsName + METRICS_ERROR_KEY);

            Timer.Context timingContext = timer.time();
            meter.mark();

            if (!methods.contains(methodName)) {
                log.warn("Failed to find JSON RPC method " + methodName + ".");
                basic.sendText(new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, request.getID()).toString());
                allErrors.mark();
                errors.mark();
                return;
            }

            JsonNode params = mapper.readTree(msg).get("params");
            if (params == null) {
                log.warn("Failed to parse JSON RPC parameters. Parameters are null.");
                JSONRPC2Error error = JSONRPC2Error.INVALID_PARAMS;
                error = error.setData("Parameter can't be null");
                basic.sendText(new JSONRPC2Response(error, request.getID()).toString());
                allErrors.mark();
                errors.mark();
                return;
            }

            Method method = methods.get(methodName);
            Object hello;
            try {
                hello = mapper.treeToValue(params, method.getParameterClass());
            } catch (JsonProcessingException ex) {
                log.warn("Failed to parse JSON RPC parameters", ex);
                JSONRPC2Error error = JSONRPC2Error.INVALID_PARAMS;
                error = error.setData(ex.getMessage());
                basic.sendText(new JSONRPC2Response(error, request.getID()).toString());
                allErrors.mark();
                errors.mark();
                return;
            }

            Object resp;
            try {
                resp = method.execute(hello);
            } catch (Exception ex) {
                log.error("Exception while executing method " + method, ex);
                JSONRPC2Error error = JSONRPC2Error.INTERNAL_ERROR;
                error = error.setData(ex.getMessage());
                basic.sendText(new JSONRPC2Response(error, request.getID()).toString());
                allErrors.mark();
                errors.mark();
                return;
            }

            // Convert the response object to string and then to a Map (or Collection<Map>) that the
            // JSONRPC2Response can actually serialize correctly back to json. (╯°□°）╯┻━┻
            String stringMapped = mapper.writeValueAsString(resp);
            Object objResp = mapper.readValue(stringMapped, method.getJsonOutputClass());
            JSONRPC2Response response = new JSONRPC2Response(objResp, request.getID());

            if (!session.isOpen()) {
                log.warn("Session closed before response sent.");
                allErrors.mark();
                errors.mark();
                return;
            }
            final String responseText = response.toJSONString();
            basic.sendText(responseText);

            allTimingContext.stop();
            timingContext.stop();
        } catch (IOException ex) {
            log.error("Failed to process websocket request", ex);
            allErrors.mark();
        }
    }

    private JSONRPC2Request getRequest(Basic remote, String msg) throws IOException {
        try {
            return JSONRPC2Request.parse(msg);
        } catch (JSONRPC2ParseException ex) {
            JSONRPC2Error error;
            switch (ex.getCauseType()) {
                case JSONRPC2ParseException.PROTOCOL:
                    error = JSONRPC2Error.INVALID_REQUEST;
                    break;
                case JSONRPC2ParseException.JSON:
                    error = JSONRPC2Error.PARSE_ERROR;
                    break;
                default:
                    log.warn("Unknown exception cause type " + ex.getCauseType() + ".");
                    error = JSONRPC2Error.PARSE_ERROR;
                    break;
            }
            error = error.setData(ex.getMessage());
            remote.sendText(new JSONRPC2Response(error, null).toString());
            log.warn("Failed to parse JSON RPC message", ex);
            return null;
        }
    }
}
