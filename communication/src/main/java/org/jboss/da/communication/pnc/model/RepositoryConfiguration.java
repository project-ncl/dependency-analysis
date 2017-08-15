package org.jboss.da.communication.pnc.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(builder = RepositoryConfiguration.RepositoryConfigurationBuilder.class)
public class RepositoryConfiguration {

    private final Integer id;

    private final String internalUrl;

    private final String externalUrl;

    private final boolean preBuildSyncEnabled;

    @JsonPOJOBuilder(withPrefix = "")
    public static class RepositoryConfigurationBuilder {
    }
}
