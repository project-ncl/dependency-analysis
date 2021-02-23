package org.jboss.da.lookup.model;

import org.jboss.da.model.rest.GAV;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import lombok.Data;
import lombok.NonNull;

@Data
public class MavenLookupResult {

    @NonNull
    @JsonUnwrapped
    private GAV gav;

    private final String bestMatchVersion;

}
