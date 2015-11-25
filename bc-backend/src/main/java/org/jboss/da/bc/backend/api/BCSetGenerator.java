package org.jboss.da.bc.backend.api;

import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pnc.api.PNCRequestException;
import org.jboss.da.communication.pnc.model.BuildConfigurationSet;

import java.util.List;

public interface BCSetGenerator {

    /**
     * Finds if BuildConfigurationSet with specified parameters already exists if not creates it
     * 
     * @param name
     * @param productVersionId
     * @param bcIds
     * @return BuildConfigurationSet
     * @throws CommunicationException Thrown if communication with PNC failed
     * @throws PNCRequestException Thrown if PNC returns an error
     */
    BuildConfigurationSet createBCSet(String name, Integer productVersionId, List<Integer> bcIds)
            throws CommunicationException, PNCRequestException;

    Integer createProduct(String name, String productVersion) throws CommunicationException,
            PNCRequestException;
}
