package org.jboss.da.test.communication;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.da.communcation.CommunicationException;
import org.jboss.da.communication.aprox.api.AproxConnector;
import org.jboss.da.communication.aprox.model.GA;
import org.jboss.da.test.AbstractArquillianTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class AproxTest {

    @Inject
    AproxConnector aproxConnector;

    @Deployment
    public static EnterpriseArchive createDeployment() {
        return AbstractArquillianTest.createDeployment();
    }

    @Test
    public void testGetVersionsOfGA() throws CommunicationException {
        GA ga = new GA("org.jboss.apiviz", "apiviz");
        List<String> apivizTest = Arrays.asList(new String[] { "1.3.1.GA-redhat-1", "1.2.5.GA", "1.3.0.GA",
                "1.3.1.GA-redhat-2", "1.3.2.GA" });
        List<String> result = aproxConnector.getVersionsOfGA(ga);
        assertTrue(apivizTest.size() == result.size());
        assertTrue(apivizTest.containsAll(result));
        assertTrue(result.containsAll(apivizTest));
    }

}
