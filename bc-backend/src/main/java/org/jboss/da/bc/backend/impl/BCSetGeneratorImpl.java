package org.jboss.da.bc.backend.impl;

import org.jboss.da.bc.backend.api.BCSetGenerator;
import org.jboss.da.communication.pnc.api.PNCConnector;
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

    public BuildConfigurationSet createBCSet(String name, Integer productVersionId,
            List<Integer> bcIds) throws Exception {
        Optional<BuildConfigurationSet> bcs = pnc
                .findBuildConfigurationSet(productVersionId, bcIds);
        BuildConfigurationSet bcSet;
        if (bcs.isPresent()) {
            bcSet = toBCSet(bcs.get());
        } else {
            bcSet = new BuildConfigurationSet();
            bcSet.setBuildConfigurationIds(bcIds);
            bcSet.setName(name);
            bcSet.setProductVersionId(productVersionId);
        }

        return pnc.createBuildConfigurationSet(bcSet);
    }

    private BuildConfigurationSet toBCSet(
            org.jboss.da.communication.pnc.model.BuildConfigurationSet pncBCSet) {

        BuildConfigurationSet bcSet = new BuildConfigurationSet();
        bcSet.setId(pncBCSet.getId());
        bcSet.setName(pncBCSet.getName());
        bcSet.setProductVersionId(pncBCSet.getProductVersionId());
        bcSet.setBuildConfigurationIds(pncBCSet.getBuildConfigurationIds());
        return bcSet;
    }

    @Override
    public Integer createProduct(String name, String productVersion) throws Exception {
        Optional<Product> product = pnc.findProduct(name);

        // product exists in PNC
        if (product.isPresent()) {
            return createOrGetProductVersionForExistingProject(product.get(), productVersion);
        } else {
            Product p = pnc.createProduct(new Product(name));
            ProductVersion pv = pnc.createProductVersion(new ProductVersion(productVersion, p
                    .getId()));
            return pv.getId();
        }
    }

    private Integer createOrGetProductVersionForExistingProject(Product product,
            String productVersion) throws Exception {

        Optional<ProductVersion> potentialPV = pnc.findProductVersion(product, productVersion);

        // check if product version exists, if yes, return the id
        if (potentialPV.isPresent()) {
            return potentialPV.get().getId();
        } else {
            // else create a new one
            ProductVersion pv = pnc.createProductVersion(new ProductVersion(productVersion, product
                    .getId()));
            return pv.getId();
        }
    }

}
