package org.jboss.da.bc.facade;

import org.apache.maven.scm.ScmException;
import org.jboss.da.bc.api.ProjectBuildConfigurationGenerator;
import org.jboss.da.bc.model.backend.ProjectGeneratorEntity;
import org.jboss.da.bc.model.rest.EntryEntity;
import org.jboss.da.bc.model.rest.ProjectFinishResponse;
import org.jboss.da.bc.model.rest.ProjectInfoEntity;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pnc.api.PNCRequestException;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.reports.model.api.SCMLocator;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.util.Optional;

@ApplicationScoped
public class BuildConfigurationsProjectFacade extends
        AbstractBuildConfigurationsFacade<ProjectInfoEntity> {

    @Inject
    ProjectBuildConfigurationGenerator bcg;

    @Inject
    Logger log;

    @Override
    protected ProjectInfoEntity start(SCMLocator scm, EntryEntity entry) throws ScmException,
            PomAnalysisException, CommunicationException {
        ProjectGeneratorEntity entity = bcg.startBCGeneration(scm, entry.getId());
        return toInfoEntity(entity);
    }

    @Override
    protected ProjectInfoEntity nextLevel(ProjectInfoEntity entity) throws CommunicationException {
        ProjectGeneratorEntity ge = toGeneratorEntity(entity);
        ge = bcg.iterateBCGeneration(ge);
        return toInfoEntity(ge);
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
