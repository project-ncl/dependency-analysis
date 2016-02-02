package org.jboss.da.bc.backend.api;

import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pnc.api.PNCRequestException;
import org.jboss.da.communication.pnc.model.BuildConfigurationSet;

import java.util.List;

public interface BCSetGenerator {

    /**
     * Create a new BuildConfigurationSet.
     * If the BuildConfigurationSet already exists, then throw a PNCRequestException.
     *
     * From DA-169, we have to report an error if the BuildConfigurationSet already
     * exists.
     * 
     * @param name
     * @param productVersionId
     * @param bcIds
     * @return BuildConfigurationSet
     * @throws CommunicationException Thrown if communication with PNC failed
     * @throws PNCRequestException Thrown if PNC returns an error, or if the
     *         BuildConfigurationSet already exists
     */
    BuildConfigurationSet createBCSet(String name, Integer productVersionId, List<Integer> bcIds)
            throws CommunicationException, PNCRequestException;

    /**
     * Find the Product on PNC and create the product version for that product.
     * The product must already exist.
     *
     * From DA-167, if the ProductVersion already exists, we will re-use it
     * and create a new BuildConfigurationSet
     *
     * @param productId id of product to use
     * @param productVersion version of product to create
     * @return The id of the product version created, or found
     * @throws CommunicationException Thrown if communication with PNC failed
     * @throws PNCRequestException Thrown if PNC returns an error
     */
    Integer createProductVersion(int productId, String productVersion)
            throws CommunicationException, PNCRequestException;
}
