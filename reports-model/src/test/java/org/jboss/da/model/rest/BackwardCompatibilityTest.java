package org.jboss.da.model.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import static org.apache.commons.io.FileUtils.readFileToString;
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
import org.jboss.da.reports.model.rest.GAVRequest;
import org.jboss.da.reports.model.rest.LookupReport;
import org.jboss.da.reports.model.rest.Product;
import org.jboss.da.reports.model.rest.Report;
import static org.junit.Assert.fail;

/**
 *
 * @author Stanislav Knot <sknot@redhat.com>
 */
public class BackwardCompatibilityTest {

    private ObjectMapper mapper = new ObjectMapper();

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

        StringWriter actual = new StringWriter();
        mapper.writeValue(actual, restProdGav);
        File expectedResponseFile = getJsonResponseFile(
                "src/test/resources/backwardCompatibilityTest", "RestProductGAV");
        assertEqualsJson(actual.toString(), readFileToString(expectedResponseFile));
    }

    @Test
    public void testWLFill() throws IOException {
        WLFill wlfill = new WLFill();
        wlfill.setPomPath("./pom.xml");
        wlfill.setRevision("master");
        wlfill.setScmUrl("git@github.com:project-ncl/dependency-analysis.git");
        StringWriter actual = new StringWriter();

        mapper.writeValue(actual, wlfill);
        File expectedResponseFile = getJsonResponseFile(
                "src/test/resources/backwardCompatibilityTest", "WLFill");
        assertEqualsJson(actual.toString(), readFileToString(expectedResponseFile));
    }

    @Test
    public void testRestProductArtifact() throws IOException {
        RestProductArtifact restProdArtifact = new RestProductArtifact();
        restProdArtifact.setGroupId("g");
        restProdArtifact.setArtifactId("a");
        restProdArtifact.setVersion("v");
        restProdArtifact.setProductId(0);
        StringWriter actual = new StringWriter();

        mapper.writeValue(actual, restProdArtifact);
        File expectedResponseFile = getJsonResponseFile(
                "src/test/resources/backwardCompatibilityTest", "RestProductArtifact");
        assertEqualsJson(actual.toString(), readFileToString(expectedResponseFile));
    }

    @Test
    public void testRestProductInput() throws IOException {
        RestProductInput restProdInput = new RestProductInput();
        restProdInput.setName("name");
        restProdInput.setSupportStatus(ProductSupportStatus.SUPPORTED);
        restProdInput.setVersion("v");
        StringWriter actual = new StringWriter();

        mapper.writeValue(actual, restProdInput);
        System.out.println("act " + actual.toString());
        File expectedResponseFile = getJsonResponseFile(
                "src/test/resources/backwardCompatibilityTest", "RestProductInput");
        assertEqualsJson(actual.toString(), readFileToString(expectedResponseFile));
    }

    @Test
    public void testContainsResponse() throws IOException {
        ContainsResponse containsResponse = new ContainsResponse();
        containsResponse.setContains(true);
        RestArtifact gav = new RestArtifact();
        gav.setArtifactId("id");
        gav.setGroupId("gid");
        gav.setVersion("ver");
        ArrayList list = new ArrayList<RestArtifact>();
        list.add(gav);
        containsResponse.setFound(list);
        StringWriter actual = new StringWriter();

        mapper.writeValue(actual, containsResponse);
        File expectedResponseFile = getJsonResponseFile(
                "src/test/resources/backwardCompatibilityTest", "ContainsResponse");
        assertEqualsJson(actual.toString(), readFileToString(expectedResponseFile));
    }

    @Test
    public void testSuccessResponse() throws IOException {
        SuccessResponse succResponse = new SuccessResponse();
        succResponse.setMessage("Hello World!");
        succResponse.setSuccess(true);
        StringWriter actual = new StringWriter();

        mapper.writeValue(actual, succResponse);
        File expectedResponseFile = getJsonResponseFile(
                "src/test/resources/backwardCompatibilityTest", "SuccessResponse");
        assertEqualsJson(actual.toString(), readFileToString(expectedResponseFile));
    }

    @Test
    public void testRestProduct() throws IOException {
        RestProduct restProduct = new RestProduct();
        restProduct.setName("name");
        restProduct.setId(5L);
        restProduct.setVersion("ver");
        restProduct.setSupportStatus(ProductSupportStatus.SUPPORTED);

        StringWriter actual = new StringWriter();
        mapper.writeValue(actual, restProduct);
        File expectedResponseFile = getJsonResponseFile(
                "src/test/resources/backwardCompatibilityTest", "RestProduct");
        assertEqualsJson(actual.toString(), readFileToString(expectedResponseFile));
    }

    @Test
    public void testRestArtifact() throws IOException {
        RestArtifact restArtifact = new RestArtifact();
        restArtifact.setGroupId("g");
        restArtifact.setArtifactId("a");
        restArtifact.setVersion("v");

        StringWriter actual = new StringWriter();
        mapper.writeValue(actual, restArtifact);
        File expectedResponseFile = getJsonResponseFile(
                "src/test/resources/backwardCompatibilityTest", "RestArtifact");
        assertEqualsJson(actual.toString(), readFileToString(expectedResponseFile));
    }

    @Test
    public void testGAV() throws IOException {
        GAV gav = new GAV("g", "a", "v");
        StringWriter actual = new StringWriter();
        mapper.writeValue(actual, gav);

        File expectedResponseFile = getJsonResponseFile(
                "src/test/resources/backwardCompatibilityTest", "GAV");
        assertEqualsJson(actual.toString(), readFileToString(expectedResponseFile));
    }

    @Test
    public void testGADiff() throws IOException {
        GADiff gadiff = new GADiff(new GA("g", "a"), "1.0.0", "1.0.1");
        StringWriter actual = new StringWriter();
        mapper.writeValue(actual, gadiff);

        File expectedResponseFile = getJsonResponseFile(
                "src/test/resources/backwardCompatibilityTest", "GADiff");
        assertEqualsJson(actual.toString(), readFileToString(expectedResponseFile));
    }

    @Test
    public void testGAVRequest() throws IOException {
        GAVRequest gavRequest = new GAVRequest("g", "a", "v", new HashSet<String>(),
                new HashSet<Long>());
        StringWriter actual = new StringWriter();
        mapper.writeValue(actual, gavRequest);

        File expectedResponseFile = getJsonResponseFile(
                "src/test/resources/backwardCompatibilityTest", "GAVRequest");
        assertEqualsJson(actual.toString(), readFileToString(expectedResponseFile));
    }

    @Test
    public void testLookupReport() throws IOException {
        LookupReport lookupReport = new LookupReport(new GAV("g", "a", "v"), "ver",
                new ArrayList<String>(), true, new ArrayList<RestProductInput>());
        StringWriter actual = new StringWriter();
        mapper.writeValue(actual, lookupReport);

        File expectedResponseFile = getJsonResponseFile(
                "src/test/resources/backwardCompatibilityTest", "LookupReport");
        assertEqualsJson(actual.toString(), readFileToString(expectedResponseFile));
    }

    @Test
    public void testProduct() throws IOException {
        Product prod = new Product("EAP", "4.2");

        StringWriter actual = new StringWriter();
        mapper.writeValue(actual, prod);

        File expectedResponseFile = getJsonResponseFile(
                "src/test/resources/backwardCompatibilityTest", "Product");
        assertEqualsJson(actual.toString(), readFileToString(expectedResponseFile));
    }

    @Test
    public void testReport() throws IOException {
        Report report = new Report(new GAV("g", "a", "v"), new ArrayList<String>(), "4.3", true,
                new ArrayList<Report>(), true, new ArrayList<RestProductInput>(), 0);
        StringWriter actual = new StringWriter();
        mapper.writeValue(actual, report);

        System.out.println("actu " + actual);
        File expectedResponseFile = getJsonResponseFile(
                "src/test/resources/backwardCompatibilityTest", "Report");
        assertEqualsJson(actual.toString(), readFileToString(expectedResponseFile));
    }

    protected void assertEqualsJson(String expected, String actual) {
        try {
            JSONAssert.assertEquals(expected, actual, JSONCompareMode.NON_EXTENSIBLE);
        } catch (JSONException ex) {
            fail("The test wasn't able to compare JSON strings" + ex);
        }
    }

    protected File getJsonResponseFile(String path, String variant) {
        return new ExpectedResponseFilenameBuilder(Paths.get(""), path, variant).getFile();
    }

    protected static class ExpectedResponseFilenameBuilder {

        protected static final String DEFAULT_VARIANT = "";

        private final Path folder;

        private final String path;

        private final String variant;

        public ExpectedResponseFilenameBuilder(Path folder, String path, String variant) {
            this.folder = folder;
            this.path = path;
            this.variant = variant;
        }

        public File getFile() {
            return getPath().toFile();
        }

        public Path getPath() {
            return folder.resolve(convertPath(path) + convertVariant(variant) + ".json");
        }

        private String convertVariant(String path) {
            return variant.equals(DEFAULT_VARIANT) ? "" : "/" + variant;
        }

        private String convertPath(String path) {
            String convertedPath = path.endsWith("/") ? path.substring(0, path.length() - 1)
                    + "index" : path;
            return convertedPath.startsWith("/") ? convertedPath.substring(1) : convertedPath;
        }

    }

}
