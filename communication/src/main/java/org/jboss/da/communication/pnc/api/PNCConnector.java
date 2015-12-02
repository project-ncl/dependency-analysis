package org.jboss.da.communication.pnc.api;

import org.jboss.da.common.CommunicationException;
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

    List<BuildConfiguration> getBuildConfigurations() throws CommunicationException,
            PNCRequestException;

    /**
     * Gets all BuildConfigurations from PNC with the specific SCM URL and SCM Revision
     * 
     * @param scmUrl SCM URL expected in BC
     * @param scmRevision SCM revision expected in BC
     * @return List of BCs with specified SCM URL and SCM revision or empty list if no BC was found
     * @throws CommunicationException Thrown if communication with PNC failed
     * @throws PNCRequestException Thrown if PNC returns an error
     */
    List<BuildConfiguration> getBuildConfigurations(String scmUrl, String scmRevision)
            throws CommunicationException, PNCRequestException;

    BuildConfiguration createBuildConfiguration(BuildConfigurationCreate bc)
            throws CommunicationException, PNCRequestException;

    /**
     * Deletes BuildConfiguration from PNC by ID of the BC
     * 
     * @param bc BC to be deleted
     * @return True, if the BC was successfully removed, otherwise false
     * @throws CommunicationException Thrown if the communication with PNC failed
     * @throws PNCRequestException Thrown if PNC returns an error
     */
    boolean deleteBuildConfiguration(BuildConfiguration bc) throws CommunicationException,
            PNCRequestException;

    /**
     * Deletes BuildConfiguration from PNC by ID of the BC
     * 
     * @param bcId ID of BC to be deleted
     * @return True, if the BC was successfully removed, otherwise false
     * @throws CommunicationException Thrown if the communication with PNC failed
     * @throws PNCRequestException Thrown if PNC returns an error
     */
    boolean deleteBuildConfiguration(int bcId) throws CommunicationException, PNCRequestException;

    BuildConfigurationSet createBuildConfigurationSet(BuildConfigurationSet bcs)
            throws CommunicationException, PNCRequestException;

    /**
     * Finds BuildConfigurationSet with specific product version id and build configurations ids from pnc
     * 
     * @param productVersionId
     * @param buildConfigurationIds
     * @return Optional.empty() if buildConfigurationSet not found, else the BuildConfigurationSet
     * @throws CommunicationException Thrown if the communication with PNC failed
     * @throws PNCRequestException Thrown if PNC returns an error
     */
    Optional<BuildConfigurationSet> findBuildConfigurationSet(int productVersionId,
            List<Integer> buildConfigurationIds) throws CommunicationException, PNCRequestException;

    /**
     * Find ProductVersion assigned to a particular product and having a specific
     * version. Since each product has unique product versions, we can only find
     * one such ProductVersion
     * @param product the product entity
     * @param version the version to find
     * @return Optional.empty() if productVersion not found, else the productVersion
     * @throws CommunicationException Thrown if the communication with PNC failed
     * @throws PNCRequestException Thrown if PNC returns an error
     */
    Optional<ProductVersion> findProductVersion(Product product, String version)
            throws CommunicationException, PNCRequestException;

    /**
     * Find ProductVersion assigned to a particular product and having a specific
     * version. Since each product has unique product versions, we can only find
     * one such ProductVersion
     * @param productId the product id
     * @param version version to find
     * @return Optional.empty() if productVersion not found, else the productVersion
     * @throws CommunicationException Thrown if the communication with PNC failed
     * @throws PNCRequestException Thrown if PNC returns an error
     */
    Optional<ProductVersion> findProductVersion(int productId, String version)
            throws CommunicationException, PNCRequestException;

    /**
     * Find product having a particular name. Since each product has a unique
     * name, we can only find one such product with that name
     *
     * @param name name of product
     * @return Optional.empty() if product not found, else the Product
     * @throws Exception
     */
    Optional<Product> findProduct(String name) throws CommunicationException, PNCRequestException;

    Product createProduct(Product p) throws CommunicationException, PNCRequestException;

    ProductVersion createProductVersion(ProductVersion pv) throws CommunicationException,
            PNCRequestException;
}
