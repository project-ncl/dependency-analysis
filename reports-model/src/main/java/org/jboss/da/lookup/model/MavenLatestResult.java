package org.jboss.da.lookup.model;

import lombok.EqualsAndHashCode;
import org.jboss.da.model.rest.GAV;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MavenLatestResult extends MavenResult {

    private String latestVersion;

    public MavenLatestResult(@NonNull GAV gav, String latestVersion) {
        super(gav);
        this.latestVersion = latestVersion;
    }

}
