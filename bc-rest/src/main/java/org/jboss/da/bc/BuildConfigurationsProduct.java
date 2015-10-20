package org.jboss.da.bc;

import org.apache.maven.scm.ScmException;
import org.jboss.da.bc.api.BuildConfigurationGenerator;
import org.jboss.da.bc.model.BuildConfiguration;
import org.jboss.da.bc.model.EntryEntity;
import org.jboss.da.bc.model.FinishResponse;
import org.jboss.da.bc.model.GeneratorEntity;
import org.jboss.da.bc.model.InfoEntity;
import org.jboss.da.bc.model.ProjectDetail;
import org.jboss.da.bc.model.ProjectHiearchy;
import org.jboss.da.communication.CommunicationException;
import org.jboss.da.communication.aprox.api.AproxConnector;
import org.jboss.da.communication.model.GAV;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.reports.api.SCMLocator;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path("/build-configuration/generate/product")
@Api(value = "/build-configuration/generate/product", description = "BC generator for product")
public class BuildConfigurationsProduct {

    @Inject
    BuildConfigurationGenerator bcg;

    @Inject
    Logger log;

    @POST
    @Path("/start-process")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Start initial analyse of product", response = InfoEntity.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response succesfully generated"),
            @ApiResponse(code = 500, message = "Response failed") })
    public Response startAnalyse(
            @ApiParam(value = "Basic information about analysed product") EntryEntity product) {

        SCMLocator scm = new SCMLocator(product.getScmUrl(), product.getScmRevision(),
                product.getPomPath());
        try {
            GeneratorEntity entity = bcg.startBCGeneration(scm, product.getName(),
                    product.getProductVersion());
            return Response.ok().entity(toInfoEntity(entity)).build();
        } catch (ScmException | PomAnalysisException | CommunicationException ex) {
            return Response.serverError().entity(ex).build();
        }
    }

    @POST
    @Path("/analyse-next-level")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Analyse next level of product dependencies", response = InfoEntity.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response succesfully generated"),
            @ApiResponse(code = 500, message = "Response failed") })
    public Response analyseNextLevel(
            @ApiParam(value = "Detail information needed to create BCs") InfoEntity bc) {
        try {
            GeneratorEntity ge = toGeneratorEntity(bc);
            ge = bcg.iterateBCGeneration(ge);
            return Response.ok().entity(toInfoEntity(ge)).build();
        } catch (CommunicationException ex) {
            return Response.serverError().entity(new AnalyseNextLevelExceptionContainer(ex, bc))
                    .build();
        }
    }

    @POST
    @Path("/finish-process")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Finish analysis and create BCs", response = FinishResponse.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response succesfully generated") })
    public FinishResponse finishAnalyse(@ApiParam(
            value = "Complete detail information needed to create BCs") InfoEntity bc) {
        FinishResponse response = new FinishResponse();
        response.setEntity(bc);
        try {
            GeneratorEntity ge = toGeneratorEntity(bc);
            response.setProductVersionId(bcg.createBC(ge));
            response.setSuccess(true);
            return response;
        } catch (Exception ex) {
            log.warn("Could not finish: ", ex);
            response.setSuccess(false);
            response.setErrorType(ex.getClass().toString());
            response.setMessage(ex.getMessage());
            return response;
        }
    }

    private InfoEntity toInfoEntity(GeneratorEntity entity) {
        InfoEntity ie = new InfoEntity();
        ie.setBcSetName(entity.getBcSetName());
        ie.setName(entity.getName());
        ie.setPomPath(entity.getPomPath());
        ie.setTopLevelBc(toBuildConfiguration(entity.getToplevelBc()));
        ie.setProductVersion(entity.getProductVersion());
        return ie;
    }

    private BuildConfiguration toBuildConfiguration(ProjectHiearchy ph) {
        ProjectDetail p = ph.getProject();
        BuildConfiguration bc = new BuildConfiguration();

        bc.setBcExists(p.isBcExists());
        bc.setBuildScript(p.getBuildScript());
        bc.setCloneRepo(p.isCloneRepo());
        bc.setDescription(p.getDescription());
        bc.setEnvironmentId(p.getEnvironmentId());
        bc.setGav(p.getGav());
        bc.setInternallyBuilt(p.getInternallyBuilt().orElse(null));
        bc.setName(p.getName());
        bc.setProjectId(p.getProjectId());
        bc.setScmRevision(p.getScmRevision());
        bc.setScmUrl(p.getScmUrl());
        bc.setSelected(ph.isSelected());
        bc.setUseExistingBc(p.isUseExistingBc());

        List<BuildConfiguration> dependencies = ph.getDependencies()
                .map(deps -> deps.stream()
                        .map(x -> toBuildConfiguration(x))
                        .collect(Collectors.toList()))
                .orElse(null);
        bc.setDependencies(dependencies);

        return bc;
    }

    private GeneratorEntity toGeneratorEntity(InfoEntity bc) {
        String url = bc.getTopLevelBc().getScmUrl();
        String revision = bc.getTopLevelBc().getScmRevision();
        String path = bc.getPomPath();
        SCMLocator scml = new SCMLocator(url, revision, path);
        GAV gav = bc.getTopLevelBc().getGav();

        GeneratorEntity ge = new GeneratorEntity(scml, bc.getName(), gav, bc.getProductVersion());

        ge.setBcSetName(bc.getBcSetName());
        ge.setToplevelBc(toProjectHiearchy(bc.getTopLevelBc()));
        return ge;
    }

    private ProjectHiearchy toProjectHiearchy(BuildConfiguration bc) {
        ProjectDetail pd = new ProjectDetail(bc.getGav());
        pd.setBcExists(bc.isBcExists());
        pd.setBuildScript(bc.getBuildScript());
        pd.setCloneRepo(bc.isCloneRepo());
        pd.setDescription(bc.getDescription());
        pd.setEnvironmentId(bc.getEnvironmentId());
        pd.setInternallyBuilt(Optional.ofNullable(bc.getInternallyBuilt()));
        pd.setName(bc.getName());
        pd.setProjectId(bc.getProjectId());
        pd.setScmRevision(bc.getScmRevision());
        pd.setScmUrl(bc.getScmUrl());
        pd.setUseExistingBc(bc.isUseExistingBc());

        ProjectHiearchy ph = new ProjectHiearchy(pd, bc.isSelected());

        if (bc.getDependencies() == null) {
            ph.setDependencies(Optional.empty());
        } else {
            ph.setDependencies(Optional.of(bc.getDependencies().stream()
                    .map(dep -> toProjectHiearchy(dep)).collect(Collectors.toSet())));
        }
        return ph;
    }
}
