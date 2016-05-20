package org.jboss.da.test.client.rest;

import org.apache.commons.io.FileUtils;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import org.jboss.da.test.client.AbstractClientApiTest;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;

import javax.ws.rs.core.MediaType;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author jbrazdil
 */
public abstract class AbstractRestApiTest extends AbstractClientApiTest {

    protected final String restApiURL;

    protected final String restApiVersion;

    public AbstractRestApiTest() {
        this.restApiVersion = readRestApiVersion();
        this.restApiURL = readRestApiUrl();
    }

    private String readRestApiUrl() {
        return readConfigurationValue("testsuite.restApiUrl", "http://" + hostUrl + "/"
                + getContextRoot() + "/rest" + (restApiVersion == null ? "" : "/" + restApiVersion));
    }

    protected ClientRequest createClientRequest(String relativePath, String jsonRequest) {
        ClientRequest request = new ClientRequest(restApiURL + relativePath);
        request.header("Content-Type", APPLICATION_JSON);
        request.body(MediaType.APPLICATION_JSON_TYPE, jsonRequest);
        return request;
    }

    protected ClientResponse<String> assertResponseForRequest(String endpoint, String requestFile)
            throws IOException, Exception {
        File jsonRequestFile = getJsonRequestFile(endpoint, requestFile);
        ClientRequest request = createClientRequest(endpoint,
                FileUtils.readFileToString(jsonRequestFile, ENCODING));
        ClientResponse<String> response = request.post(String.class);
        File expectedResponseFile = getJsonResponseFile(endpoint, requestFile);
        assertEqualsJson(FileUtils.readFileToString(expectedResponseFile).trim(), response
                .getEntity(String.class).trim());
        return response;
    }
}
