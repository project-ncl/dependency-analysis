package org.jboss.da.bc.backend.api;

import org.jboss.da.bc.model.BuildConfigurationSet;

import java.util.List;

public interface BCSetGenerator {

    /**
     * Finds if BuildConfigurationSet with specified parameters already exists if not creates it
     * 
     * @param name
     * @param productVersionId
     * @param bcIds
     * @return BuildConfigurationSet
     */
    BuildConfigurationSet createBCSet(String name, Integer productVersionId, List<Integer> bcIds);

}
