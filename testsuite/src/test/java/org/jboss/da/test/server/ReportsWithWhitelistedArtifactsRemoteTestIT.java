package org.jboss.da.test.server;

import static org.junit.Assert.*;

import org.apache.maven.scm.ScmException;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.common.auth.impl.JAASAuthenticatorService;
import org.jboss.da.communication.indy.FindGAVDependencyException;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.listings.api.dao.ProductVersionDAO;
import org.jboss.da.listings.api.model.ProductVersion;
import org.jboss.da.listings.api.service.ProductService;
import org.jboss.da.listings.api.service.WhiteArtifactService;
import org.jboss.da.listings.model.ProductSupportStatus;
import org.jboss.da.listings.model.rest.RestProductArtifact;
import org.jboss.da.model.rest.GAV;
import org.jboss.da.reports.api.ArtifactReport;
import org.jboss.da.reports.api.ReportsGenerator;
import org.jboss.da.reports.model.api.SCMLocator;
import org.jboss.da.reports.model.request.GAVRequest;
import org.jboss.da.reports.model.request.LookupGAVsRequest;
import org.jboss.da.reports.model.request.SCMReportRequest;
import org.jboss.da.reports.model.response.LookupReport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 *
 * @author fkujikis
 */
@RunWith(Arquillian.class)
public class ReportsWithWhitelistedArtifactsRemoteTestIT extends AbstractServerTest {

    @Inject
    private ReportsGenerator reportGenerator;

    @Inject
    private WhiteArtifactService whiteService;

    @Inject
    private ProductVersionDAO productVersionDao;

    @Inject
    private ProductService productService;

    private ProductVersion prodVer1_1;

    private RestProductArtifact whiteArtifact1_1_1;

    private RestProductArtifact whiteArtifact1_1_2;

    private ProductVersion prodVer1_2;

    private RestProductArtifact whiteArtifact1_2_1;

    private RestProductArtifact whiteArtifact1_2_2;

    private ProductVersion prodVer2_1;

    private RestProductArtifact whiteArtifact2_1_a;

    private RestProductArtifact whiteArtifact2_1_b;

    private RestProductArtifact whiteArtifact2_1_c;

    private GAV hibernateGav;

    private GAV daGav;

    private GAV jacksonGav;

    @Before
    public void workaroundNoHttpResponseException() throws InterruptedException {
        Thread.sleep(2000);
    }

