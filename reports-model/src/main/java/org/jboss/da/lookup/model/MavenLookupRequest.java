package org.jboss.da.lookup.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.jboss.da.model.rest.GAV;

import java.util.Set;

@Data
@Jacksonized
@Builder
public class MavenLookupRequest {

    @NonNull
    private final Set<GAV> artifacts;

    @NonNull
    private final String mode;

    @JsonProperty(defaultValue = "false")
    private final boolean brewPullActive;

}
