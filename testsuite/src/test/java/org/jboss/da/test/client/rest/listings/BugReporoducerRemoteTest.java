/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.da.test.client.rest.listings;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import javax.ws.rs.core.Response;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public class BugReporoducerRemoteTest extends AbstractRestApiListingTest {

    private static final String PATH_LOOKUP_GAVS = "/reports/lookup/gavs";

    @Test
    public void testNCL5035() throws Exception {
        manipulateEntityFile(ListEntityType.BLACK, OperationType.POST, "gavParent", true);

        Response response = assertResponseForRequest(PATH_LOOKUP_GAVS, "ncl5035");
        // File jsonRequestFile = getJsonRequestFile(PATH_LOOKUP_GAVS, "parent");
        // Response response = createClientRequest(PATH_LOOKUP_GAVS).post(
        // Entity.json(FileUtils.readFileToString(jsonRequestFile, ENCODING)));
        // System.out.println("Response: " + response.readEntity(String.class));

        assertEquals(200, response.getStatus());
    }
}
