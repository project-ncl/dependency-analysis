package org.jboss.da.test.server;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.da.test.ArquillianDeploymentFactory;
import org.jboss.da.test.ArquillianDeploymentFactory.DepType;
import org.jboss.da.test.ArquillianDeploymentFactory.TestSide;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
public class AbstractServerTest {

    @RegisterExtension
    public static final WireMockExtension wireMockRule = WireMockExtension.newInstance()
            .options(options().port(8081).usingFilesUnderDirectory("src/test/resources/wiremock"))
            .build();
}