    @Before
    public void setUp() {
        JAASAuthenticatorService.setUser("user");

        whiteArtifact1_1_1 = new RestProductArtifact();
        whiteArtifact1_1_1.setGroupId("org.jboss.da");
        whiteArtifact1_1_1.setArtifactId("common");
        whiteArtifact1_1_1.setVersion("0.1.0-redhat-1");

        whiteArtifact1_1_2 = new RestProductArtifact();
        whiteArtifact1_1_2.setGroupId("org.hibernate");
        whiteArtifact1_1_2.setArtifactId("hibernate-core");
        whiteArtifact1_1_2.setVersion("4.2.28.Final-redhat-1");

        whiteArtifact1_2_1 = new RestProductArtifact();
        whiteArtifact1_2_1.setGroupId("org.jboss.da");
        whiteArtifact1_2_1.setArtifactId("common");
        whiteArtifact1_2_1.setVersion("0.1.1-redhat-1");

        whiteArtifact1_2_2 = new RestProductArtifact();
        whiteArtifact1_2_2.setGroupId("org.hibernate");
        whiteArtifact1_2_2.setArtifactId("hibernate-core");
        whiteArtifact1_2_2.setVersion("4.2.29.Final-redhat-1");

        productService.addProduct("Product1", "1.0.0", ProductSupportStatus.UNKNOWN);
        prodVer1_1 = productVersionDao.findProductVersion("Product1", "1.0.0").get();
        productService.addProduct("Product1", "2.0.0", ProductSupportStatus.UNKNOWN);
        prodVer1_2 = productVersionDao.findProductVersion("Product1", "2.0.0").get();

        whiteService.addArtifact(
                whiteArtifact1_1_1.getGroupId(),
                whiteArtifact1_1_1.getArtifactId(),
                whiteArtifact1_1_1.getVersion(),
                prodVer1_1.getId());
        whiteService.addArtifact(
                whiteArtifact1_1_2.getGroupId(),
                whiteArtifact1_1_2.getArtifactId(),
                whiteArtifact1_1_2.getVersion(),
                prodVer1_1.getId());

        whiteService.addArtifact(
                whiteArtifact1_2_1.getGroupId(),
                whiteArtifact1_2_1.getArtifactId(),
                whiteArtifact1_2_1.getVersion(),
                prodVer1_2.getId());
        whiteService.addArtifact(
                whiteArtifact1_2_2.getGroupId(),
                whiteArtifact1_2_2.getArtifactId(),
                whiteArtifact1_2_2.getVersion(),
                prodVer1_2.getId());

        whiteArtifact2_1_a = new RestProductArtifact();
        whiteArtifact2_1_a.setGroupId("org.jboss.da");
        whiteArtifact2_1_a.setArtifactId("common");
        whiteArtifact2_1_a.setVersion("0.2.0-redhat-1");

        whiteArtifact2_1_b = new RestProductArtifact();
        whiteArtifact2_1_b.setGroupId("com.fasterxml.jackson.core");
        whiteArtifact2_1_b.setArtifactId("jackson-databind");
        whiteArtifact2_1_b.setVersion("2.4.4-redhat-1");

        whiteArtifact2_1_c = new RestProductArtifact();
        whiteArtifact2_1_c.setGroupId("org.hibernate");
        whiteArtifact2_1_c.setArtifactId("hibernate-core");
        whiteArtifact2_1_c.setVersion("4.2.30.Final-redhat-1");

        productService.addProduct("Product2", "1.0.0", ProductSupportStatus.UNKNOWN);
        prodVer2_1 = productVersionDao.findProductVersion("Product2", "1.0.0").get();

        whiteService.addArtifact(
                whiteArtifact2_1_a.getGroupId(),
                whiteArtifact2_1_a.getArtifactId(),
                whiteArtifact2_1_a.getVersion(),
                prodVer2_1.getId());
        whiteService.addArtifact(
                whiteArtifact2_1_b.getGroupId(),
                whiteArtifact2_1_b.getArtifactId(),
                whiteArtifact2_1_b.getVersion(),
                prodVer2_1.getId());
        whiteService.addArtifact(
                whiteArtifact2_1_c.getGroupId(),
                whiteArtifact2_1_c.getArtifactId(),
                whiteArtifact2_1_c.getVersion(),
                prodVer2_1.getId());

        hibernateGav = artifactToGAV(whiteArtifact1_1_2);
        daGav = artifactToGAV(whiteArtifact1_1_1);
        jacksonGav = artifactToGAV(whiteArtifact2_1_b);
    }

    @After
    public void tearDown() {

        // Remove artifacts
        whiteService.removeArtifact(
                whiteArtifact1_1_1.getGroupId(),
                whiteArtifact1_1_1.getArtifactId(),
                whiteArtifact1_1_1.getVersion());
        whiteService.removeArtifact(
                whiteArtifact1_1_2.getGroupId(),
                whiteArtifact1_1_2.getArtifactId(),
                whiteArtifact1_1_2.getVersion());
        whiteService.removeArtifact(
                whiteArtifact1_2_1.getGroupId(),
                whiteArtifact1_2_1.getArtifactId(),
                whiteArtifact1_2_1.getVersion());
        whiteService.removeArtifact(
                whiteArtifact1_2_2.getGroupId(),
                whiteArtifact1_2_2.getArtifactId(),
                whiteArtifact1_2_2.getVersion());

        whiteService.removeArtifact(
                whiteArtifact2_1_a.getGroupId(),
                whiteArtifact2_1_a.getArtifactId(),
                whiteArtifact2_1_a.getVersion());
        whiteService.removeArtifact(
                whiteArtifact2_1_b.getGroupId(),
                whiteArtifact2_1_b.getArtifactId(),
                whiteArtifact2_1_b.getVersion());
        whiteService.removeArtifact(
                whiteArtifact2_1_c.getGroupId(),
                whiteArtifact2_1_c.getArtifactId(),
                whiteArtifact2_1_c.getVersion());

        // Remove products
        productService.removeProduct("Product1", "1.0.0");
        productService.removeProduct("Product1", "2.0.0");
        productService.removeProduct("Product2", "1.0.0");

        JAASAuthenticatorService.setUser(null);
    }

