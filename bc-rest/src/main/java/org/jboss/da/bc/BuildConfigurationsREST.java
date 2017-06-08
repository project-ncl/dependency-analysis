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

import org.jboss.da.model.rest.ErrorMessage;
import org.jboss.da.validation.Validation;
import org.jboss.da.validation.ValidationException;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 * @param <I> Type of payload
 */
public abstract class BuildConfigurationsREST<I extends InfoEntity> {

    @Inject
    private Logger log;

    @Inject
    private Validation validation;

    protected abstract BuildConfigurationsFacade<I> getFacade();

    @POST
    @Path("/start-process")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Response failed") })
    public Response startAnalyse(EntryEntity entry) {
        try {
            validation.validation(entry, "Starting initial analyse failed");
            I infoEntity = getFacade().startAnalyse(entry);
            return Response.ok().entity(infoEntity).build();
        } catch (ScmException ex) {
            log.error("Error during SCM analysis occured", ex);
            return Response
                    .serverError()
                    .entity(new ErrorMessage(ErrorMessage.eType.SCM_ANALYSIS,
                            "Error during SCM analysis occured", ex.getMessage())).build();
        } catch (PomAnalysisException ex) {
            log.error("Error during POM analysis occured", ex);
            return Response
                    .serverError()
                    .entity(new ErrorMessage(ErrorMessage.eType.POM_ANALYSIS,
                            "Error during POM analysis occured.", ex.getMessage())).build();
        } catch (CommunicationException ex) {
            log.error("Error during communication occured", ex);
            return Response
                    .serverError()
                    .entity(new ErrorMessage(ErrorMessage.eType.COMMUNICATION_FAIL,
                            "Error during communication occured", ex.getMessage())).build();
        } catch (ValidationException e) {
            return e.getResponse();
        }

    }

    @ApiResponses(value = { @ApiResponse(code = 500, message = "Response failed",
            response = AnalyseNextLevelExceptionContainer.class) })
    public Response analyseNextLevel(I bc) {
        try {
            validation.validation(bc, "Analyse next level of dependencies failed");
            I infoEntity = getFacade().analyseNextLevel(bc);
            return Response.ok().entity(infoEntity).build();
        } catch (CommunicationException ex) {
            return Response.serverError().entity(new AnalyseNextLevelExceptionContainer(ex, bc))
                    .build();
        } catch (ValidationException e) {
            return e.getResponse();
        }
    }

    public Response finishAnalyse(I bc) {
        try {
            validation.validation(bc, "Finish analysis and create BCs failed");
            FinishResponse<I> infoEntity = getFacade().finishAnalyse(bc);
            return Response.ok().entity(infoEntity).build();
        } catch (ValidationException e) {
            return e.getResponse();
        }
    }
}
