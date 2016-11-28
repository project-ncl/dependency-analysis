package org.jboss.da.bc;

import org.jboss.da.bc.facade.BuildConfigurationsFacade;
import org.jboss.da.bc.facade.BuildConfigurationsProjectFacade;
import org.jboss.da.bc.model.rest.EntryEntity;
import org.jboss.da.bc.model.rest.ProjectFinishResponse;
import org.jboss.da.bc.model.rest.ProjectInfoEntity;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Path("/build-configuration/generate/project")
@Api(value = "project")
public class BuildConfigurationsProject extends BuildConfigurationsREST<ProjectInfoEntity> {

    @Inject
    BuildConfigurationsProjectFacade bcpf;

    @Inject
    Logger log;

    @Override
    @ApiOperation(value = "Start initial analyse of project", response = ProjectInfoEntity.class)
    public Response startAnalyse(EntryEntity entry) {
        return super.startAnalyse(entry);
    }

    @Override
    @POST
    // When annotations @Post, @Path and @Produces were on parent, the endpoint was not found (I dont know why, jbrazdil)
    @Path("/analyse-next-level")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Analyse next level of project dependencies",
            response = ProjectInfoEntity.class)
    public Response analyseNextLevel(
            @ApiParam(value = "Detail information needed to create BCs") ProjectInfoEntity bc) {
        return super.analyseNextLevel(bc);
    }

    @Override
    @POST
    // When annotations @Post, @Path and @Produces were on parent, the endpoint was not found (I dont know why, jbrazdil)
    @Path("/finish-process")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Finish analysis and create BCs", response = ProjectFinishResponse.class)
    public Response finishAnalyse(@ApiParam(
            value = "Complete detail information needed to create BCs") ProjectInfoEntity bc) {
        return super.finishAnalyse(bc);
    }

    @Override
    protected BuildConfigurationsFacade<ProjectInfoEntity> getFacade() {
        return bcpf;
    }
}
