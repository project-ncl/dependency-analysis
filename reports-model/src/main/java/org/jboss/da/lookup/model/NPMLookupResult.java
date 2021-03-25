package org.jboss.da.lookup.model;

import lombok.Data;
import lombok.NonNull;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import org.jboss.da.model.rest.NPMPackage;

@Data
public class NPMLookupResult {

    @NonNull
    @JsonUnwrapped
    private NPMPackage npmPackage;

    private final String bestMatchVersion;
}
