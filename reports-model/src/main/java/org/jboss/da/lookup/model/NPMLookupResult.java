package org.jboss.da.lookup.model;

import org.jboss.da.model.rest.GAV;
import org.jboss.da.model.rest.NPMPackage;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Data
public class NPMLookupResult {

    @NonNull
    @JsonUnwrapped
    private NPMPackage npmPackage;

    private final String bestMatchVersion;
}
