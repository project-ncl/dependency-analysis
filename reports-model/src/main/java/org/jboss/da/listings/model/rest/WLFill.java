package org.jboss.da.listings.model.rest;

import java.util.Collections;
import java.util.List;

import org.jboss.da.model.rest.validators.ScmUrl;

import com.fasterxml.jackson.annotation.JsonRootName;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@JsonRootName(value = "productArtifact")
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class WLFill {

    @Getter
    @Setter
    @ScmUrl
    private String scmUrl;

    @Getter
    @Setter
    private String revision;

    @Getter
    @Setter
    private String pomPath;

    @Getter
    @Setter
    private List<String> repositories = Collections.emptyList();

    @Getter
    private long productId;
}
