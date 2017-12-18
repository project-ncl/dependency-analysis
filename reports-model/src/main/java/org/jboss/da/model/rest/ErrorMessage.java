package org.jboss.da.model.rest;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@RequiredArgsConstructor
public class ErrorMessage {

    public static enum ErrorType {
        BLACKLIST, UNEXPECTED_SERVER_ERR, PRODUCT_NOT_FOUND, PARAMS_REQUIRED,
        NO_RELATIONSHIP_FOUND, GA_NOT_FOUND, COMMUNICATION_FAIL, SCM_ENDPOINT, POM_ANALYSIS,
        ILLEGAL_ARGUMENTS, INCORRECT_DATA, SCM_ANALYSIS, INPUT_VALIDATION
    };

    @Getter
    private final ErrorType errorType;

    @Getter
    @NonNull
    private final String errorMessage;

    @Getter
    private final Object details;

}
