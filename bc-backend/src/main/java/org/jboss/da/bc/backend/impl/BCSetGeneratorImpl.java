package org.jboss.da.bc.backend.impl;

import org.jboss.da.bc.backend.api.BCSetGenerator;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pnc.api.PNCConnectorProvider;
import org.jboss.da.communication.pnc.api.PNCRequestException;
import org.jboss.da.communication.pnc.model.BuildConfigurationSet;
import org.jboss.da.communication.pnc.model.ProductVersion;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class BCSetGeneratorImpl implements BCSetGenerator {

    @Inject
    private PNCConnectorProvider pnc;

    @Override
    public BuildConfigurationSet createBCSet(String name, Integer productVersionId,
            List<Integer> bcIds, String token) throws CommunicationException, PNCRequestException {

        Optional<BuildConfigurationSet> existingBcSet = pnc.getConnector()
                .findBuildConfigurationSet(productVersionId, bcIds);

        if (existingBcSet.isPresent()) {
            // bcSet already exists, this should not happen. return an error
            throw new PNCRequestException("Build Configuration Set already exists!");
        }

        BuildConfigurationSet bcSet = new BuildConfigurationSet();
        bcSet.setBuildConfigurationIds(bcIds);
        bcSet.setName(name);
        bcSet.setProductVersionId(productVersionId);

        return pnc.getAuthConnector(token).createBuildConfigurationSet(bcSet);
    }

    @Override
    public Integer createProductVersion(int productId, String productVersion, String token)
            throws CommunicationException, PNCRequestException {

        Optional<ProductVersion> potentialPV = pnc.getConnector().findProductVersion(productId,
                productVersion);

        if (potentialPV.isPresent()) {
            return potentialPV.get().getId();
        }
        // if product version doesn't exist yet, create a new one
        ProductVersion pv = pnc.getAuthConnector(token).createProductVersion(
                new ProductVersion(productVersion, productId));
        return pv.getId();

    }

}
