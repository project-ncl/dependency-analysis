package org.jboss.da.bc.facade;

import org.apache.maven.scm.ScmException;
import org.jboss.da.bc.api.ProductBuildConfigurationGenerator;
import org.jboss.da.bc.model.backend.ProductGeneratorEntity;
import org.jboss.da.bc.model.rest.EntryEntity;
import org.jboss.da.bc.model.rest.ProductFinishResponse;
import org.jboss.da.bc.model.rest.ProductInfoEntity;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pnc.api.PNCRequestException;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.reports.api.SCMLocator;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.util.Optional;

@ApplicationScoped
public class BuildConfigurationsProductFacade extends
        AbstractBuildConfigurationsFacade<ProductInfoEntity> {

    @Inject
    ProductBuildConfigurationGenerator bcg;

    @Inject
    Logger log;

    @Override
    protected ProductInfoEntity start(SCMLocator scm, EntryEntity entry) throws ScmException,
            PomAnalysisException, CommunicationException {
        ProductGeneratorEntity entity = bcg.startBCGeneration(scm, entry.getId(),
                entry.getProductVersion());
        return toInfoEntity(entity);
    }

    @Override
    protected ProductInfoEntity nextLevel(ProductInfoEntity entity) throws CommunicationException {
        ProductGeneratorEntity ge = toGeneratorEntity(entity);
        ge = bcg.iterateBCGeneration(ge);
        return toInfoEntity(ge);
    }

    @Override
    protected Optional<Integer> finish(ProductInfoEntity entity) throws CommunicationException,
            PNCRequestException {
        ProductGeneratorEntity ge = toGeneratorEntity(entity);
        return bcg.createBC(ge);
    }

    @Override
    protected ProductFinishResponse getFinishResponse(ProductInfoEntity entity) {
        ProductFinishResponse response = new ProductFinishResponse();
        response.setEntity(entity);
        return response;
    }

    private ProductInfoEntity toInfoEntity(ProductGeneratorEntity ge) {
        ProductInfoEntity ie = new ProductInfoEntity();
        fillInfoEntity(ie, ge);
        ie.setProductVersion(ge.getProductVersion());
        return ie;
    }

    private ProductGeneratorEntity toGeneratorEntity(ProductInfoEntity bc) {
        return toGeneratorEntity(ProductGeneratorEntity.getConstructor(bc.getProductVersion()), bc);
    }
}
