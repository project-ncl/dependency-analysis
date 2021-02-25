package org.jboss.da.test.client.rest.lookup;

import javax.ws.rs.core.Response;

import org.jboss.da.test.client.rest.AbstractRestReportsTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LookupTestIT extends AbstractRestReportsTest {

    private static final String PATH_LOOKUP_MAVEN = "/lookup/maven";

    private static final String PATH_LOOKUP_NPM = "/lookup/npm";

    @Test
    public void testGavLookupSingle() throws Exception {
        Response response = assertResponseForRequest(PATH_LOOKUP_MAVEN, "guava13");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGavLookupSingleTemporary() throws Exception {
        Response response = assertResponseForRequest(PATH_LOOKUP_MAVEN, "guava13Temp");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGavLookupList() throws Exception {
        Response response = assertResponseForRequest(PATH_LOOKUP_MAVEN, "guava13List");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testNCLSUP132() throws Exception {
        Response response = assertResponseForRequest(PATH_LOOKUP_MAVEN, "NCLSUP132");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testNPMLookupSingle() throws Exception {
        Response response = assertResponseForRequest(PATH_LOOKUP_NPM, "jquery151");
        assertEquals(200, response.getStatus());
    }
}
