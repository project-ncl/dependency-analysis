package org.jboss.da.test.client.rest.reports;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.jboss.da.test.client.rest.AbstractRestReportsTest;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public class BugReporoducerRemoteTest extends AbstractRestReportsTest {

    private static final String ENCODING = "utf-8";

    private static final String PATH_SCM = "/reports/scm";

    @Test
    public void testDA176() throws Exception {
        String gavNonexisting = "keycloak-1.6.0.Final";
        File jsonRequestFile = getJsonRequestFile(PATH_SCM, gavNonexisting);

        Response response = createClientRequest(PATH_SCM).post(
                Entity.json(FileUtils.readFileToString(jsonRequestFile, ENCODING)));

        assertEquals(200, response.getStatus());
    }

    @Test
    public void testDA179() throws Exception {
        String gavNonexisting = "pnc-3de7ed5";
        File jsonRequestFile = getJsonRequestFile(PATH_SCM, gavNonexisting);

        Response response = createClientRequest(PATH_SCM).post(
                Entity.json(FileUtils.readFileToString(jsonRequestFile, ENCODING)));

        assertEquals(200, response.getStatus());
    }
}
