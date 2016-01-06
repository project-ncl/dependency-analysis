package org.jboss.da.bc;

import org.apache.maven.scm.ScmException;
import org.jboss.da.bc.api.ProductBuildConfigurationGenerator;
import org.jboss.da.bc.model.EntryEntity;
import org.jboss.da.bc.model.ProductFinishResponse;
import org.jboss.da.bc.model.ProductGeneratorEntity;
import org.jboss.da.bc.model.ProductInfoEntity;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pnc.api.PNCRequestException;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.reports.api.SCMLocator;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.Optional;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path("/build-configuration/generate/product")
@Api(value = "product")
public class BuildConfigurationsProduct extends
        BuildConfigurationsREST<ProductInfoEntity, ProductFinishResponse> {

    @Inject
    ProductBuildConfigurationGenerator bcg;

    @Inject
    Logger log;

    @Override
    @ApiOperation(value = "Start initial analyse of product", response = ProductInfoEntity.class)
    public Response startAnalyse(EntryEntity entry) {
        return super.startAnalyse(entry);
    }

    @Override
    protected ProductInfoEntity start(SCMLocator scm, EntryEntity entry) throws ScmException,
            PomAnalysisException, CommunicationException {
        ProductGeneratorEntity entity = bcg.startBCGeneration(scm, entry.getName(),
                entry.getProductVersion());
        return toInfoEntity(entity);
    }

    @Override
    @POST
    // When annotations @Post, @Path and @Produces were on parent, the endpoint was not found (I dont know why, jbrazdil)
    @Path("/analyse-next-level")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Analyse next level of product dependencies",
            response = ProductInfoEntity.class)
    public Response analyseNextLevel(
            @ApiParam(value = "Detail information needed to create BCs") ProductInfoEntity bc) {
        return super.analyseNextLevel(bc);
    }

    @Override
    protected ProductInfoEntity nextLevel(ProductInfoEntity entity) throws CommunicationException {
        ProductGeneratorEntity ge = toGeneratorEntity(entity);
        ge = bcg.iterateBCGeneration(ge);
        return toInfoEntity(ge);
    }

    @Override
    @POST
    // When annotations @Post, @Path and @Produces were on parent, the endpoint was not found (I dont know why, jbrazdil)
    @Path("/finish-process")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Finish analysis and create BCs", response = ProductFinishResponse.class)
    public ProductFinishResponse finishAnalyse(@ApiParam(
            value = "Complete detail information needed to create BCs") ProductInfoEntity bc) {
        return super.finishAnalyse(bc);
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
