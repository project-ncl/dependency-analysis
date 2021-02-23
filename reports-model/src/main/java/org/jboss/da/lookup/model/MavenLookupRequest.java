package org.jboss.da.lookup.model;

import java.util.Set;

import org.jboss.da.model.rest.GAV;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

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
