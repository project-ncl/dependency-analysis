package org.jboss.da.bc.facade;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.maven.scm.ScmException;
import org.jboss.da.bc.model.backend.GeneratorEntity;
import org.jboss.da.bc.model.backend.GeneratorEntity.EntityConstructor;
import org.jboss.da.bc.model.backend.ProjectDetail;
import org.jboss.da.bc.model.backend.ProjectHiearchy;
import org.jboss.da.bc.model.rest.BuildConfiguration;
import org.jboss.da.bc.model.rest.EntryEntity;
import org.jboss.da.bc.model.rest.FinishResponse;
import org.jboss.da.bc.model.rest.InfoEntity;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pnc.api.PNCRequestException;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.model.rest.GAV;
import org.jboss.da.reports.model.api.SCMLocator;
import org.slf4j.Logger;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 * @param <I> Type of the payload
 */
public abstract class AbstractBuildConfigurationsFacade<I extends InfoEntity> implements
        BuildConfigurationsFacade<I> {

    @Inject
    private Logger log;

    @Override
    public I startAnalyse(EntryEntity entry) throws ScmException, PomAnalysisException,
            CommunicationException {
        log.info("Start process " + entry);
        SCMLocator scm;
        if (entry.getScmUrl() == null || entry.getScmRevision() == null
                || entry.getScmUrl().isEmpty() || entry.getScmRevision().isEmpty()) {
            scm = SCMLocator.external(entry.getExternalScmUrl(), entry.getExternalScmRevision(),
                    entry.getPomPath(), entry.getRepositories());
        } else {
            scm = SCMLocator.internal(entry.getScmUrl(), entry.getScmRevision(),
                    entry.getPomPath(), entry.getRepositories());
        }
        return start(scm, entry);
    }

    @Override
    public I analyseNextLevel(I bc) throws CommunicationException {
        log.info("Analyze next level " + bc);
        return nextLevel(bc);
    }

    @Override
    public FinishResponse<I> finishAnalyse(I bc) {
        log.info("Finish process " + bc);
        FinishResponse<I> response = getFinishResponse(bc);
        try {
            Optional<Integer> id = finish(bc);
            response.setSuccess(id.isPresent());
            id.ifPresent(x -> response.setCreatedEntityId(id.get()));
            return response;
        } catch (CommunicationException | PNCRequestException ex) {
            log.warn("Could not finish: ", ex);
            response.setSuccess(false);
            response.setErrorType(ex.getClass().toString());
            response.setMessage(ex.getMessage());
            return response;
        }
    }

    protected abstract I start(SCMLocator scm, EntryEntity entry) throws ScmException,
            PomAnalysisException, CommunicationException;

    protected abstract I nextLevel(I entity) throws CommunicationException;

    protected abstract Optional<Integer> finish(I entity) throws CommunicationException,
            PNCRequestException;

    protected abstract FinishResponse<I> getFinishResponse(I entity);

    protected ProjectHiearchy toProjectHiearchy(BuildConfiguration bc) {
        ProjectDetail pd = new ProjectDetail(bc.getGav());
        pd.setExistingBCs(bc.getExistingBCs());
        pd.setBuildScript(bc.getBuildScript());
        pd.setDescription(bc.getDescription());
        pd.setEnvironmentId(bc.getEnvironmentId());
        pd.setInternallyBuilt(Optional.ofNullable(bc.getInternallyBuilt()));
        pd.setAvailableVersions(bc.getAvailableVersions());
        pd.setName(bc.getName());
        pd.setProjectId(bc.getProjectId());
        if(bc.getScmUrl() != null && !bc.getScmUrl().isEmpty()){
            pd.setInternalSCM(bc.getScmUrl(), bc.getScmRevision());
        }
        if(bc.getExternalScmUrl() != null && !bc.getExternalScmUrl().isEmpty()){
            pd.setExternalSCM(bc.getExternalScmUrl(), bc.getExternalScmRevision());
        }
        if(bc.getErrors() != null)
            pd.setErrors(bc.getErrors());
        pd.setBcId(bc.getBcId());

        ProjectHiearchy ph = new ProjectHiearchy(pd, bc.isSelected());

        if (bc.getDependencies() != null) {
            ph.setDependencies(bc.getDependencies().stream()
                    .map(dep -> toProjectHiearchy(dep)).collect(Collectors.toSet()));
        }
        ph.setAnalysisStatus(bc.getAnalysisStatus());
        return ph;
    }

    protected BuildConfiguration toBuildConfiguration(ProjectHiearchy ph) {
        ProjectDetail p = ph.getProject();
        BuildConfiguration bc = new BuildConfiguration();

        bc.setExistingBCs(p.getExistingBCs());
        bc.setBuildScript(p.getBuildScript());
        bc.setDescription(p.getDescription());
        bc.setEnvironmentId(p.getEnvironmentId());
        bc.setGav(p.getGav());
        bc.setInternallyBuilt(p.getInternallyBuilt().orElse(null));
        bc.setAvailableVersions(p.getAvailableVersions());
        bc.setName(p.getName());
        bc.setProjectId(p.getProjectId());
        p.getInternalSCM().ifPresent(scm -> {
            bc.setScmUrl(scm.getUrl());
            bc.setScmRevision(scm.getRevision());
        });
        p.getExternalSCM().ifPresent(scm -> {
            bc.setExternalScmUrl(scm.getUrl());
            bc.setExternalScmRevision(scm.getRevision());
        });
        bc.setSelected(ph.isSelected());
        bc.setBcId(p.getBcId());
        bc.setAnalysisStatus(ph.getAnalysisStatus());
        bc.setErrors(p.getErrors());

        List<BuildConfiguration> dependencies = ph.getDependencies()
                .stream()
                .map(x -> toBuildConfiguration(x))
                .collect(Collectors.toList());
        bc.setDependencies(dependencies);

        return bc;
    }

    protected void fillInfoEntity(InfoEntity ie, GeneratorEntity ge) {
        ie.setBcSetName(ge.getBcSetName());
        ie.setId(ge.getId());
        ie.setPomPath(ge.getPomPath());
        ie.setTopLevelBc(toBuildConfiguration(ge.getToplevelBc()));
    }

    protected <T extends GeneratorEntity> T toGeneratorEntity(EntityConstructor<T> constructor,
            InfoEntity ie) {
        SCMLocator scml;
        final BuildConfiguration bc = ie.getTopLevelBc();
        if (bc.getScmUrl() == null || bc.getScmUrl().isEmpty()) {
            scml = SCMLocator.external(bc.getExternalScmUrl(), bc.getExternalScmRevision(),
                    ie.getPomPath());
        } else {
            scml = SCMLocator.internal(bc.getScmUrl(), bc.getScmRevision(), ie.getPomPath());
        }

        T ge = constructor.construct(scml, ie.getId(), bc.getGav());

        ge.setBcSetName(ie.getBcSetName());
        ge.setToplevelBc(toProjectHiearchy(bc));
        ge.setAuthToken(ie.getSecurityToken());
        return ge;
    }

}
