package org.jboss.da.test.client.rest;

import static org.jboss.da.common.Constants.REST_API_VERSION_REPORTS;

public abstract class AbstractRestReportsTest extends AbstractRestApiTest {

    private static final String DEFAULT_REST_API_VERSION = "v-" + REST_API_VERSION_REPORTS;

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
