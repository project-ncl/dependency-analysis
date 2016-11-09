package org.jboss.da.test.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.da.communication.auth.impl.JAASAuthenticatorService;
import org.jboss.da.listings.api.dao.ProductVersionDAO;
import org.jboss.da.listings.api.model.BlackArtifact;
import org.jboss.da.listings.api.model.ProductVersion;
import org.jboss.da.listings.api.model.WhiteArtifact;
import org.jboss.da.listings.api.service.ArtifactService.ArtifactStatus;
import org.jboss.da.listings.api.service.BlackArtifactService;
import org.jboss.da.listings.api.service.ProductService;
import org.jboss.da.listings.api.service.WhiteArtifactService;
import org.jboss.da.listings.model.ProductSupportStatus;
import org.jboss.da.listings.model.rest.RestProductArtifact;
import org.jboss.da.test.ArquillianDeploymentFactory;
import org.jboss.da.test.ArquillianDeploymentFactory.DepType;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author fkujikis
 */
@RunWith(Arquillian.class)
public class ArtifactBlackWhitelistTransitionsTest {

    @Inject
    private BlackArtifactService blackService;

    @Inject
    private WhiteArtifactService whiteService;

    @Inject
    private ProductVersionDAO productVersionDao;

    @Inject
    private ProductService productService;

    @Deployment
    public static EnterpriseArchive createDeployment() {
        return new ArquillianDeploymentFactory().createDeployment(DepType.REPORTS);
    }

    private RestProductArtifact whiteArtifact1;

    private RestProductArtifact whiteArtifact2;

    private RestProductArtifact blackArtifact1;

    private RestProductArtifact whiteToBlackArtifact;

    private ProductVersion product1;

    private ProductVersion product2;

    @Before
    public void setUp() {
        JAASAuthenticatorService.setUser(null);

        whiteArtifact1 = new RestProductArtifact();
        whiteArtifact1.setArtifactId("white1");
        whiteArtifact1.setGroupId("org.test");
        whiteArtifact1.setVersion("1.0.0");

        whiteArtifact2 = new RestProductArtifact();
        whiteArtifact2.setArtifactId("white2");
        whiteArtifact2.setGroupId("org.test");
        whiteArtifact2.setVersion("1.0.0");

        blackArtifact1 = new RestProductArtifact();
        blackArtifact1.setArtifactId("black1");
        blackArtifact1.setGroupId("org.test");
        blackArtifact1.setVersion("1.0.0");

        whiteToBlackArtifact = new RestProductArtifact();
        whiteToBlackArtifact.setArtifactId("whiteToBlack");
        whiteToBlackArtifact.setGroupId("org.test");
        whiteToBlackArtifact.setVersion("1.0.0");

        // Add product1 and product2
        productService.addProduct("Product1", "1.0.0", ProductSupportStatus.UNKNOWN);
        product1 = productVersionDao.findProductVersion("Product1", "1.0.0").get();
        productService.addProduct("Product2", "1.0.0", ProductSupportStatus.UNKNOWN);
        product2 = productVersionDao.findProductVersion("Product2", "1.0.0").get();

        JAASAuthenticatorService.setUser("user");

        // Add whiteArtifact1 and whiteToBlackArtifact to Product1
        whiteService.addArtifact(whiteArtifact1.getGroupId(), whiteArtifact1.getArtifactId(),
                whiteArtifact1.getVersion(), product1.getId());
        whiteService.addArtifact(whiteToBlackArtifact.getGroupId(),
                whiteToBlackArtifact.getArtifactId(), whiteToBlackArtifact.getVersion(),
                product1.getId());

        // Add whiteArtifact2 and whiteToBlackArtifact to Product2
        whiteService.addArtifact(whiteArtifact2.getGroupId(), whiteArtifact2.getArtifactId(),
                whiteArtifact2.getVersion(), product2.getId());
        whiteService.addArtifact(whiteToBlackArtifact.getGroupId(),
                whiteToBlackArtifact.getArtifactId(), whiteToBlackArtifact.getVersion(),
                product2.getId());

        blackService.addArtifact(blackArtifact1.getGroupId(), blackArtifact1.getArtifactId(),
                blackArtifact1.getVersion());

    }

