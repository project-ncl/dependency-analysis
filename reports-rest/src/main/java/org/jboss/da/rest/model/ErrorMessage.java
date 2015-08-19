package org.jboss.da.rest.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
@RequiredArgsConstructor
public class ErrorMessage {

    @Getter
    @NonNull
    private final String message;
}
