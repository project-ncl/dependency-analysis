package org.jboss.da.bc;

import org.apache.maven.scm.ScmException;
import org.jboss.da.bc.api.ProjectBuildConfigurationGenerator;
import org.jboss.da.bc.model.EntryEntity;
import org.jboss.da.bc.model.ProjectFinishResponse;
import org.jboss.da.bc.model.ProjectGeneratorEntity;
import org.jboss.da.bc.model.ProjectInfoEntity;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pnc.api.PNCRequestException;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.reports.api.SCMLocator;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import java.util.Optional;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Path("/build-configuration/generate/project")
@Api(value = "project")
public class BuildConfigurationsProject extends
        BuildConfigurationsREST<ProjectInfoEntity, ProjectFinishResponse> {

    @Inject
    ProjectBuildConfigurationGenerator bcg;

    @Inject
    Logger log;

    @Override
    @ApiOperation(value = "Start initial analyse of project", response = ProjectInfoEntity.class)
    public Response startAnalyse(EntryEntity entry) {
        return super.startAnalyse(entry);
    }

    @Override
    protected ProjectInfoEntity start(SCMLocator scm, EntryEntity entry) throws ScmException,
            PomAnalysisException, CommunicationException {
        ProjectGeneratorEntity entity = bcg.startBCGeneration(scm, entry.getName());
        return toInfoEntity(entity);
    }

    @Override
    @ApiOperation(value = "Analyse next level of project dependencies",
            response = ProjectInfoEntity.class)
    public Response analyseNextLevel(
            @ApiParam(value = "Detail information needed to create BCs") ProjectInfoEntity bc) {
        return super.analyseNextLevel(bc);
    }

    @Override
    protected ProjectInfoEntity nextLevel(ProjectInfoEntity entity) throws CommunicationException {
        ProjectGeneratorEntity ge = toGeneratorEntity(entity);
        ge = bcg.iterateBCGeneration(ge);
        return toInfoEntity(ge);
    }

    @Override
    @ApiOperation(value = "Finish analysis and create BCs", response = ProjectFinishResponse.class)
    public ProjectFinishResponse finishAnalyse(@ApiParam(
            value = "Complete detail information needed to create BCs") ProjectInfoEntity bc) {
        return super.finishAnalyse(bc);
    }

    @Override
    protected Optional<Integer> finish(ProjectInfoEntity entity) throws CommunicationException,
            PNCRequestException {
        ProjectGeneratorEntity ge = toGeneratorEntity(entity);
        return bcg.createBC(ge);
    }

    @Override
    protected ProjectFinishResponse getFinishResponse(ProjectInfoEntity entity) {
        ProjectFinishResponse response = new ProjectFinishResponse();
        response.setEntity(entity);
        return response;
    }

    private ProjectInfoEntity toInfoEntity(ProjectGeneratorEntity ge) {
        ProjectInfoEntity ie = new ProjectInfoEntity();
        fillInfoEntity(ie, ge);
        return ie;
    }

    private ProjectGeneratorEntity toGeneratorEntity(ProjectInfoEntity bc) {
        return toGeneratorEntity(ProjectGeneratorEntity.getConstructor(), bc);
    }

}
