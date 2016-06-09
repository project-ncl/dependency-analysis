package org.jboss.da.bc.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BcError {

    NO_NAME("No name specified",
            "There is no specified name in BC or name doesn't meet expected format"),
    NO_DEPENDENCY("No dependency found", "Failed to found dependencies in Aprox and SCM repository"),
    NO_EXISTING_BC("No existing BuildConfiguration",
            "There may be checked use existing BC ,but no BC exist for this GAV"), NO_ENV_SELECTED(
            "Enviroment ID is null",
            "There is no enviroment selected or another error occured and enviroment Id is null"),
    NO_PROJECT_SELECTED("Project ID is null",
            "There is no project selected or another error occured and project Id is null"),
    POM_EXCEPTION("POM error occured", "Error while parsing POM file occured"), SCM_EXCEPTION(
            "SCM error occured", "Error while cloning scm repository"), NO_SCM_URL(
            "SCM URL is not specified", "The project does not have specified SCM URL");

    private final String error;

    private final String message;

    private BcError(String error, String message) {
        this.error = error;
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    @JsonValue
    public String getValue() {
        return error + ":" + message;
    }

    @JsonCreator
    public static BcError toEnum(final String error) {
        if (error != null) {
            for (BcError e : BcError.values()) {
                if (error.equalsIgnoreCase(e.getError() + ":" + e.getMessage())) {
                    return e;
                }
            }
        }
        throw new IllegalArgumentException("Error message doesn't match predefined errors");
    }
}
