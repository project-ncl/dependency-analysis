package org.jboss.da.test.client.rest;

import org.apache.commons.io.FileUtils;
import org.jboss.da.test.client.AbstractClientApiTest;
import org.jboss.resteasy.client.jaxrs.BasicAuthentication;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.File;
import java.io.IOException;

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
        ClientRequestFilter crf = new BasicAuthentication(userId, password);
        builder = ClientBuilder.newBuilder().register(crf);
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
        WebTarget target = builder.build().target(restApiURL + relativePath);
        return target.request(MediaType.APPLICATION_JSON_TYPE);
    }

    protected Response getResponseForRequest(String endpoint, String requestFile) throws IOException {
        File jsonRequestFile = getJsonRequestFile(endpoint, requestFile);
        final String entity = FileUtils.readFileToString(jsonRequestFile, ENCODING);
        return createClientRequest(endpoint).post(Entity.json(entity));
    }

    protected Response assertResponseForRequest(String endpoint, String requestFile) throws IOException, Exception {
        Response response = getResponseForRequest(endpoint, requestFile);
        File expectedResponseFile = getJsonResponseFile(endpoint, requestFile);
        final String actual = response.readEntity(String.class).trim();
        System.out.println("Actual: " + actual);
        assertEqualsJson(FileUtils.readFileToString(expectedResponseFile).trim(), actual);
        return response;
    }
}
