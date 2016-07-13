package org.jboss.da.test.server;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.maven.scm.ScmException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.aprox.FindGAVDependencyException;
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
import org.jboss.da.reports.model.rest.GAVRequest;
import org.jboss.da.reports.model.rest.LookupGAVsRequest;
import org.jboss.da.reports.model.rest.LookupReport;
import org.jboss.da.test.ArquillianDeploymentFactory;
import org.jboss.da.test.ArquillianDeploymentFactory.DepType;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
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
public class ReportsWithWhitelistedArtifactsRemoteTest {

    @Deployment
    public static EnterpriseArchive createDeployment() {
        return new ArquillianDeploymentFactory().createDeployment(DepType.REPORTS);
    }

    @Inject
    private ReportsGenerator reportGenerator;

    @Inject
    private WhiteArtifactService whiteService;

    @Inject
    private ProductVersionDAO productVersionDao;

    @Inject
    private ProductService productService;

    ProductVersion prodVer1_1;

    RestProductArtifact whiteArtifact1_1_1;

    RestProductArtifact whiteArtifact1_1_2;

    ProductVersion prodVer1_2;

    RestProductArtifact whiteArtifact1_2_1;

    RestProductArtifact whiteArtifact1_2_2;

    ProductVersion prodVer2_1;

    RestProductArtifact whiteArtifact2_1_a;

    RestProductArtifact whiteArtifact2_1_b;

    RestProductArtifact whiteArtifact2_1_c;

