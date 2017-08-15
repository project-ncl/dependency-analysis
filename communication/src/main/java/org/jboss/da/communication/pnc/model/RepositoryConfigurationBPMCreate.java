package org.jboss.da.communication.pnc.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 *
 * @author Honza Brázdil <janinko.g@gmail.com>
 */
@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@JsonDeserialize(
        builder = RepositoryConfigurationBPMCreate.RepositoryConfigurationCreateBuilder.class)
public class RepositoryConfigurationBPMCreate implements Serializable {

    @NonNull
    private final String scmUrl;

    private final Boolean preBuildSyncEnabled;

    private final BuildConfigurationBPMCreate buildConfigurationRest;

    public RepositoryConfigurationBPMCreate(String scmUrl) {
        this(scmUrl, null, null);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class RepositoryConfigurationCreateBuilder {
    }
}
