package org.jboss.da.communication.pnc.api;

import org.jboss.da.communication.pnc.model.BuildConfiguration;
import org.jboss.da.communication.pnc.model.BuildConfigurationCreate;
import org.jboss.da.communication.pnc.model.BuildConfigurationSet;
import org.jboss.da.communication.pnc.model.Product;
import org.jboss.da.communication.pnc.model.ProductVersion;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
public interface PNCConnector {

    List<BuildConfiguration> getBuildConfigurations() throws Exception;

    /**
     * Gets all BuildConfigurations from PNC with the specific SCM URL and SCM Revision
     * 
     * @param scmUrl SCM URL expected in BC
     * @param scmRevision SCM revision expected in BC
     * @return List of BCs with specified SCM URL and SCM revision or empty list if no BC was found
     * @throws Exception Thrown if communication with PNC failed
     */
    List<BuildConfiguration> getBuildConfigurations(String scmUrl, String scmRevision)
            throws Exception;

    BuildConfiguration createBuildConfiguration(BuildConfigurationCreate bc) throws Exception;

    /**
     * Deletes BuildConfiguration from PNC by ID of the BC
     * 
     * @param bc BC to be deleted
     * @return True, if the BC was successfully removed, otherwise false
     * @throws Exception Thrown if the communication with PNC failed
     */
    boolean deleteBuildConfiguration(BuildConfiguration bc) throws Exception;

    /**
     * Deletes BuildConfiguration from PNC by ID of the BC
     * 
     * @param bcId ID of BC to be deleted
     * @return True, if the BC was successfully removed, otherwise false
     * @throws Exception Thrown if the communication with PNC failed
     */
    boolean deleteBuildConfiguration(int bcId) throws Exception;

    BuildConfigurationSet createBuildConfigurationSet(BuildConfigurationSet bcs) throws Exception;

    /**
     * Finds BuildConfigurationSet with specific product version id and build configurations ids from pnc
     * 
     * @param productVersionId
     * @param buildConfigurationIds
     * @return Optional.empty() if buildConfigurationSet not found, else the BuildConfigurationSet
     */
    Optional<BuildConfigurationSet> findBuildConfigurationSet(int productVersionId,
            List<Integer> buildConfigurationIds) throws Exception;

    Product createProduct(Product p) throws Exception;

    /**
     * Find product having a particular name. Since each product has a unique
     * name, we can only find one such product with that name
     *
     * @param name
     * @return Optional.empty() if product not found, else the Product
     * @throws Exception
     */
    Optional<Product> findProduct(String name) throws Exception;

    ProductVersion createProductVersion(ProductVersion pv) throws Exception;

    /**
     * Find ProductVersion assigned to a particular product and having a specific
     * version. Since each product has unique product versions, we can only find
     * one such ProductVersion
     * @param product
     * @param version
     * @return Optional.empty() if productVersion not found, else the productVersion
     * @throws Exception
     */
    Optional<ProductVersion> findProductVersion(Product p, String version) throws Exception;

    /**
     * Find ProductVersion assigned to a particular product and having a specific
     * version. Since each product has unique product versions, we can only find
     * one such ProductVersion
     * @param product id
     * @param version
     * @return Optional.empty() if productVersion not found, else the productVersion
     * @throws Exception
     */
    Optional<ProductVersion> findProductVersion(int productId, String version) throws Exception;
}
