/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.da.test.client.rest.reports;

import org.jboss.da.test.client.rest.AbstractRestReportsTest;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import javax.ws.rs.core.Response;

/**
 *
 * @author sknot
 */
public class AlignRemoteTest extends AbstractRestReportsTest {

    @Test
    public void test() throws Exception {
        Response response = assertResponseForRequest(RestApiReportsRemoteTest.PATH_REPORTS_ALIGN,
                "align-test-wrong");
        assertEquals(500, response.getStatus());
    }
}
