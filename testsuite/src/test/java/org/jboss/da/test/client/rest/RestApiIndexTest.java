package org.jboss.da.test.client.rest;

import org.apache.http.entity.ContentType;
import org.jboss.da.test.client.AbstractRestApiTest;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Test;

import java.io.File;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.http.entity.ContentType.TEXT_HTML;
import static org.junit.Assert.assertEquals;

public class RestApiIndexTest extends AbstractRestApiTest {

    @Test
    public void testIndexHtml() throws Exception {
        String path = "/";
        ContentType contentType = TEXT_HTML;
        ClientRequest request = new ClientRequest(restApiURL + path);
        request.header("Content-Type", contentType);

        ClientResponse<String> response = request.get(String.class);

        File expectedResponseFile = new ExpectedResponseFilenameBuilder(
                restApiExpectedResponseFolder, path, contentType).getFile();
        assertEquals(readFileToString(expectedResponseFile), response.getEntity(String.class));
    }

}
