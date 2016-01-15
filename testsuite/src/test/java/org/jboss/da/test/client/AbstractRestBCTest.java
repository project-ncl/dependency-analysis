package org.jboss.da.test.client;

import static org.jboss.da.common.Constants.REST_API_VERSION_BC;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.da.test.ArquillianDeploymentFactory;
import org.jboss.da.test.ArquillianDeploymentFactory.DepType;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;

public abstract class AbstractRestBCTest extends AbstractRestApiTest {

    private static final String DEFAULT_REST_API_VERSION = "v-" + REST_API_VERSION_BC;

    @Deployment
    public static EnterpriseArchive createDeployment() {
        return new ArquillianDeploymentFactory().createDeployment(DepType.BC);
    }

    @Override
    protected String getContextRoot() {
        return "testsuite-bc";
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
