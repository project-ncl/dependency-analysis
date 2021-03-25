package org.jboss.da.lookup.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;

import org.jboss.da.model.rest.NPMPackage;

import java.util.Set;

@Data
@Jacksonized
@Builder
public class NPMLookupRequest {

    @NonNull
    private final Set<NPMPackage> packages;

    @NonNull
    private final String mode;

}
