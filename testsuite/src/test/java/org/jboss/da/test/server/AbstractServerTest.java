package org.jboss.da.test.server;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.extension.RegisterExtension;

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
