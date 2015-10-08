package org.jboss.da.test.client;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.da.test.ArquillianDeploymentFactory;
import org.jboss.da.test.ArquillianDeploymentFactory.DepType;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;

public abstract class AbstractRestReportsTest extends AbstractRestApiTest {

    @Deployment
    public static EnterpriseArchive createDeployment() {
        return new ArquillianDeploymentFactory().createDeployment(DepType.REPORTS);
    }

    @Override
    protected String getContextRoot() {
        return ArquillianDeploymentFactory.DEPLOYMENT_NAME;
    }

}
