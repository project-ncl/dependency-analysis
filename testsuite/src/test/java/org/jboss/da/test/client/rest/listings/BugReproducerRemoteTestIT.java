/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.da.test.client.rest.listings;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Disabled
public class BugReproducerRemoteTestIT extends AbstractRestApiListingTest {

    private static final String PATH_LOOKUP_GAVS = "/reports/lookup/gavs";
    private static final String PATH_LOOKUP_MAVEN = "/lookup/maven";

    @Test
    public void testNCL5035() throws Exception {
        manipulateEntityFile(OperationType.POST, "gavParent").close();

        try (Response response = assertResponseForRequest(PATH_LOOKUP_GAVS, "ncl5035")) {
            assertEquals(200, response.getStatus());
        }
    }

    @Test
    public void testNCL5035OnLookupEndpoint() throws Exception {
        manipulateEntityFile(OperationType.POST, "gavParent").close();

        try (Response response = assertResponseForRequest(PATH_LOOKUP_MAVEN, "ncl5035")) {
            assertEquals(200, response.getStatus());
        }
    }
}
