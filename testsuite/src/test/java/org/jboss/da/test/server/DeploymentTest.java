package org.jboss.da.test.server;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.da.test.ArquillianDeploymentFactory;
import org.jboss.da.test.ArquillianDeploymentFactory.DepType;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@RunWith(Arquillian.class)
public class DeploymentTest {

    @Deployment
    public static EnterpriseArchive createDeployment() {
        return new ArquillianDeploymentFactory().createDeployment(DepType.REPORTS);
    }

    @Test
    public void testDeployment() {
    }

}