    @After
    public void tearDown() {
        // Remove all artifacts
        whiteService.removeArtifact(whiteArtifact1.getGroupId(), whiteArtifact1.getArtifactId(),
                whiteArtifact1.getVersion());
        whiteService.removeArtifact(whiteArtifact2.getGroupId(), whiteArtifact2.getArtifactId(),
                whiteArtifact2.getVersion());
        whiteService.removeArtifact(whiteToBlackArtifact.getGroupId(),
                whiteToBlackArtifact.getArtifactId(), whiteToBlackArtifact.getVersion());
        blackService.removeArtifact(whiteToBlackArtifact.getGroupId(),
                whiteToBlackArtifact.getArtifactId(), whiteToBlackArtifact.getVersion());
        blackService.removeArtifact(blackArtifact1.getGroupId(), blackArtifact1.getArtifactId(),
                blackArtifact1.getVersion());

        // Remove all products
        productService.removeProduct("Product1", "1.0.0");
        productService.removeProduct("Product2", "1.0.0");

        JAASAuthenticatorService.setUser(null);
    }

    private void refreshProducts() {
        product1 = productVersionDao.read(product1.getId());
        product2 = productVersionDao.read(product2.getId());
    }

    @Test
    public void testBlacklistWhitelistedArtifact() {

        // Check original artifacts for product are set
        refreshProducts();
        assertEquals(2, product1.getWhiteArtifacts().size());
        assertEquals(2, product2.getWhiteArtifacts().size());

        // Check artifact was previously white listed
        ArtifactStatus as = blackService.addArtifact(whiteToBlackArtifact.getGroupId(),
                whiteToBlackArtifact.getArtifactId(), whiteToBlackArtifact.getVersion());
        assertEquals(ArtifactStatus.WAS_WHITELISTED, as);

        // Check the artifact was blacklisted
        Optional<BlackArtifact> artifact = blackService.getArtifact(
                whiteToBlackArtifact.getGroupId(), whiteToBlackArtifact.getArtifactId(),
                whiteToBlackArtifact.getVersion());
        assertTrue(artifact.isPresent());

        // Check artifact is no longer in whitelist DB
        List<WhiteArtifact> whiteArtifactsMatchingGav = whiteService.getArtifacts(
                whiteToBlackArtifact.getGroupId(), whiteToBlackArtifact.getArtifactId(),
                whiteToBlackArtifact.getVersion());
        assertTrue(whiteArtifactsMatchingGav.isEmpty());

        // Check artifact is still on product white lists
        // Demonstrates issue NCL-1883:
        // - correct behaviour is as above asserts in test plus
        // - blacklisted artifact shouldn't be removed from ProductVersion whiteArtifact lists
        refreshProducts();
        assertEquals(2, product1.getWhiteArtifacts().size());
        assertEquals(2, product2.getWhiteArtifacts().size());
    }

    @Test
    public void testAddBlacklistedArtifactToProductWhitelist() {

        // Check original artifacts for product are set
        refreshProducts();
        assertEquals(2, product1.getWhiteArtifacts().size());

        // Add the blacklisted artifact to a product
        ArtifactStatus as = whiteService.addArtifact(blackArtifact1.getGroupId(),
                blackArtifact1.getArtifactId(), blackArtifact1.getVersion(), product1.getId());
        assertEquals(as, ArtifactStatus.ADDED);

        // Check blackArtifact1 artifact has been added to product 1
        refreshProducts();
        assertEquals(3, product1.getWhiteArtifacts().size());

        // Confirm blackArtifact1 still on blacklist
        boolean artifactOnBlacklist = blackService.isArtifactPresent(blackArtifact1.getGroupId(),
                blackArtifact1.getArtifactId(), blackArtifact1.getVersion());
        assertTrue(artifactOnBlacklist);

        // Confirm artifact not on whitelist
        List<WhiteArtifact> whiteArtifacts = whiteService.getArtifacts(blackArtifact1.getGroupId(),
                blackArtifact1.getArtifactId(), blackArtifact1.getVersion());
        assertTrue(whiteArtifacts.isEmpty());

        // Check artifact can be removed as before
        boolean artifactRemovedFromProduct = whiteService.removeArtifractFromProductVersion(
                blackArtifact1.getGroupId(), blackArtifact1.getArtifactId(),
                blackArtifact1.getVersion(), product1.getId());
        assertTrue(artifactRemovedFromProduct);

        // Check blackArtifact1 artifact has been removed from product 1
        refreshProducts();
        assertEquals(2, product1.getWhiteArtifacts().size());
    }
}