    // This allows to test:
    // - whitelist lookup by product
    // - whitelist lookup by product version
    // - whitelist lookup without either (get all - 3 additions to top level)
    // - whitelist addition to top level
    // - whitelist addition to top-1 level

    private SCMReportRequest getDefaultSCMRequest() {
        SCMLocator locator = SCMLocator
                .generic("https://github.com/project-ncl/dependency-analysis.git", "05a878", "common");

        SCMReportRequest request = new SCMReportRequest();
        request.setScml(locator);

        return request;
    }

    @Test
    public void testScmAllWhiteArtifactsPresent() throws ScmException, PomAnalysisException, CommunicationException {

        SCMReportRequest request = getDefaultSCMRequest();

        Optional<ArtifactReport> ar = reportGenerator.getReportFromSCM(request);

        // Ensure we got an artifact report back
        assertTrue(ar.isPresent());
        ArtifactReport artifactReport = ar.get();

        // Ensure all dependencies from whitelist are present
        assertTrue(artifactReport.getAvailableVersions().contains(whiteArtifact1_1_1.getVersion()));
        assertTrue(artifactReport.getAvailableVersions().contains(whiteArtifact1_2_1.getVersion()));
        assertTrue(artifactReport.getAvailableVersions().contains(whiteArtifact2_1_a.getVersion()));

        // These have different GA so shouldn't be there
        assertFalse(artifactReport.getAvailableVersions().contains(whiteArtifact1_1_2.getVersion()));
        assertFalse(artifactReport.getAvailableVersions().contains(whiteArtifact1_2_2.getVersion()));
        assertFalse(artifactReport.getAvailableVersions().contains(whiteArtifact2_1_b.getVersion()));
        assertFalse(artifactReport.getAvailableVersions().contains(whiteArtifact2_1_c.getVersion()));

        // Find jackson artifactReport and check it contains the jackson version
        Optional<ArtifactReport> dependencyJacksonDatabind = artifactReport.getDependencies()
                .stream()
                .filter(
                        x -> x.getArtifactId().equals(whiteArtifact2_1_b.getArtifactId())
                                && x.getGroupId().equals(whiteArtifact2_1_b.getGroupId()))
                .findFirst();

        assertTrue(dependencyJacksonDatabind.isPresent());
        assertTrue(dependencyJacksonDatabind.get().getAvailableVersions().contains(whiteArtifact2_1_b.getVersion()));
    }

