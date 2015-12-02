package org.jboss.da.bc.backend.impl;

import org.jboss.da.bc.backend.api.BCSetGenerator;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pnc.api.PNCConnector;
import org.jboss.da.communication.pnc.api.PNCRequestException;
import org.jboss.da.communication.pnc.model.BuildConfigurationSet;
import org.jboss.da.communication.pnc.model.Product;
import org.jboss.da.communication.pnc.model.ProductVersion;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class BCSetGeneratorImpl implements BCSetGenerator {

    @Inject
    private PNCConnector pnc;

    @Override
    public BuildConfigurationSet createBCSet(String name, Integer productVersionId,
            List<Integer> bcIds) throws CommunicationException, PNCRequestException {

        Optional<BuildConfigurationSet> existingBcSet = pnc.findBuildConfigurationSet(
                productVersionId, bcIds);

        if (existingBcSet.isPresent()) {
            // bcSet already exists, this should not happen. return an error
            throw new PNCRequestException("Build Configuration Set already exists!");
        }

        BuildConfigurationSet bcSet = new BuildConfigurationSet();
        bcSet.setBuildConfigurationIds(bcIds);
        bcSet.setName(name);
        bcSet.setProductVersionId(productVersionId);

        return pnc.createBuildConfigurationSet(bcSet);
    }

    @Override
    public Integer createProduct(String name, String productVersion) throws CommunicationException,
            PNCRequestException {
        Optional<Product> p = pnc.findProduct(name);

        Product product;

        // create product if it does not exist
        if (!p.isPresent()) {
            product = pnc.createProduct(new Product(name));
        } else {
            product = p.get();
        }

        Optional<ProductVersion> potentialPV = pnc.findProductVersion(p.get(), productVersion);

        if (potentialPV.isPresent()) {
            return potentialPV.get().getId();
        }
        // if product version doesn't exist yet, create a new one
        ProductVersion pv = pnc.createProductVersion(new ProductVersion(productVersion, product
                .getId()));
        return pv.getId();

    }

}
