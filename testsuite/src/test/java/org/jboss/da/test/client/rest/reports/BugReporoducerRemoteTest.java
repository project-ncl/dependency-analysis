package org.jboss.da.test.client.rest.reports;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.jboss.da.test.client.AbstractRestReportsTest;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import static org.junit.Assert.assertEquals;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
public class BugReporoducerRemoteTest extends AbstractRestReportsTest {

    private static final String ENCODING = "utf-8";

    private static final String PATH_SCM = "/reports/scm";

    @Test
    public void testDA176() throws Exception {
        String gavNonexisting = "keycloak-1.6.0.Final";
        File jsonRequestFile = getJsonRequestFile(PATH_SCM, gavNonexisting);

        ClientRequest request = createClientRequest(PATH_SCM,
                FileUtils.readFileToString(jsonRequestFile, ENCODING));

        ClientResponse<String> response = request.post(String.class);

        assertEquals(200, response.getStatus());
    }

    @Test
    public void testDA179() throws Exception {
        String gavNonexisting = "pnc-3de7ed5";
        File jsonRequestFile = getJsonRequestFile(PATH_SCM, gavNonexisting);

        ClientRequest request = createClientRequest(PATH_SCM,
                FileUtils.readFileToString(jsonRequestFile, ENCODING));

        ClientResponse<String> response = request.post(String.class);

        assertEquals(200, response.getStatus());
    }
}
