package org.jboss.da.test.server.listings;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.da.communication.auth.impl.JAASAuthenticatorService;
import org.jboss.da.listings.api.model.ProductVersion;
import org.jboss.da.listings.api.service.ArtifactService.ArtifactStatus;
import org.jboss.da.listings.api.service.ProductService;
import org.jboss.da.listings.api.service.ProductVersionService;
import org.jboss.da.listings.api.service.WhiteArtifactService;
import org.jboss.da.listings.model.ProductSupportStatus;
import org.jboss.da.test.server.AbstractServerTest;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@RunWith(Arquillian.class)
public class WhiteArtifactServiceTest extends AbstractServerTest {

    @Inject
    ProductVersionService pvs;

    @Inject
    ProductService ps;

    @Inject
    WhiteArtifactService was;

    @Test
    public void testAddDuplicateGA() {
        JAASAuthenticatorService.setUser("user");
        ps.addProduct("EAP", "9.4.6", ProductSupportStatus.UNKNOWN);
        ProductVersion pv = pvs.getProductVersion("EAP", "9.4.6").get();
        ArtifactStatus ret1 = was.addArtifact("foo", "bar", "1.0.0", pv.getId());
        assertEquals(ArtifactStatus.ADDED, ret1);
        ArtifactStatus ret2 = was.addArtifact("foo", "bar", "1.0.0", pv.getId());
        assertEquals(ArtifactStatus.NOT_MODIFIED, ret2);
        ArtifactStatus ret3 = was.addArtifact("foo", "bar", "2.0.0", pv.getId());
        assertEquals(ArtifactStatus.GA_EXISTS, ret3);
    }

}
