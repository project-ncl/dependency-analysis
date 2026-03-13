package org.jboss.da.lookup.model;

import org.jboss.da.model.rest.GAV;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MavenLookupResult extends MavenResult {

    private String bestMatchVersion;

    public MavenLookupResult(@NonNull GAV gav, String bestMatchVersion) {
        super(gav);
        this.bestMatchVersion = bestMatchVersion;
    }

}
