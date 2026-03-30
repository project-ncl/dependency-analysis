package org.jboss.da.test.client.rest.listings;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.logging.LogRecord;

import jakarta.ws.rs.core.Response;

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.quarkus.test.LogCollectingTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(value = H2DatabaseTestResource.class, restrictToAnnotatedClass = true)
@QuarkusTestResource(
        value = LogCollectingTestResource.class,
        restrictToAnnotatedClass = true,
        initArgs = @ResourceArg(name = LogCollectingTestResource.LEVEL, value = "FINE"))
public class RestApiListingsTestIT extends AbstractRestApiListingTest {

    @Test
    public void testAddBlackArtifact() throws Exception {
        Response response = manipulateEntityFile(OperationType.POST, "gav");

        checkExpectedResponse(response, "success");

        List<LogRecord> logRecords = LogCollectingTestResource.current().getRecords();
        assertTrue(
                logRecords.stream()
                        .anyMatch(
                                r -> LogCollectingTestResource.format(r)
                                        .contains("Looking for user with name testUser")));

    }

    @Test
    public void testDeleteBlackArtifact() throws Exception {
        // add artifact
        manipulateEntityFile(OperationType.POST, "gav").close();

        // delete artifact
        Response response = manipulateEntityFile(OperationType.DELETE, "gav");

        checkExpectedResponse(response, "success");
    }

    @Test
    public void shouldBlackListSpecificRedhatBuild() throws Exception {
        String g = "org.jboss.da";
        String a = "dependency-analyzer";
        String v = "0.3.0";
        manipulateEntityFile(OperationType.POST, "gavRh").close();

        Response response = getBlacklistedGAV(g, a, v);
        assertEquals(404, response.getStatus());
        response = getBlacklistedGAV(g, a, v + ".redhat-2");
        assertEquals(404, response.getStatus());
        response = getBlacklistedGAV(g, a, v + ".redhat-1");
        assertEquals(200, response.getStatus());
        checkExpectedResponse(response, "gavRhNonOSGiResponse");

    }

    @Test
    public void shouldBlacklistWholeVersions() throws Exception { //
        String g = "org.jboss.da";
        String a = "dependency-analyzer";
        String v = "0.3.0";
        manipulateEntityFile(OperationType.POST, "gav").close();

        Response response = getBlacklistedGAV(g, a, v + "-redhat-1");
        assertEquals(200, response.getStatus());
        checkExpectedResponse(response, "gavNonRhResponse");
        response = getBlacklistedGAV(g, a, v);
        assertEquals(200, response.getStatus());
        checkExpectedResponse(response, "gavNonRhResponse");
        response = getBlacklistedGAV(g, a, v + "-redhat-2");
        assertEquals(200, response.getStatus());
        checkExpectedResponse(response, "gavNonRhResponse");
    }

    @Test
    public void shouldUnBlacklistWithoutAndWithRHSuffix() throws Exception {
        String g = "org.jboss.da";
        String a = "dependency-analyzer";
        String v = "0.3.0";
        manipulateEntityFile(OperationType.POST, "gavRh").close();
        manipulateEntityFile(OperationType.POST, "gav").close();
        Response response = getBlacklistedGAV(g, a, v);
        checkExpectedResponse(response, "gavNonRhResponse");
        response = getBlacklistedGAV(g, a, v + "-redhat-1");
        checkExpectedResponse(response, "gavNonRhResponse");
        response = getBlacklistedGAV(g, a, v + "-redhat-2");
        checkExpectedResponse(response, "gavNonRhResponse");

        manipulateEntityFile(OperationType.DELETE, "gav").close();
        response = getBlacklistedGAV(g, a, v);
        assertEquals(404, response.getStatus());
        response = getBlacklistedGAV(g, a, v + ".redhat-2");
        assertEquals(404, response.getStatus());
        response = getBlacklistedGAV(g, a, v + ".redhat-1");
        assertEquals(200, response.getStatus());
        checkExpectedResponse(response, "gavRhNonOSGiResponse");

        manipulateEntityFile(OperationType.DELETE, "gavRh").close();
        response = getBlacklistedGAV(g, a, v);
        assertEquals(404, response.getStatus());
        response = getBlacklistedGAV(g, a, v + ".redhat-2");
        assertEquals(404, response.getStatus());
        response = getBlacklistedGAV(g, a, v + ".redhat-1");
        assertEquals(404, response.getStatus());
    }