    @Test
    public void testScmWhiteArtifactsPresentForDependenciesWithSingleProduct()
            throws ScmException, PomAnalysisException, CommunicationException {

        SCMReportRequest request = getDefaultSCMRequest();

        // Only use artifacts from the single product Product2 : version "1.0.0"
        request.getProductNames().add(prodVer2_1.getProduct().getName());

        Optional<ArtifactReport> ar = reportGenerator.getReportFromSCM(request);

        assertTrue(ar.isPresent());
        ArtifactReport artifactReport = ar.get();

        // Artifacts from Product2 version "1.0.0" expected
        assertFalse(artifactReport.getAvailableVersions().contains(whiteArtifact1_1_1.getVersion()));
        assertFalse(artifactReport.getAvailableVersions().contains(whiteArtifact1_2_1.getVersion()));
        assertTrue(artifactReport.getAvailableVersions().contains(whiteArtifact2_1_a.getVersion()));
        assertFalse(artifactReport.getAvailableVersions().contains(whiteArtifact2_1_b.getVersion()));

        // Find jackson artifactReport and check it contains the jackson version
        Optional<ArtifactReport> dependencyJacksonDatabind = artifactReport.getDependencies()
                .stream()
                .filter(
                        x -> x.getArtifactId().equals(whiteArtifact2_1_b.getArtifactId())
                                && x.getGroupId().equals(whiteArtifact2_1_b.getGroupId()))
                .findFirst();

        assertTrue(dependencyJacksonDatabind.isPresent());
        assertTrue(dependencyJacksonDatabind.get().getAvailableVersions().contains(whiteArtifact2_1_b.getVersion()));

    }

    @Test
    public void testScmWhiteArtifactsPresentForSingleProduct()
            throws ScmException, PomAnalysisException, CommunicationException {

        SCMReportRequest request = getDefaultSCMRequest();
        // Only use artifacts from the single product Product1 : versions "1.0.0" and "2.0.0" should be included
        request.getProductNames().add(prodVer1_1.getProduct().getName());

        Optional<ArtifactReport> ar = reportGenerator.getReportFromSCM(request);

        assertTrue(ar.isPresent());
        ArtifactReport artifactReport = ar.get();

        // Artifacts from Product1 (product versions "1.0.0", "2.0.0" expected)
        assertTrue(artifactReport.getAvailableVersions().contains(whiteArtifact1_1_1.getVersion()));
        assertTrue(artifactReport.getAvailableVersions().contains(whiteArtifact1_2_1.getVersion()));
        assertFalse(artifactReport.getAvailableVersions().contains(whiteArtifact2_1_a.getVersion()));
        assertFalse(artifactReport.getAvailableVersions().contains(whiteArtifact2_1_b.getVersion()));
    }

    @Test
    public void testScmWhiteArtifactsPresentForSingleProductVersion()
            throws ScmException, PomAnalysisException, CommunicationException {

        SCMReportRequest request = getDefaultSCMRequest();
        // Only use artifacts from the single product version : "Product1", "1.0.0"
        request.getProductVersionIds().add(prodVer1_1.getId());

        Optional<ArtifactReport> ar = reportGenerator.getReportFromSCM(request);

        assertTrue(ar.isPresent());
        ArtifactReport artifactReport = ar.get();

        // Artifacts from the single product version : "Product1", "1.0.0" : expect 1 only
        assertTrue(artifactReport.getAvailableVersions().contains(whiteArtifact1_1_1.getVersion()));
        assertFalse(artifactReport.getAvailableVersions().contains(whiteArtifact1_2_1.getVersion()));
        assertFalse(artifactReport.getAvailableVersions().contains(whiteArtifact2_1_a.getVersion()));
        assertFalse(artifactReport.getAvailableVersions().contains(whiteArtifact2_1_b.getVersion()));
    }

    private GAVRequest getDefaultGAVRequest() {
        Set<String> productNames = new HashSet<>();
        Set<Long> productVersionIds = new HashSet<>();
        GAVRequest gavRequest = new GAVRequest(
                "org.hibernate",
                "hibernate-core",
                "4.2.21.Final",
                productNames,
                productVersionIds);
        return gavRequest;
    }

    private GAV artifactToGAV(RestProductArtifact art) {
        return new GAV(art.getGroupId(), art.getArtifactId(), art.getVersion());
    }

    private LookupGAVsRequest getDefaultLookupRequest() {
        List<GAV> gavs = new ArrayList<>();
        gavs.add(hibernateGav);
        gavs.add(daGav);
        gavs.add(jacksonGav);

        LookupGAVsRequest req = new LookupGAVsRequest(gavs);

        return req;
    }

