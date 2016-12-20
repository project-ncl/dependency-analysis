package org.jboss.da.communication.pnc.api;

import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pnc.model.BuildConfiguration;
import org.jboss.da.communication.pnc.model.BuildConfigurationBPMCreate;
import org.jboss.da.communication.pnc.model.BuildConfigurationSet;
import org.jboss.da.communication.pnc.model.ProductVersion;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public interface PNCAuthConnector extends PNCConnector {

    /**
     * Starts creation of build configuration. The build configuration is created asynchronously.
     *
     * @param bc The build configuration to be created.
     * @throws CommunicationException Thrown if communication with PNC failed
     * @throws PNCRequestException Thrown if PNC returns an error
     */
    void createBuildConfiguration(BuildConfigurationBPMCreate bc) throws CommunicationException,
            PNCRequestException;

    /**
     * Starts creation of build configuration. The build configuration is created asynchronously.
     *
     * @param bc The build configuration to be created.
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
