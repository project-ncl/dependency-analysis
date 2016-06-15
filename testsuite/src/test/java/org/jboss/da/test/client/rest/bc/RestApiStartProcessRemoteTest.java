/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.da.test.client.rest.bc;

import org.jboss.da.test.client.rest.AbstractRestBCTest;
import org.jboss.resteasy.client.ClientResponse;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author sknot
 */
public class RestApiStartProcessRemoteTest extends AbstractRestBCTest {

    @Test
    public void testStartProcess() throws Exception {
        ClientResponse<String> response = assertResponseForRequest(RestApiBCTest.PATH_BC_START,
                "start-process-wrong");
        assertEquals(500, response.getStatus());
    }

}
