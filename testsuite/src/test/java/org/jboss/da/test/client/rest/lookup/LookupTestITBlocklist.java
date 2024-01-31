package org.jboss.da.test.client.rest.lookup;

import org.jboss.da.test.client.rest.AbstractRestReportsTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static org.jboss.da.test.client.rest.listings.AbstractRestApiListingTest.PATH_BLACK_LISTINGS_GAV;
import static org.jboss.da.test.client.rest.lookup.LookupTestIT.PATH_LOOKUP_MAVEN;
import static org.junit.Assert.assertEquals;

public class LookupTestITBlocklist extends AbstractRestReportsTest {

    @Before
    public void prepareBlocklist() {
        String blocklist = "{\"groupId\":\"com.google.guava\",\"artifactId\":\"guava\",\"version\":\"13.0.1-redhat-2\"}";
        createClientRequest(PATH_BLACK_LISTINGS_GAV).post(Entity.json(blocklist)).close();
    }

    @Test
    public void testGavLookupSingleWithBlocklist() throws Exception {
        Response response = assertResponseForRequest(PATH_LOOKUP_MAVEN, "guava13Blocklist");
        assertEquals(200, response.getStatus());
    }

    @After
    public void cleanBlocklist() {
        String blocklist = "{\"groupId\":\"com.google.guava\",\"artifactId\":\"guava\",\"version\":\"13.0.1-redhat-2\"}";
        createClientRequest(PATH_BLACK_LISTINGS_GAV).method("DELTE", Entity.json(blocklist)).close();
    }
}
