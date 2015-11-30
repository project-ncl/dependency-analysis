package org.jboss.da.communication.pnc.api;

import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pnc.model.BuildConfiguration;
import org.jboss.da.communication.pnc.model.BuildConfigurationCreate;
import org.jboss.da.communication.pnc.model.BuildConfigurationSet;
import org.jboss.da.communication.pnc.model.Product;
import org.jboss.da.communication.pnc.model.ProductVersion;

import java.util.List;

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
     * @return BuildConfigurationSet or null if it is not found
     */
    BuildConfigurationSet findBuildConfigurationSet(int productVersionId,
            List<Integer> buildConfigurationIds);

    Product createProduct(Product p) throws CommunicationException, PNCRequestException;

    ProductVersion createProductVersion(ProductVersion pv) throws CommunicationException,
            PNCRequestException;
}
