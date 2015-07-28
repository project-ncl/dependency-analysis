package org.jboss.da.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
@RunWith(Arquillian.class)
public class TestOfTheTest {

    @Deployment
    public static EnterpriseArchive createDeployment() {
        return AbstractArquillianTest.createDeployment();
    }

    @Test
    public void testTheTest() {
        System.out.println("All systems are go!");
    }

}
