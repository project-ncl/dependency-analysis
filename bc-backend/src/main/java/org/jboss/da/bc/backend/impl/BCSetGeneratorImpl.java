package org.jboss.da.bc.backend.impl;

import org.jboss.da.bc.backend.api.BCSetGenerator;
import org.jboss.da.bc.model.BuildConfigurationSet;
import org.jboss.da.communication.pnc.api.PNCConnector;
import org.jboss.da.communication.pnc.model.Product;
import org.jboss.da.communication.pnc.model.ProductVersion;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.util.List;

@ApplicationScoped
public class BCSetGeneratorImpl implements BCSetGenerator {

    @Inject
    private PNCConnector pnc;

    public BuildConfigurationSet createBCSet(String name, Integer productVersionId,
            List<Integer> bcIds) {
        BuildConfigurationSet bcSet = toBCSet(pnc
                .findBuildConfigurationSet(productVersionId, bcIds));
        if (bcSet == null) {
            bcSet = new BuildConfigurationSet();
            bcSet.setBuildConfigurationIds(bcIds);
            bcSet.setName(name);
            bcSet.setProductVersionId(productVersionId);
        }
        return bcSet;
    }

    private BuildConfigurationSet toBCSet(
            org.jboss.da.communication.pnc.model.BuildConfigurationSet pncBCSet) {
        if (pncBCSet == null)
            return null;
        BuildConfigurationSet bcSet = new BuildConfigurationSet();
        bcSet.setId(pncBCSet.getId());
        bcSet.setName(pncBCSet.getName());
        bcSet.setProductVersionId(pncBCSet.getProductVersionId());
        bcSet.setBuildConfigurationIds(pncBCSet.getBuildConfigurationIds());
        return bcSet;
    }

    @Override
    public Integer createProduct(String name, String productVersion) throws Exception {
        Product p = pnc.createProduct(new Product(name));
        ProductVersion pv = pnc.createProductVersion(new ProductVersion(productVersion, p.getId()));
        return pv.getId();
    }

}
