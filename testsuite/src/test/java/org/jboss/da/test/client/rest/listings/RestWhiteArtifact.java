package org.jboss.da.test.client.rest.listings;

import com.fasterxml.jackson.annotation.JsonRootName;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@JsonRootName(value = "artifact")
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class RestWhiteArtifact {

    @Getter
    @Setter
    protected String name;

    @Getter
    @Setter
    protected String version;

    @Getter
    @Setter
    protected String supportStatus;

    @Getter
    @Setter
    protected RestArtifact gav;
}
