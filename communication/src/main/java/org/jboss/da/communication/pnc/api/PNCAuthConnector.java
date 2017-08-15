package org.jboss.da.communication.pnc.api;

import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pnc.model.BuildConfiguration;
import org.jboss.da.communication.pnc.model.BuildConfigurationCreate;
import org.jboss.da.communication.pnc.model.BuildConfigurationSet;
import org.jboss.da.communication.pnc.model.ProductVersion;

import java.util.concurrent.Future;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public interface PNCAuthConnector extends PNCConnector {

    /**
     * Creates Build Configuration.
     *
     * @param bc The build configuration to be created.
     * @return Created Build Configuration
     * @throws CommunicationException Thrown if communication with PNC failed
     * @throws PNCRequestException Thrown if PNC returns an error
     */
    BuildConfiguration createBuildConfiguration(BuildConfigurationCreate bc)
            throws CommunicationException, PNCRequestException;

    /**
     * Starts creation of repository configuration. The repository configuration is created
     * asynchronously and with default configuration.
     *
     * @param url Url of the repository
     * @return Id of the created configuration.
     */
    Future<Integer> createRepositoryConfiguration(String url);

    /**
     * Updates existing Build Configuration.
     *
     * @param bc The build configuration to be updated.
     * @throws CommunicationException Thrown if communication with PNC failed
     * @throws PNCRequestException Thrown if PNC returns an error
     */
    void updateBuildConfiguration(BuildConfiguration bc) throws CommunicationException,
            PNCRequestException;

    /**
     * Deletes BuildConfiguration from PNC by ID of the BC
     * 
     * @param bc BC to be deleted
     * @throws CommunicationException Thrown if the communication with PNC failed
     * @throws PNCRequestException Thrown if PNC returns an error
     */
    void deleteBuildConfiguration(BuildConfiguration bc) throws CommunicationException,
            PNCRequestException;

    /**
     * Deletes BuildConfiguration from PNC by ID of the BC
     * 
     * @param bcId ID of BC to be deleted
     * @throws CommunicationException Thrown if the communication with PNC failed
     * @throws PNCRequestException Thrown if PNC returns an error
     */
    void deleteBuildConfiguration(int bcId) throws CommunicationException, PNCRequestException;

    BuildConfigurationSet createBuildConfigurationSet(BuildConfigurationSet bcs)
            throws CommunicationException, PNCRequestException;

    ProductVersion createProductVersion(ProductVersion pv) throws CommunicationException,
            PNCRequestException;
}
