/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.da.test.client.rest.reports;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.jboss.da.test.client.rest.AbstractRestReportsTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import jakarta.ws.rs.core.Response;

/**
 *
 * @author sknot
 */
@QuarkusTest
@QuarkusTestResource(value = H2DatabaseTestResource.class, restrictToAnnotatedClass = true)
public class AlignRemoteTestIT extends AbstractRestReportsTest {

    @Test
    public void test() throws Exception {
        try (Response response = assertResponseForRequest(
                RestApiReportsRemoteTestIT.PATH_REPORTS_ALIGN,
                "align-test-wrong")) {
            assertEquals(500, response.getStatus());
        }
    }
}
