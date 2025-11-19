package org.jboss.da.test.client.rest.lookup;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.jboss.da.test.client.rest.AbstractRestReportsTest;
import org.junit.Test;

import static org.jboss.da.test.client.rest.listings.AbstractRestApiListingTest.PATH_BLACK_LISTINGS_GAV;
import static org.junit.Assert.assertEquals;

public class LookupTestIT extends AbstractRestReportsTest {

    static final String PATH_LOOKUP_MAVEN = "/lookup/maven";
    private static final String PATH_LATEST_MAVEN = "/lookup/maven/latest";
    private static final String PATH_MAVEN_VERSIONS = "/lookup/maven/versions";

    private static final String PATH_LOOKUP_NPM = "/lookup/npm";
    private static final String PATH_NPM_VERSIONS = "/lookup/npm/versions";

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
    public void testGavLatestTemporary() throws Exception {
        Response response = assertResponseForRequest(PATH_LATEST_MAVEN, "guava13Temp");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGavLatestMissingTemporary() throws Exception {
        Response response = assertResponseForRequest(PATH_LATEST_MAVEN, "guava13Temp2");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGavLookupList() throws Exception {
        Response response = assertResponseForRequest(PATH_LOOKUP_MAVEN, "guava13List");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGavVersions() throws Exception {
        Response response = assertResponseForRequest(PATH_MAVEN_VERSIONS, "guava13");
        assertEquals(200, response.getStatus());
        response = assertResponseForRequest(PATH_MAVEN_VERSIONS, "guava13Minor");
        assertEquals(200, response.getStatus());
        response = assertResponseForRequest(PATH_MAVEN_VERSIONS, "guava13Closest");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testPackageVersions() throws Exception {
        Response response = assertResponseForRequest(PATH_NPM_VERSIONS, "jquery151");
        assertEquals(200, response.getStatus());
        response = assertResponseForRequest(PATH_NPM_VERSIONS, "jquery151Major");
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
