package org.jboss.da.model.rest;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;

import org.jboss.da.listings.model.ProductSupportStatus;
import org.jboss.da.listings.model.rest.RestArtifact;
import org.jboss.da.listings.model.rest.RestProductGAV;
import org.junit.Test;

import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.json.JSONException;
import org.jboss.da.listings.model.rest.ContainsResponse;
import org.jboss.da.listings.model.rest.RestProduct;
import org.jboss.da.listings.model.rest.RestProductArtifact;
import org.jboss.da.listings.model.rest.RestProductInput;
import org.jboss.da.listings.model.rest.SuccessResponse;
import org.jboss.da.listings.model.rest.WLFill;
import org.jboss.da.products.model.rest.GADiff;
import org.jboss.da.reports.model.request.GAVRequest;
import org.jboss.da.reports.model.response.LookupReport;
import org.jboss.da.reports.model.response.Report;
import static org.junit.Assert.fail;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 *
 * @author Stanislav Knot <sknot@redhat.com>
 */
public class BackwardCompatibilityTest {

    private static final String EXPECETD_PATH = "src/test/resources/backwardCompatibilityTest";

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testRestProductGAV() throws IOException {
        RestProductGAV restProdGav = new RestProductGAV();
        restProdGav.setName("name");
        RestArtifact gav = new RestArtifact();
        gav.setArtifactId("id");
        gav.setGroupId("gid");
        gav.setVersion("ver");
        restProdGav.setGav(gav);
        restProdGav.setSupportStatus(ProductSupportStatus.SUPPORTED);
        restProdGav.setVersion("ver");

        compare(restProdGav, "RestProductGAV");
    }

    @Test
    public void testWLFill() throws IOException {
        WLFill wlfill = new WLFill();
        wlfill.setPomPath("./pom.xml");
        wlfill.setRevision("master");
        wlfill.setScmUrl("git@github.com:project-ncl/dependency-analysis.git");
        StringWriter actual = new StringWriter();

        compare(wlfill, "WLFill");
    }

    @Test
    public void testRestProductArtifact() throws IOException {
        RestProductArtifact restProdArtifact = new RestProductArtifact();
        restProdArtifact.setGroupId("g");
        restProdArtifact.setArtifactId("a");
        restProdArtifact.setVersion("v");
        restProdArtifact.setProductId(0);

        compare(restProdArtifact, "RestProductArtifact");
    }

    @Test
    public void testRestProductInput() throws IOException {
        RestProductInput restProdInput = new RestProductInput();
        restProdInput.setName("name");
        restProdInput.setSupportStatus(ProductSupportStatus.SUPPORTED);
        restProdInput.setVersion("v");

        compare(restProdInput, "RestProductInput");
    }

    @Test
    public void testContainsResponse() throws IOException {
        ContainsResponse containsResponse = new ContainsResponse();
        containsResponse.setContains(true);
        RestArtifact gav = new RestArtifact();
        gav.setArtifactId("id");
        gav.setGroupId("gid");
        gav.setVersion("ver");
        ArrayList list = new ArrayList<>();
        list.add(gav);
        containsResponse.setFound(list);

        compare(containsResponse, "ContainsResponse");
    }

    @Test
    public void testSuccessResponse() throws IOException {
        SuccessResponse succResponse = new SuccessResponse();
        succResponse.setMessage("Hello World!");
        succResponse.setSuccess(true);

        compare(succResponse, "SuccessResponse");
    }

    @Test
    public void testRestProduct() throws IOException {
        RestProduct restProduct = new RestProduct();
        restProduct.setName("name");
        restProduct.setId(5L);
        restProduct.setVersion("ver");
        restProduct.setSupportStatus(ProductSupportStatus.SUPPORTED);

        compare(restProduct, "RestProduct");
    }

    @Test
    public void testRestArtifact() throws IOException {
        RestArtifact restArtifact = new RestArtifact();
        restArtifact.setGroupId("g");
        restArtifact.setArtifactId("a");
        restArtifact.setVersion("v");

        compare(restArtifact, "RestArtifact");
    }

    @Test
    public void testGAV() throws IOException {
        GAV gav = new GAV("g", "a", "v");

        compare(gav, "GAV");
    }

    @Test
    public void testGADiff() throws IOException {
        GADiff gadiff = new GADiff(new GA("g", "a"), "1.0.0", "1.0.1", "MICRO");

        compare(gadiff, "GADiff");
    }

    @Test
    public void testGAVRequest() throws IOException {
        GAVRequest gavRequest = new GAVRequest("g", "a", "v", new HashSet<>(), new HashSet<>());

        compare(gavRequest, "GAVRequest");
    }

    @Test
    public void testLookupReport() throws IOException {
        LookupReport lookupReport = new LookupReport(
                new GAV("g", "a", "v"),
                "ver",
                new ArrayList<>(),
                true,
                new ArrayList<>());

        compare(lookupReport, "LookupReport");
    }

    @Test
    public void testReport() throws IOException {
        Report report = new Report(
                new GAV("g", "a", "v"),
                new ArrayList<>(),
                "4.3",
                true,
                new ArrayList<>(),
                true,
                new ArrayList<>(),
                0);

        compare(report, "Report");
    }

    protected void assertEqualsJson(String expected, String actual) {
        try {
            JSONAssert.assertEquals(expected, actual, JSONCompareMode.NON_EXTENSIBLE);
        } catch (JSONException ex) {
            fail("The test wasn't able to compare JSON strings" + ex);
        }
    }

    private void compare(Object obj, String expectedFile) throws IOException {
        StringWriter actual = new StringWriter();
        mapper.writeValue(actual, obj);
        Path expectedResponseFile = getJsonResponseFile(EXPECETD_PATH, expectedFile);
        String expected = Files.lines(expectedResponseFile).collect(Collectors.joining());
        assertEqualsJson(expected, actual.toString());
    }

    protected Path getJsonResponseFile(String path, String variant) {
        return Paths.get(path, variant + ".json");
    }

}
