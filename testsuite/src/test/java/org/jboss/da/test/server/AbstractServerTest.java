package org.jboss.da.test.server;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.da.test.ArquillianDeploymentFactory;
import org.jboss.da.test.ArquillianDeploymentFactory.DepType;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Rule;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
public class AbstractServerTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(8081)
            .usingFilesUnderDirectory("src/test/resources/wiremock"));

    @Deployment
    public static EnterpriseArchive createDeployment() {
        return new ArquillianDeploymentFactory().createDeployment(DepType.REPORTS);
    }

}
