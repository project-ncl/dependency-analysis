package org.jboss.da.test.client.rest.reports;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.jboss.da.test.client.rest.AbstractRestReportsTest;
import static org.junit.Assert.assertEquals;
import org.junit.Assume;
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
        final String repo = System.getenv("DA_hosted_repo");
        Assume.assumeTrue(repo != null);
        String gavNonexisting = "keycloak-1.6.0.Final";
        File jsonRequestFile = getJsonRequestFile(PATH_SCM, gavNonexisting);
        String json = FileUtils.readFileToString(jsonRequestFile, ENCODING);
        json = json.replace("${DA-hosted-repo}", repo);

        Response response = createClientRequest(PATH_SCM).post(Entity.json(json));

        assertEquals(200, response.getStatus());
    }

    @Test
    public void testDA179() throws Exception {
        String gavNonexisting = "pnc-3de7ed5";
        File jsonRequestFile = getJsonRequestFile(PATH_SCM, gavNonexisting);

        Response response = createClientRequest(PATH_SCM)
                .post(Entity.json(FileUtils.readFileToString(jsonRequestFile, ENCODING)));

        assertEquals(200, response.getStatus());
    }

    @Test
    public void testNCL5377() throws Exception {
        String nonOSGiDependency = "NCL5377";
        File jsonRequestFile = getJsonRequestFile(PATH_SCM, nonOSGiDependency);

        Response response = createClientRequest(PATH_SCM)
                .post(Entity.json(FileUtils.readFileToString(jsonRequestFile, ENCODING)));

        assertEquals(200, response.getStatus());
    }
}
