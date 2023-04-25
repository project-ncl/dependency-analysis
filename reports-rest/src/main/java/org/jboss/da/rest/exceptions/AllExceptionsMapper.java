package org.jboss.da.rest.exceptions;

import lombok.extern.slf4j.Slf4j;

import org.apache.maven.scm.ScmException;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.communication.repository.api.RepositoryException;
import org.jboss.da.model.rest.ErrorMessage;
import org.jboss.da.validation.ValidationException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import java.util.NoSuchElementException;

import static org.jboss.da.model.rest.ErrorMessage.ErrorType.COMMUNICATION_FAIL;
import static org.jboss.da.model.rest.ErrorMessage.ErrorType.ILLEGAL_ARGUMENTS;
import static org.jboss.da.model.rest.ErrorMessage.ErrorType.INPUT_VALIDATION;
import static org.jboss.da.model.rest.ErrorMessage.ErrorType.NO_RELATIONSHIP_FOUND;
import static org.jboss.da.model.rest.ErrorMessage.ErrorType.POM_ANALYSIS;
import static org.jboss.da.model.rest.ErrorMessage.ErrorType.SCM_ENDPOINT;
import static org.jboss.da.model.rest.ErrorMessage.ErrorType.UNEXPECTED_SERVER_ERR;

@Slf4j
@Provider
public class AllExceptionsMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception e) {
        if (e instanceof ValidationException) { // order of tests is important
            return ((ValidationException) e).getResponse();
        } else if (e instanceof org.jboss.pnc.common.alignment.ranking.exception.ValidationException) {
            return handleException("Constraint validation failed.", INPUT_VALIDATION, Response.Status.BAD_REQUEST, e);
        } else if (e instanceof NoSuchElementException) {
            return handleException("No relationship found", NO_RELATIONSHIP_FOUND, Response.Status.NOT_FOUND, e);
        } else if (e instanceof RepositoryException) {
            return handleException(
                    "Communication with remote repository failed",
                    COMMUNICATION_FAIL,
                    Response.Status.BAD_GATEWAY,
                    e);
        } else if (e instanceof CommunicationException) {
            return handleException("Exception thrown when communicating with external service", COMMUNICATION_FAIL, e);
        } else if (e instanceof PomAnalysisException) {
            return handleException("Exception thrown in POM analysis", POM_ANALYSIS, e);
        } else if (e instanceof ScmException) {
            return handleException("Exception thrown in SCM analysis", SCM_ENDPOINT, e);
        } else if (e instanceof IllegalArgumentException) {
            return handleException("Illegal arguments exception", ILLEGAL_ARGUMENTS, e);
        } else {
            return handleException("Unknown error while handling request", UNEXPECTED_SERVER_ERR, e);
        }
    }

    private Response handleException(String message, ErrorMessage.ErrorType errorType, Exception e) {
        return handleException(message, errorType, Response.Status.INTERNAL_SERVER_ERROR, e);
    }

    private Response handleException(
            String message,
            ErrorMessage.ErrorType errorType,
            Response.Status status,
            Exception e) {
        log.error(message, e);
        return Response.status(status).entity(new ErrorMessage(errorType, message, e.getMessage())).build();
    }
}