    @Test
    public void testDeleteNonExistingBlackArtifact() throws Exception {
        Response response = manipulateEntityFile(OperationType.DELETE, "gav");

        checkExpectedResponse(response, "successFalse");
    }

    @Test
    public void testAlreadyAddedBlackArtifact() throws Exception {
        // add first black artifact
        manipulateEntityFile(OperationType.POST, "gav").close();

        // add second black artifact
        Response response = manipulateEntityFile(OperationType.POST, "gav");

        checkExpectedResponse(response, "successFalse");
    }

    @Test
    public void testGetAllBlackArtifacts() throws Exception {
        // Add artifacts to blacklist
        manipulateEntityFile(OperationType.POST, "gav").close();

        manipulateEntityFile(OperationType.POST, "gav2").close();

        // Get list
        Response response = createClientRequest(PATH_BLACK_LIST).get();
        assertEquals(200, response.getStatus());

        checkExpectedResponse(response, "gavBlackList");
    }

    @Test
    public void testGetGABlackArtifacts() throws Exception {
        // Check empty list
        checkExpectedResponse(getBlacklisted("foo", "bar"), "gaBlacklistEmpty");

        // Add artifacts
        manipulateEntityFile(OperationType.POST, "gavFoobar").close();
        manipulateEntityFile(OperationType.POST, "gavFoobaz-1").close();
        manipulateEntityFile(OperationType.POST, "gavFoobaz-2").close();
        manipulateEntityFile(OperationType.POST, "gavFoobarbaz").close();
        manipulateEntityFile(OperationType.POST, "gavFoobarbaz-4").close();

        // Check responses
        checkExpectedResponse(getBlacklisted("foo", "bar"), "gaBlacklistFoobar");
        checkExpectedResponse(getBlacklisted("foo", "baz"), "gaBlacklistFoobaz");
        checkExpectedResponse(getBlacklisted("foo", "bar-baz"), "gaBlacklistFoobarbaz");
    }

    @Test
    public void testCheckRHBlackArtifact() throws Exception {
        manipulateEntityFile(OperationType.POST, "gav").close();

        Response response = getBlacklisted("org.jboss.da", "dependency-analyzer", "0.3.0.redhat-1");

        checkExpectedResponse(response, "gavResponse");
    }

    private Response getBlacklistedGAV(String groupId, String artifactId, String version) {
        return createClientRequest(
                PATH_BLACK_LISTINGS_GAV + "?groupid=" + groupId + "&artifactid=" + artifactId + "&version=" + version)
                .get();
    }

    private Response getBlacklisted(String groupId, String artifactId, String version) {
        Response response = createClientRequest(
                PATH_BLACK_LIST + "?groupid=" + groupId + "&artifactid=" + artifactId + "&version=" + version).get();
        assertEquals(200, response.getStatus());
        return response;
    }

    private Response getBlacklisted(String groupId, String artifactId) {
        Response response = createClientRequest(
                PATH_BLACK_LISTINGS_GA + "?groupid=" + groupId + "&artifactid=" + artifactId).get();
        assertEquals(200, response.getStatus());
        return response;
    }

    /**
     * Non RedHat but OSGi compliant black artifact test
     */
    @Test
    public void testCheckNonRHBlackArtifact() throws Exception {
        manipulateEntityFile(OperationType.POST, "gav").close();

        Response response = getBlacklisted("org.jboss.da", "dependency-analyzer", "0.3.0");

        checkExpectedResponse(response, "gavResponse");
    }

    /**
     * Non RedHat non OSGi compliant black artifact test
     */
    @Test
    public void testCheckNonRHNonOSGiBlackArtifact() throws Exception {
        manipulateEntityFile(OperationType.POST, "gav").close();

        Response response = getBlacklisted("org.jboss.da", "dependency-analyzer", "0.3");

        checkExpectedResponse(response, "gavResponse");
    }

    @Override
    protected void assertEqualsJson(String expected, String actual) {
        try {

            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        } catch (JSONException ex) {
            fail("The test wasn't able to compare JSON strings" + ex);
        }
    }

    private void checkExpectedResponse(Response response, String expectedFile) throws IOException {
        File expectedResponseFile = getJsonResponseFile(PATH_FILES_LISTINGS_GAV, expectedFile);
        assertEqualsJson(
                readFileToString(expectedResponseFile, Charset.defaultCharset()),
                response.readEntity(String.class));
        response.close();
    }
}
