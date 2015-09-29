package org.jboss.da.bc;

import org.jboss.da.bc.model.BuildConfiguration;
import org.jboss.da.bc.model.EntryEntity;
import org.jboss.da.bc.model.FinishResponse;
import org.jboss.da.bc.model.GAV;
import org.jboss.da.bc.model.InfoEntity;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.List;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path("/build-configuration/generate/product")
@Api(value = "/build-configuration/generate/product", description = "BC generator for product")
public class BuildConfigurationsProduct {

    @POST
    @Path("/start-process")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Start initial analyse of product", response = InfoEntity.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response succesfully generated") })
    public InfoEntity startAnalyse(
            @ApiParam(value = "Basic information about analysed product") EntryEntity product) {
        return getStartAnalyseResponse();
    }

    @POST
    @Path("/analyse-next-level")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Analyse next level of product dependencies", response = InfoEntity.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response succesfully generated") })
    public InfoEntity analyseNextLevel(
            @ApiParam(value = "Detail information needed to create BCs") InfoEntity bc) {
        return getAnalyseNextLevelResponse();
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

    // TODO remove after implementation
    private InfoEntity getStartAnalyseResponse() {
        InfoEntity bcset = new InfoEntity();
        bcset.setBcSetName("EAP BCSet");
        bcset.setName("EAP");
        BuildConfiguration bctop = new BuildConfiguration();
        bctop.setBcExists(true);
        bctop.setBuildScript("mvn clean deploy");
        bctop.setCloneRepo(false);
        bctop.setDescription("hibernate build configuration");
        bctop.setEnviromentId(1);
        bctop.setGav(new GAV("org.jboss", "hibernate-core", "10"));
        bctop.setInternallyBuilt("null");
        bctop.setName("hibernate bc");
        bctop.setPomPath("/pom.xml");
        bctop.setProjectId(12);
        bctop.setScmRevision("e9f99a8");
        bctop.setScmUrl("hibernate.url");
        bctop.setSelected(true);
        bctop.setUseExistingBc(true);
        bctop.setDependencies(new ArrayList<BuildConfiguration>());
        bcset.setTopLevelBc(bctop);
        return bcset;
    }

    private InfoEntity getAnalyseNextLevelResponse() {
        InfoEntity bcset = new InfoEntity();
        bcset.setBcSetName("EAP BCSet");
        bcset.setName("EAP");
        BuildConfiguration bctop = new BuildConfiguration();
        bctop.setBcExists(true);
        bctop.setBuildScript("mvn clean deploy");
        bctop.setCloneRepo(false);
        bctop.setDescription("hibernate build configuration");
        bctop.setEnviromentId(1);
        bctop.setGav(new GAV("org.jboss", "hibernate-core", "10"));
        bctop.setInternallyBuilt("null");
        bctop.setName("hibernate bc");
        bctop.setPomPath("/pom.xml");
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
        bc2.setEnviromentId(1);
        bc2.setGav(new GAV("org.junit", "junit", "2"));
        bc2.setInternallyBuilt("2.0.0.redhat-1");
        bc2.setName("junit bc");
        bc2.setPomPath("/pom.xml");
        bc2.setProjectId(12);
        bc2.setScmRevision("erf99a8");
        bc2.setScmUrl("git.junit.url");
        bc2.setSelected(false);
        bc2.setUseExistingBc(true);

        BuildConfiguration bc3 = new BuildConfiguration();
        bc3.setBcExists(false);
        bc3.setBuildScript("");
        bc3.setCloneRepo(false);
        bc3.setDescription("");
        bc3.setEnviromentId(1);
        bc3.setGav(new GAV("org.unknown", "unknown-unit", "2.8"));
        bc3.setInternallyBuilt(null);
        bc3.setName("");
        bc3.setPomPath("");
        bc3.setProjectId(null);
        bc3.setScmRevision("");
        bc3.setScmUrl("");
        bc3.setSelected(false);
        bc3.setUseExistingBc(false);
        //
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

}
