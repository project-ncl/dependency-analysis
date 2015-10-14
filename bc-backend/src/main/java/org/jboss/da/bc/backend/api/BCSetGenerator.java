package org.jboss.da.bc.backend.api;

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
     * @throws Exception 
     */
    BuildConfigurationSet createBCSet(String name, Integer productVersionId, List<Integer> bcIds)
            throws Exception;

    Integer createProduct(String name, String productVersion) throws Exception;
}
