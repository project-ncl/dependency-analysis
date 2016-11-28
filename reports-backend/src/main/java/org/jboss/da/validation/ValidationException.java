package org.jboss.da.validation;

import org.jboss.da.model.rest.ErrorMessage;
import static org.jboss.da.model.rest.ErrorMessage.eType.INPUT_VALIDATION;
import org.jboss.da.model.rest.validators.Validations;

import javax.ws.rs.core.Response;

import lombok.Getter;
import lombok.NonNull;

/**
 *
 * @author jbrazdil
 */
public class ValidationException extends Exception {

    @Getter
    @NonNull
    private final Validations validations;

    public ValidationException(String message, Validations validations) {
        super(message);
        this.validations = validations;
    }

    public Response getResponse() {
        ErrorMessage errorMessage = new ErrorMessage(INPUT_VALIDATION, getMessage(), validations);
        return Response.status(Response.Status.BAD_REQUEST).entity(errorMessage).build();
    }

}
