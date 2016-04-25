package org.jboss.da.bc;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
import org.jboss.da.reports.api.SCMLocator;
import org.slf4j.Logger;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 * @param <I> Type of payload
 * @param <O> Type of response from finish method
 */
public abstract class BuildConfigurationsREST<I extends InfoEntity, O extends FinishResponse> {

    @Inject
    private Logger log;

    @POST
    @Path("/start-process")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Response failed") })
    public Response startAnalyse(EntryEntity entry) {
        log.info("Start process " + entry);
        SCMLocator scm = new SCMLocator(entry.getScmUrl(), entry.getScmRevision(),
                entry.getPomPath(), entry.getRepositories());
        try {
            I infoEntity = start(scm, entry);
            return Response.ok().entity(infoEntity).build();
        } catch (ScmException ex) {
            log.error("Error during SCM analysis occured", ex);
            return Response.serverError().entity("Error during SCM analysis occured").build();
        } catch (PomAnalysisException ex) {
            log.error("Error during POM analysis occured", ex);
            return Response.serverError().entity("Error during POM analysis occured.").build();
        } catch (CommunicationException ex) {
            log.error("Error during communication occured", ex);
            return Response.serverError().entity("Error during communication occured").build();
        }
    }

    @ApiResponses(value = { @ApiResponse(code = 500, message = "Response failed",
            response = AnalyseNextLevelExceptionContainer.class) })
    public Response analyseNextLevel(I bc) {
        log.info("Analyze next level " + bc);
        try {
            I infoEntity = nextLevel(bc);
            return Response.ok().entity(infoEntity).build();
        } catch (CommunicationException ex) {
            return Response.serverError().entity(new AnalyseNextLevelExceptionContainer(ex, bc))
                    .build();
        }
    }

    public O finishAnalyse(I bc) {
        log.info("Finish process " + bc);
        O response = getFinishResponse(bc);
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

    protected abstract O getFinishResponse(I entity);

    protected ProjectHiearchy toProjectHiearchy(BuildConfiguration bc) {
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
        if(bc.getErrors() != null)
            pd.setErrors(bc.getErrors());
        pd.setUseExistingBc(bc.isUseExistingBc());
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
        bc.setBcId(p.getBcId());
        bc.setSelected(ph.isSelected());
        bc.setUseExistingBc(p.isUseExistingBc());
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
        String url = ie.getTopLevelBc().getScmUrl();
        String revision = ie.getTopLevelBc().getScmRevision();
        String path = ie.getPomPath();
        SCMLocator scml = new SCMLocator(url, revision, path);
        GAV gav = ie.getTopLevelBc().getGav();

        T ge = constructor.construct(scml, ie.getId(), gav);

        ge.setBcSetName(ie.getBcSetName());
        ge.setToplevelBc(toProjectHiearchy(ie.getTopLevelBc()));
        return ge;
    }

}
