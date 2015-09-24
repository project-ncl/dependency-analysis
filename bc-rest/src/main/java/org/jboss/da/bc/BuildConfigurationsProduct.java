package org.jboss.da.bc;

import org.jboss.da.bc.model.BuildConfiguration;
import org.jboss.da.bc.model.EntryEntity;
import org.jboss.da.bc.model.FinishResponse;
import org.jboss.da.bc.model.InfoEntity;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
        InfoEntity info = new InfoEntity();
        info.setTopLevelBc(new BuildConfiguration());
        return info;
    }

    @POST
    @Path("/analyse-next-level")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Analyse next level of product dependencies", response = InfoEntity.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response succesfully generated") })
    public InfoEntity analyseNextLevel(
            @ApiParam(value = "Detail information needed to create BCs") InfoEntity bc) {
        InfoEntity info = new InfoEntity();
        info.setTopLevelBc(new BuildConfiguration());
        return info;
    }

    @POST
    @Path("/finish-process")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Finish analyse and create BCs", response = FinishResponse.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response succesfully generated") })
    public FinishResponse finishAnalyse(@ApiParam(
            value = "Complete detail information needed to create BCs") InfoEntity bc) {
        InfoEntity info = new InfoEntity();
        info.setTopLevelBc(new BuildConfiguration());
        FinishResponse response = new FinishResponse();
        response.setEntity(info);
        return response;
    }

}