    @Test
    public void testGavsLookupAllWhiteArtifactsPresent() throws CommunicationException, FindGAVDependencyException {

        LookupGAVsRequest req = getDefaultLookupRequest();

        // Query all white artifacts from all products

        List<LookupReport> lookupReports = reportGenerator.getLookupReportsForGavs(req);

        assertTrue(lookupReports != null);
        assertEquals(3, lookupReports.size());

        // Expecting to see all whitelist versions included
        for (LookupReport lr : lookupReports) {
            if (hibernateGav.equals(lr.getGav())) {
                assertTrue(lr.getAvailableVersions().contains(whiteArtifact1_1_2.getVersion()));
                assertTrue(lr.getAvailableVersions().contains(whiteArtifact1_2_2.getVersion()));
                assertTrue(lr.getAvailableVersions().contains(whiteArtifact2_1_c.getVersion()));
                assertTrue(lr.getBestMatchVersion().equals(hibernateGav.getVersion()));
            } else if (daGav.equals(lr.getGav())) {
                assertTrue(lr.getAvailableVersions().contains(whiteArtifact1_1_1.getVersion()));
                assertTrue(lr.getAvailableVersions().contains(whiteArtifact1_2_1.getVersion()));
                assertTrue(lr.getAvailableVersions().contains(whiteArtifact2_1_a.getVersion()));
                assertTrue(lr.getBestMatchVersion().equals(daGav.getVersion()));
            } else if (jacksonGav.equals(lr.getGav())) {
                assertTrue(lr.getAvailableVersions().contains(whiteArtifact2_1_b.getVersion()));
                assertTrue(lr.getBestMatchVersion().equals(jacksonGav.getVersion()));
            }
        }
    }

    @Test
    public void testGavsLookupAllWhiteArtifactsPresentForSingleProduct()
            throws CommunicationException, FindGAVDependencyException {

        LookupGAVsRequest req = getDefaultLookupRequest();

        // Query all white artifacts from Product 1
        req.getProductNames().add(prodVer1_1.getProduct().getName());

        List<LookupReport> lookupReports = reportGenerator.getLookupReportsForGavs(req);

        assertTrue(lookupReports != null);
        assertEquals(3, lookupReports.size());

        // Expecting to see whitelist versions included for Product 1
        for (LookupReport lr : lookupReports) {
            if (hibernateGav.equals(lr.getGav())) {
                assertTrue(lr.getAvailableVersions().contains(whiteArtifact1_1_2.getVersion()));
                assertTrue(lr.getAvailableVersions().contains(whiteArtifact1_2_2.getVersion()));
                assertFalse(lr.getAvailableVersions().contains(whiteArtifact2_1_c.getVersion()));
                assertTrue(lr.getBestMatchVersion().equals(hibernateGav.getVersion()));
            } else if (daGav.equals(lr.getGav())) {
                assertTrue(lr.getAvailableVersions().contains(whiteArtifact1_1_1.getVersion()));
                assertTrue(lr.getAvailableVersions().contains(whiteArtifact1_2_1.getVersion()));
                assertFalse(lr.getAvailableVersions().contains(whiteArtifact2_1_a.getVersion()));
                assertTrue(lr.getBestMatchVersion().equals(daGav.getVersion()));
            } else if (jacksonGav.equals(lr.getGav())) {
                assertFalse(lr.getAvailableVersions().contains(whiteArtifact2_1_b.getVersion()));
                assertTrue(lr.getBestMatchVersion() == null);
            }
        }
    }

