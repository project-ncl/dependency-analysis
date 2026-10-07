package org.jboss.da.test.client.rest;

import static org.jboss.da.common.Constants.COMMIT_HASH;
import static org.jboss.da.common.Constants.DA_VERSION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import jakarta.ws.rs.core.Response;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(value = H2DatabaseTestResource.class, restrictToAnnotatedClass = true)
public class RestApiVersionTestIT extends AbstractRestReportsTest {

    @Test
    public void testVersionEndpoint() {
        String path = "/version";
        Response response = createClientRequest(path).get();
        assertEquals(200, response.getStatus());

        JSONObject json = new JSONObject(response.readEntity(String.class));
        assertEquals("Dependency Analysis", json.getString("name"));
        assertEquals(DA_VERSION, json.getString("version"));
        assertEquals(COMMIT_HASH, json.getString("commit"));
        assertFalse(json.getString("builtOn").isEmpty());
    }
}
