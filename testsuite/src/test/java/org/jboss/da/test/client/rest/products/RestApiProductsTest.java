package org.jboss.da.test.client.rest.products;

import org.jboss.da.test.client.rest.listings.AbstractRestApiListingTest;
import org.jboss.da.test.client.rest.listings.RequestGenerator;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.File;
import java.io.IOException;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import javax.ws.rs.core.Response;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public class RestApiProductsTest extends AbstractRestApiListingTest {

    protected static final String PATH_FILES_PRODUCTS = "/products";

    protected static final String PATH_PRODUCTS_DIFF = "/products/diff";

    private final RequestGenerator generator = new RequestGenerator();

    @Test
    public void testProductsDiff() throws Exception {
        String artifact;

        // Add whitelists
        manipulateEntityFile(ListEntityType.PRODUCT, OperationType.POST, "productAdd", true);
        long p1 = getIdOfProduct("test", "1.0.0");
        manipulateEntityFile(ListEntityType.PRODUCT, OperationType.POST, "productAdd2", true);
        long p2 = getIdOfProduct("test", "2.0.0");

        // Add artifacts to whitelist
        artifact = generator.returnWhiteArtifactString("com.example", "added", "1.0.0", p2);
        manipulateEntityString(ListEntityType.WHITE, OperationType.POST, artifact, true);

        artifact = generator.returnWhiteArtifactString("com.example", "removed", "1.0.0", p1);
        manipulateEntityString(ListEntityType.WHITE, OperationType.POST, artifact, true);

        artifact = generator.returnWhiteArtifactString("com.example", "changed", "1.0.0", p1);
        manipulateEntityString(ListEntityType.WHITE, OperationType.POST, artifact, true);
        artifact = generator.returnWhiteArtifactString("com.example", "changed", "1.2.0", p2);
        manipulateEntityString(ListEntityType.WHITE, OperationType.POST, artifact, true);

        artifact = generator.returnWhiteArtifactString("com.example", "unchanged", "1.0.0", p1);
        manipulateEntityString(ListEntityType.WHITE, OperationType.POST, artifact, true);
        artifact = generator.returnWhiteArtifactString("com.example", "unchanged", "1.0.0", p2);
        manipulateEntityString(ListEntityType.WHITE, OperationType.POST, artifact, true);

        // Get diff
        Response response = createClientRequest(PATH_PRODUCTS_DIFF + "?leftProduct=" + p1 + "&rightProduct=" + p2).get();
        assertEquals(200, response.getStatus());

        checkExpectedResponse(response, "diff");
    }

    private void checkExpectedResponse(Response response, String expectedFile) throws IOException {
        File expectedResponseFile = getJsonResponseFile(PATH_FILES_PRODUCTS, expectedFile);
        final String expected = readFileToString(expectedResponseFile);
        final String actual = response.readEntity(String.class);
        System.out.println("Expected: " + expected);
        System.out.println("Actual: " + actual);
        assertEqualsJson(expected, actual);
    }

    @Override
    protected void assertEqualsJson(String expected, String actual) {
        try {
            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        } catch (JSONException ex) {
            fail("The test wasn't able to compare JSON strings" + ex);
        }
    }
}
