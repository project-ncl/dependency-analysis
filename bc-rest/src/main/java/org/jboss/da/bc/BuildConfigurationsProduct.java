package org.jboss.da.bc;

import org.jboss.da.bc.model.BuildConfiguration;
import org.jboss.da.bc.model.EntryEntity;
import org.jboss.da.bc.model.FinishResponse;
import org.jboss.da.bc.model.InfoEntity;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import org.apache.maven.scm.ScmException;
import org.jboss.da.bc.api.BuildConfigurationGenerator;
import org.jboss.da.bc.model.GeneratorEntity;
import org.jboss.da.bc.model.ProjectDetail;
import org.jboss.da.bc.model.ProjectHiearchy;
import org.jboss.da.communication.CommunicationException;
import org.jboss.da.communication.model.GAV;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.reports.api.SCMLocator;

@Path("/build-configuration/generate/product")
@Api(value = "/build-configuration/generate/product", description = "BC generator for product")
public class BuildConfigurationsProduct {

    @Inject
    BuildConfigurationGenerator bcg;

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
            GeneratorEntity entity = bcg.startBCGeneration(scm, product.getName());
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
    @ApiOperation(value = "Finish analyse and create BCs", response = FinishResponse.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response succesfully generated") })
    public FinishResponse finishAnalyse(@ApiParam(
            value = "Complete detail information needed to create BCs") InfoEntity bc) {
        return getFinishAnalyseResponse();
    }

    private InfoEntity getAnalyseNextLevelResponse() {
        InfoEntity bcset = new InfoEntity();
        bcset.setBcSetName("EAP BCSet");
        bcset.setName("EAP");
        bcset.setPomPath("/pom.xml");
        BuildConfiguration bctop = new BuildConfiguration();
        bctop.setBcExists(true);
        bctop.setBuildScript("mvn clean deploy");
        bctop.setCloneRepo(false);
        bctop.setDescription("hibernate build configuration");
        bctop.setEnvironmentId(1);
        bctop.setGav(new GAV("org.jboss", "hibernate-core", "10"));
        bctop.setInternallyBuilt("null");
        bctop.setName("hibernate bc");
        bctop.setProjectId(12);
        bctop.setScmRevision("e9f99a8");
        bctop.setScmUrl("git.hibernate.url");
        bctop.setSelected(true);
        bctop.setUseExistingBc(true);
        // 2nd level dep
        BuildConfiguration bc2 = new BuildConfiguration();
        bc2.setBcExists(true);
        bc2.setBuildScript("mvn clean deploy");
        bc2.setCloneRepo(false);
        bc2.setDescription("junit build configuration");
        bc2.setEnvironmentId(1);
        bc2.setGav(new GAV("org.junit", "junit", "2"));
        bc2.setInternallyBuilt("2.0.0.redhat-1");
        bc2.setName("junit bc");
        bc2.setProjectId(12);
        bc2.setScmRevision("erf99a8");
        bc2.setScmUrl("git.junit.url");
        bc2.setSelected(true);
        bc2.setUseExistingBc(true);

        BuildConfiguration bc3 = new BuildConfiguration();
        bc3.setBcExists(false);
        bc3.setBuildScript("");
        bc3.setCloneRepo(false);
        bc3.setDescription("");
        bc3.setEnvironmentId(1);
        bc3.setGav(new GAV("org.unknown", "unknown-unit", "2.8"));
        bc3.setInternallyBuilt(null);
        bc3.setName("");
        bc3.setProjectId(null);
        bc3.setScmRevision("");
        bc3.setScmUrl("");
        bc3.setSelected(false);
        bc3.setUseExistingBc(false);

        // 3rd level
        BuildConfiguration bc4 = new BuildConfiguration();
        bc4.setBcExists(false);
        bc4.setBuildScript("");
        bc4.setCloneRepo(false);
        bc4.setDescription("");
        bc4.setEnvironmentId(1);
        bc4.setGav(new GAV("org.dep", "unknown-unit-dep", "2.6"));
        bc4.setInternallyBuilt(null);
        bc4.setName("");
        bc4.setProjectId(null);
        bc4.setScmRevision("");
        bc4.setScmUrl("");
        bc4.setSelected(false);
        bc4.setUseExistingBc(false);
        bc2.setDependencies(Arrays.asList(bc4));

        List<BuildConfiguration> bclist = new ArrayList<BuildConfiguration>();
        bclist.add(bc2);
        bclist.add(bc3);
        bctop.setDependencies(bclist);

        bcset.setTopLevelBc(bctop);
        return bcset;
    }

    private FinishResponse getFinishAnalyseResponse() {
        FinishResponse response = new FinishResponse();
        response.setSuccess(true);
        response.setEntity(getAnalyseNextLevelResponse());
        return response;
    }

    private InfoEntity toInfoEntity(GeneratorEntity entity) {
        InfoEntity ie = new InfoEntity();
        ie.setBcSetName(entity.getBcSetName());
        ie.setName(entity.getName());
        ie.setPomPath(entity.getPomPath());
        ie.setTopLevelBc(toBuildConfiguration(entity.getToplevelBc()));
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

        GeneratorEntity ge = new GeneratorEntity(scml, bc.getName(), gav);

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

        if(bc.getDependencies() == null){
            ph.setDependencies(Optional.empty());
        }else{
            ph.setDependencies(Optional.of(bc.getDependencies().stream().map(dep -> toProjectHiearchy(dep)).collect(Collectors.toSet())));
        }
        return ph;
    }
}
