package org.jboss.da.test.client.rest;

import io.quarkus.logging.Log;
import org.apache.commons.io.FileUtils;
import org.jboss.da.test.client.AbstractClientApiTest;

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 *
 * @author jbrazdil
 */
public abstract class AbstractRestApiTest extends AbstractClientApiTest {

    protected final String restApiURL;

    protected final String restApiVersion;

    private final ClientBuilder builder;

    public AbstractRestApiTest() {
        this.restApiVersion = readRestApiVersion();
        this.restApiURL = readRestApiUrl();

        String userId = "user";
        String password = "pass.1234";
        ClientRequestFilter crf = null;// new BasicAuthentication(userId, password);
        builder = ClientBuilder.newBuilder();// .register(crf);
    }

    private String readRestApiUrl() {
        return readConfigurationValue(
                "testsuite.restApiUrl",
                "http://" + hostUrl + "/" + getContextRoot() + "/rest"
                        + (restApiVersion == null ? "" : "/" + restApiVersion));
    }

    protected WebTarget createWebTarget(String relativePath) {
        return builder.build().target(restApiURL + relativePath);
    }

    protected Invocation.Builder createClientRequest(String relativePath) {
        Log.warn("### Using request target " + restApiURL + relativePath);
        WebTarget target = builder.build().target(restApiURL + relativePath);
        return target.request(MediaType.APPLICATION_JSON_TYPE);
    }

    protected Response getResponseForRequest(String endpoint, String requestFile) throws IOException {
        File jsonRequestFile = getJsonRequestFile(endpoint, requestFile);
        System.out.println(
                "### Using file " + jsonRequestFile + " containing "
                        + FileUtils.readFileToString(jsonRequestFile, ENCODING));
        final String entity = FileUtils.readFileToString(jsonRequestFile, ENCODING);
        return createClientRequest(endpoint).post(Entity.json(entity));
    }

    protected Response assertResponseForRequest(String endpoint, String requestFile) throws Exception {
        Response response = getResponseForRequest(endpoint, requestFile);
        File expectedResponseFile = getJsonResponseFile(endpoint, requestFile);
        System.out.println(
                "### Expected from file " + expectedResponseFile + " is: "
                        + FileUtils.readFileToString(expectedResponseFile, Charset.defaultCharset()));
        final String actual = response.readEntity(String.class).trim();
        System.out.println("### Actual: " + actual);
        assertEqualsJson(FileUtils.readFileToString(expectedResponseFile, Charset.defaultCharset()).trim(), actual);
        return response;
    }
}