    @Test
    public void testGavsLookupAllWhiteArtifactsPresentForSingleProductVersion()
            throws CommunicationException, FindGAVDependencyException {

        LookupGAVsRequest req = getDefaultLookupRequest();

        // Query all white artifacts from Product 1 version 2
        req.getProductVersionIds().add(prodVer1_2.getId());

        List<LookupReport> lookupReports = reportGenerator.getLookupReportsForGavs(req);

        assertTrue(lookupReports != null);
        assertEquals(3, lookupReports.size());

        // Expecting to see whitelist versions included for Product 1 version 2
        for (LookupReport lr : lookupReports) {
            if (hibernateGav.equals(lr.getGav())) {
                assertFalse(lr.getAvailableVersions().contains(whiteArtifact1_1_2.getVersion()));
                assertTrue(lr.getAvailableVersions().contains(whiteArtifact1_2_2.getVersion()));
                assertFalse(lr.getAvailableVersions().contains(whiteArtifact2_1_c.getVersion()));
            } else if (daGav.equals(lr.getGav())) {
                assertFalse(lr.getAvailableVersions().contains(whiteArtifact1_1_1.getVersion()));
                assertTrue(lr.getAvailableVersions().contains(whiteArtifact1_2_1.getVersion()));
                assertFalse(lr.getAvailableVersions().contains(whiteArtifact2_1_a.getVersion()));
            } else if (jacksonGav.equals(lr.getGav())) {
                assertFalse(lr.getAvailableVersions().contains(whiteArtifact2_1_b.getVersion()));
                assertTrue(lr.getBestMatchVersion() == null);
            }
        }
    }

    @Test
    public void testGavsLookupAllWhiteArtifactsErroneousInput()
            throws CommunicationException, FindGAVDependencyException {

        LookupGAVsRequest productsNamesReq = getDefaultLookupRequest();

        // Test with product that doesn't exist
        // Inputs : productNames [Product3]
        productsNamesReq.getProductNames().add("Product3");
        try {
            reportGenerator.getLookupReportsForGavs(productsNamesReq);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Product3"));
        }

        // Test combination of existing / in-existing products
        // Inputs : productNames [Product1, Product3]
        productsNamesReq.getProductNames().add("Product1");
        try {
            reportGenerator.getLookupReportsForGavs(productsNamesReq);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Product3"));
        }

        LookupGAVsRequest productVersionsReq = getDefaultLookupRequest();

        // Test with product version ID that doesn't exist
        // Inputs : productVersionIds [5555]
        productVersionsReq.getProductVersionIds().add(5555L);
        try {
            reportGenerator.getLookupReportsForGavs(productVersionsReq);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("5555"));
        }

        // Test combination of existing / in-existing product version IDs
        // Inputs : productVersionIds [5555, prodVer1_2.getId()]
        productVersionsReq.getProductVersionIds().add(prodVer1_2.getId());
        try {
            reportGenerator.getLookupReportsForGavs(productVersionsReq);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("5555"));
        }

        // Test valid product name request with invalid product version id
        LookupGAVsRequest validNameRequest = getDefaultLookupRequest();
        validNameRequest.getProductNames().add("Product1");
        validNameRequest.getProductVersionIds().add(5555L);
        try {
            reportGenerator.getLookupReportsForGavs(validNameRequest);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("5555"));
        }

        // Test valid product version id request with invalid product name
        LookupGAVsRequest validProdVerRequest = getDefaultLookupRequest();
        validProdVerRequest.getProductNames().add("NoSuchProduct");
        validProdVerRequest.getProductVersionIds().add(prodVer1_2.getId());
        try {
            reportGenerator.getLookupReportsForGavs(validProdVerRequest);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("NoSuchProduct"));
        }

        // Test combination of valid/invalid inputs for both name/ids
        LookupGAVsRequest combinedValidAndInvalidRequest = getDefaultLookupRequest();
        combinedValidAndInvalidRequest.getProductNames().add("NoSuchProduct");
        combinedValidAndInvalidRequest.getProductNames().add("Product1");
        combinedValidAndInvalidRequest.getProductVersionIds().add(5555L);
        combinedValidAndInvalidRequest.getProductVersionIds().add(prodVer1_2.getId());
        try {
            reportGenerator.getLookupReportsForGavs(combinedValidAndInvalidRequest);
            fail();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            assertTrue(e.getMessage().contains("NoSuchProduct"));
            assertTrue(e.getMessage().contains("5555"));
        }
    }

}
