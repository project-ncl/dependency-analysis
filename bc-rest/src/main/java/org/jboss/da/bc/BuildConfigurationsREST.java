package org.jboss.da.bc;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.maven.scm.ScmException;
import org.jboss.da.bc.facade.BuildConfigurationsFacade;
import org.jboss.da.bc.model.rest.EntryEntity;
import org.jboss.da.bc.model.rest.FinishResponse;
import org.jboss.da.bc.model.rest.InfoEntity;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.slf4j.Logger;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 * @param <I> Type of payload
 */
public abstract class BuildConfigurationsREST<I extends InfoEntity> {

    @Inject
    private Logger log;

    protected abstract BuildConfigurationsFacade<I> getFacade();

    @POST
    @Path("/start-process")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Response failed") })
    public Response startAnalyse(EntryEntity entry) {
        try {
            I infoEntity = getFacade().startAnalyse(entry);
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
        try {
            I infoEntity = getFacade().analyseNextLevel(bc);
            return Response.ok().entity(infoEntity).build();
        } catch (CommunicationException ex) {
            return Response.serverError().entity(new AnalyseNextLevelExceptionContainer(ex, bc))
                    .build();
        }
    }

    public FinishResponse<I> finishAnalyse(I bc) {
        return getFacade().finishAnalyse(bc);
    }
}
