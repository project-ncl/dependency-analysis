package org.jboss.da.test.client.ws.reports;

import static org.jboss.da.common.Constants.REST_API_VERSION_BC;

import org.jboss.da.test.client.ws.AbstractWebsocketApiTest;

public abstract class AbstractWebsocketReportsTest extends AbstractWebsocketApiTest {

    private static final String DEFAULT_REST_API_VERSION = "v-" + REST_API_VERSION_BC;

    @Override
    protected String getContextRoot() {
        return "";
    }

    @Override
    protected String readRestApiVersion() {
        return readConfigurationValue("testsuite.restApiVersion", DEFAULT_REST_API_VERSION);
    }

    @Override
    protected String convertRestApiVersionToFolderName() {
        return getFolderName(DEFAULT_REST_API_VERSION);
    }
}
