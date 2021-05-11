package org.jboss.da.lookup.model;

import org.jboss.da.model.rest.GAV;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MavenLookupResult {

    @NonNull
    @JsonUnwrapped
    private GAV gav;

    private String bestMatchVersion;

}
