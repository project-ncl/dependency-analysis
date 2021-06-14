package org.jboss.da.lookup.model;

import java.util.Set;

import org.jboss.da.model.rest.GAV;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class MavenLatestRequest {

    @NonNull
    private final Set<GAV> artifacts;

    @NonNull
    private final String mode;

}