    @Before
    public void setUp() {

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

        whiteService.addArtifact(whiteArtifact1_1_1.getGroupId(),
                whiteArtifact1_1_1.getArtifactId(), whiteArtifact1_1_1.getVersion(),
                prodVer1_1.getId());
        whiteService.addArtifact(whiteArtifact1_1_2.getGroupId(),
                whiteArtifact1_1_2.getArtifactId(), whiteArtifact1_1_2.getVersion(),
                prodVer1_1.getId());

        whiteService.addArtifact(whiteArtifact1_2_1.getGroupId(),
                whiteArtifact1_2_1.getArtifactId(), whiteArtifact1_2_1.getVersion(),
                prodVer1_2.getId());
        whiteService.addArtifact(whiteArtifact1_2_2.getGroupId(),
                whiteArtifact1_2_2.getArtifactId(), whiteArtifact1_2_2.getVersion(),
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

        whiteService.addArtifact(whiteArtifact2_1_a.getGroupId(),
                whiteArtifact2_1_a.getArtifactId(), whiteArtifact2_1_a.getVersion(),
                prodVer2_1.getId());
        whiteService.addArtifact(whiteArtifact2_1_b.getGroupId(),
                whiteArtifact2_1_b.getArtifactId(), whiteArtifact2_1_b.getVersion(),
                prodVer2_1.getId());
        whiteService.addArtifact(whiteArtifact2_1_c.getGroupId(),
                whiteArtifact2_1_c.getArtifactId(), whiteArtifact2_1_c.getVersion(),
                prodVer2_1.getId());
    }

    @After
    public void tearDown() {

        // Remove artifacts
        whiteService.removeArtifact(whiteArtifact1_1_1.getGroupId(),
                whiteArtifact1_1_1.getArtifactId(), whiteArtifact1_1_1.getVersion());
        whiteService.removeArtifact(whiteArtifact1_1_2.getGroupId(),
                whiteArtifact1_1_2.getArtifactId(), whiteArtifact1_1_2.getVersion());
        whiteService.removeArtifact(whiteArtifact1_2_1.getGroupId(),
                whiteArtifact1_2_1.getArtifactId(), whiteArtifact1_2_1.getVersion());
        whiteService.removeArtifact(whiteArtifact1_2_2.getGroupId(),
                whiteArtifact1_2_2.getArtifactId(), whiteArtifact1_2_2.getVersion());

        whiteService.removeArtifact(whiteArtifact2_1_a.getGroupId(),
                whiteArtifact2_1_a.getArtifactId(), whiteArtifact2_1_a.getVersion());
        whiteService.removeArtifact(whiteArtifact2_1_b.getGroupId(),
                whiteArtifact2_1_b.getArtifactId(), whiteArtifact2_1_b.getVersion());
        whiteService.removeArtifact(whiteArtifact2_1_c.getGroupId(),
                whiteArtifact2_1_c.getArtifactId(), whiteArtifact2_1_c.getVersion());

        // Remove products
        productService.removeProduct("Product1", "1.0.0");
        productService.removeProduct("Product1", "2.0.0");
        productService.removeProduct("Product2", "1.0.0");
    }

    // This allows to test:
    // - whitelist lookup by product
    // - whitelist lookup by product version
    // - whitelist lookup without either (get all - 3 additions to top level)
    // - whitelist addition to top level
    // - whitelist addition to top-1 level

    @Test
    public void testScmAllWhiteArtifactsPresent() throws ScmException, PomAnalysisException, CommunicationException{

        SCMLocator locator = new SCMLocator(
                "https://github.com/project-ncl/dependency-analysis.git",
                "05a878", "common", null);

        Optional<ArtifactReport> ar = reportGenerator.getReportFromSCM(locator);
        
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
        Optional<ArtifactReport> dependencyJacksonDatabind = artifactReport.getDependencies().stream()
            .filter(x -> 
                        x.getArtifactId().equals(whiteArtifact2_1_b.getArtifactId())
                        &&
                        x.getGroupId().equals(whiteArtifact2_1_b.getGroupId())
                   )
            .findFirst();
            
        assertTrue(dependencyJacksonDatabind.isPresent());
        assertTrue(dependencyJacksonDatabind.get().getAvailableVersions().contains(whiteArtifact2_1_b.getVersion()));
    }    @Test
    public void testScmWhiteArtifactsPresentForDependenciesWithSingleProduct() throws ScmException, PomAnalysisException, CommunicationException{

        SCMLocator locator = new SCMLocator(
                "https://github.com/project-ncl/dependency-analysis.git",
                "05a878", "common", null);
        
        // Only use artifacts from the single product Product2 : version "1.0.0"
        locator.getProductIds().add(prodVer2_1.getProduct().getId());
        Optional<ArtifactReport> ar = reportGenerator.getReportFromSCM(locator);
        
        assertTrue(ar.isPresent());
        ArtifactReport artifactReport = ar.get();

        // Artifacts from Product2 version "1.0.0" expected
        assertFalse(artifactReport.getAvailableVersions().contains(whiteArtifact1_1_1.getVersion()));
        assertFalse(artifactReport.getAvailableVersions().contains(whiteArtifact1_2_1.getVersion()));
        assertTrue(artifactReport.getAvailableVersions().contains(whiteArtifact2_1_a.getVersion()));
        assertFalse(artifactReport.getAvailableVersions().contains(whiteArtifact2_1_b.getVersion()));  
    
        // Find jackson artifactReport and check it contains the jackson version
        Optional<ArtifactReport> dependencyJacksonDatabind = artifactReport.getDependencies().stream()
            .filter(x -> 
                        x.getArtifactId().equals(whiteArtifact2_1_b.getArtifactId())
                        &&
                        x.getGroupId().equals(whiteArtifact2_1_b.getGroupId())
                   )
            .findFirst();
            
        assertTrue(dependencyJacksonDatabind.isPresent());
        assertTrue(dependencyJacksonDatabind.get().getAvailableVersions().contains(whiteArtifact2_1_b.getVersion()));
    
    }

    @Test
    public void testScmWhiteArtifactsPresentForSingleProduct() throws ScmException,
            PomAnalysisException, CommunicationException {

        SCMLocator locator = new SCMLocator(
                "https://github.com/project-ncl/dependency-analysis.git", "05a878", "common", null);

        // Only use artifacts from the single product Product1 : versions "1.0.0" and "2.0.0" should be included
        locator.getProductIds().add(prodVer1_1.getProduct().getId());
        Optional<ArtifactReport> ar = reportGenerator.getReportFromSCM(locator);

        assertTrue(ar.isPresent());
        ArtifactReport artifactReport = ar.get();

        // Artifacts from Product1 (product versions  "1.0.0", "2.0.0" expected)
        assertTrue(artifactReport.getAvailableVersions().contains(whiteArtifact1_1_1.getVersion()));
        assertTrue(artifactReport.getAvailableVersions().contains(whiteArtifact1_2_1.getVersion()));
        assertFalse(artifactReport.getAvailableVersions().contains(whiteArtifact2_1_a.getVersion()));
        assertFalse(artifactReport.getAvailableVersions().contains(whiteArtifact2_1_b.getVersion()));
    }

    @Test
    public void testScmWhiteArtifactsPresentForSingleProductVersion() throws ScmException,
            PomAnalysisException, CommunicationException {

        SCMLocator locator = new SCMLocator(
                "https://github.com/project-ncl/dependency-analysis.git", "05a878", "common", null);

        // Only use artifacts from the single product version : "Product1", "1.0.0"
        locator.getProductVersionIds().add(prodVer1_1.getId());
        Optional<ArtifactReport> ar = reportGenerator.getReportFromSCM(locator);

        assertTrue(ar.isPresent());
        ArtifactReport artifactReport = ar.get();

        // Artifacts from the single product version : "Product1", "1.0.0" : expect 1 only
        assertTrue(artifactReport.getAvailableVersions().contains(whiteArtifact1_1_1.getVersion()));
        assertFalse(artifactReport.getAvailableVersions().contains(whiteArtifact1_2_1.getVersion()));
        assertFalse(artifactReport.getAvailableVersions().contains(whiteArtifact2_1_a.getVersion()));
        assertFalse(artifactReport.getAvailableVersions().contains(whiteArtifact2_1_b.getVersion()));
    }

    @Test
    public void testGavAllWhiteArtifactsPresent() throws CommunicationException,
            FindGAVDependencyException {

        // Query all white artifacts from all products
        Set<Long> productIds = new HashSet<>();
        Set<Long> productVersionIds = new HashSet<>();
        GAVRequest gavRequest = new GAVRequest("org.hibernate", "hibernate-core", "4.2.21.Final",
                productIds, productVersionIds);
        ArtifactReport artifactReport = reportGenerator.getReport(gavRequest);

        // Ensure we got an artifact report back 
        assertTrue(artifactReport != null);

        // Ensure all dependencies from whitelist are present matching the query GA
        assertTrue(artifactReport.getAvailableVersions().contains(whiteArtifact1_1_2.getVersion()));
        assertTrue(artifactReport.getAvailableVersions().contains(whiteArtifact1_2_2.getVersion()));
        assertTrue(artifactReport.getAvailableVersions().contains(whiteArtifact2_1_c.getVersion()));
    }

    @Test
    public void testGavWhiteArtifactsPresentForSingleProduct() throws CommunicationException,
            FindGAVDependencyException {

        // Query all white artifacts from Product 1
        Set<Long> productIds = new HashSet<>();
        productIds.add(prodVer1_1.getProduct().getId());
        Set<Long> productVersionIds = new HashSet<>();
        GAVRequest gavRequest = new GAVRequest("org.hibernate", "hibernate-core", "4.2.21.Final",
                productIds, productVersionIds);
        ArtifactReport artifactReport = reportGenerator.getReport(gavRequest);

        // Ensure we got an artifact report back 
        assertTrue(artifactReport != null);

        // Dependencies from Product 1 expected
        assertTrue(artifactReport.getAvailableVersions().contains(whiteArtifact1_1_2.getVersion()));
        assertTrue(artifactReport.getAvailableVersions().contains(whiteArtifact1_2_2.getVersion()));
        assertFalse(artifactReport.getAvailableVersions().contains(whiteArtifact2_1_c.getVersion()));
    }

    @Test
    public void testGavWhiteArtifactsPresentForSingleProductVersion()
            throws CommunicationException, FindGAVDependencyException {

        // Query all white artifacts from Product 1 version 2
        Set<Long> productIds = new HashSet<>();
        Set<Long> productVersionIds = new HashSet<>();
        productVersionIds.add(prodVer1_2.getId());
        GAVRequest gavRequest = new GAVRequest("org.hibernate", "hibernate-core", "4.2.21.Final",
                productIds, productVersionIds);
        ArtifactReport artifactReport = reportGenerator.getReport(gavRequest);

        // Ensure we got an artifact report back 
        assertTrue(artifactReport != null);

        // Dependencies from Product 1 version 2 expected
        assertFalse(artifactReport.getAvailableVersions().contains(whiteArtifact1_1_2.getVersion()));
        assertTrue(artifactReport.getAvailableVersions().contains(whiteArtifact1_2_2.getVersion()));
        assertFalse(artifactReport.getAvailableVersions().contains(whiteArtifact2_1_c.getVersion()));
    }

    private GAV artifactToGAV(RestProductArtifact art) {
        return new GAV(art.getGroupId(), art.getArtifactId(), art.getVersion());
    }

    @Test
    public void testGavsLookupAllWhiteArtifactsPresent() throws CommunicationException,
            FindGAVDependencyException {

        // Query all white artifacts from all products
        Set<Long> productIds = new HashSet<>();
        Set<Long> productVersionIds = new HashSet<>();

        GAV hibernateGav = artifactToGAV(whiteArtifact1_1_2);
        GAV daGav = artifactToGAV(whiteArtifact1_1_1);
        GAV jacksonGav = artifactToGAV(whiteArtifact2_1_b);

        List<GAV> gavs = new ArrayList<GAV>();
        gavs.add(hibernateGav);
        gavs.add(daGav);
        gavs.add(jacksonGav);
        LookupGAVsRequest req = new LookupGAVsRequest(productIds, productVersionIds, gavs);

        List<LookupReport> lookupReports = reportGenerator.getLookupReportsForGavs(req);

        assertTrue(lookupReports != null);

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

        // Query all white artifacts from Product 1
        Set<Long> productIds = new HashSet<>();
        productIds.add(prodVer1_1.getProduct().getId());
        Set<Long> productVersionIds = new HashSet<>();

        GAV hibernateGav = artifactToGAV(whiteArtifact1_1_2);
        GAV daGav = artifactToGAV(whiteArtifact1_1_1);
        GAV jacksonGav = artifactToGAV(whiteArtifact2_1_b);

        List<GAV> gavs = new ArrayList<GAV>();
        gavs.add(hibernateGav);
        gavs.add(daGav);
        gavs.add(jacksonGav);
        LookupGAVsRequest req = new LookupGAVsRequest(productIds, productVersionIds, gavs);

        List<LookupReport> lookupReports = reportGenerator.getLookupReportsForGavs(req);

        assertTrue(lookupReports != null);

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

        // Query all white artifacts from Product 1 version 2
        Set<Long> productIds = new HashSet<>();
        Set<Long> productVersionIds = new HashSet<>();
        productVersionIds.add(prodVer1_2.getId());

        GAV hibernateGav = artifactToGAV(whiteArtifact1_1_2);
        GAV daGav = artifactToGAV(whiteArtifact1_1_1);
        GAV jacksonGav = artifactToGAV(whiteArtifact2_1_b);

        List<GAV> gavs = new ArrayList<GAV>();
        gavs.add(hibernateGav);
        gavs.add(daGav);
        gavs.add(jacksonGav);
        LookupGAVsRequest req = new LookupGAVsRequest(productIds, productVersionIds, gavs);

        List<LookupReport> lookupReports = reportGenerator.getLookupReportsForGavs(req);

        assertTrue(lookupReports != null);

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

}
