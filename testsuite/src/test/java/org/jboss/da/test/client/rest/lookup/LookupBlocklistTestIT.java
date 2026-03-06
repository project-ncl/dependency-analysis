package org.jboss.da.test.client.rest.lookup;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;
import org.jboss.da.test.client.rest.AbstractRestReportsTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.jboss.da.test.client.rest.listings.AbstractRestApiListingTest.PATH_BLACK_LISTINGS_GAV;
import static org.jboss.da.test.client.rest.lookup.LookupTestIT.PATH_LOOKUP_MAVEN;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@QuarkusTestResource(value = H2DatabaseTestResource.class, restrictToAnnotatedClass = true)
public class LookupBlocklistTestIT extends AbstractRestReportsTest {

    @BeforeEach
    public void prepareBlocklist() {
        String blocklist = "{\"groupId\":\"com.google.guava\",\"artifactId\":\"guava\",\"version\":\"13.0.1-redhat-2\"}";
        createClientRequest(PATH_BLACK_LISTINGS_GAV).post(Entity.json(blocklist)).close();
    }

    @Test
    public void testGavLookupSingleWithBlocklist() throws Exception {
        try (Response response = assertResponseForRequest(PATH_LOOKUP_MAVEN, "guava13Blocklist")) {
            assertEquals(200, response.getStatus());
        }
    }

    @AfterEach
    public void cleanBlocklist() {
        String blocklist = "{\"groupId\":\"com.google.guava\",\"artifactId\":\"guava\",\"version\":\"13.0.1-redhat-2\"}";
        createClientRequest(PATH_BLACK_LISTINGS_GAV).method("DELTE", Entity.json(blocklist)).close();
    }
}
