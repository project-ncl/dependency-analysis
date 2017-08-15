package org.jboss.da.bc.model.rest;

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.jboss.da.model.rest.validators.ScmUrl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@NoArgsConstructor
@EqualsAndHashCode
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class EntryEntity {

    @Getter
    @Setter
    @ScmUrl
    protected String scmUrl;

    @Getter
    @Setter
    protected String pomPath;

    @Getter
    @Setter
    protected String scmRevision;

    @Getter
    @Setter
    protected String productVersion;

    @Getter
    @Setter
    protected int id;

    @Getter
    @Setter
    protected List<String> repositories;

}
