package org.jboss.da.bc.model.rest;

import org.jboss.da.bc.model.DependencyAnalysisStatus;

import java.util.EnumSet;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.jboss.da.bc.model.BcError;
import org.jboss.da.model.rest.GAV;
import org.jboss.da.model.rest.validators.ScmUrl;

@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class BuildConfiguration {

    @Getter
    @Setter
    protected DependencyAnalysisStatus analysisStatus;

    @Getter
    @Setter
    @ScmUrl
    protected String scmUrl;

    @Getter
    @Setter
    @ScmUrl
    protected String externalScmUrl;

    @Getter
    @Setter
    protected String scmRevision;

    @Getter
    @Setter
    protected String externalScmRevision;

    @Getter
    @Setter
    protected Integer projectId;

    @Getter
    @Setter
    protected String buildScript;

    @Getter
    @Setter
    protected String name;

    @Getter
    @Setter
    protected String description;

    @Getter
    @Setter
    protected String internallyBuilt;

    @Getter
    @Setter
    protected List<String> availableVersions;

    @Getter
    @Setter
    protected Integer bcId;

    @Getter
    @Setter
    private List<Integer> existingBCs;

    @Getter
    @Setter
    protected GAV gav;

    @Getter
    @Setter
    protected Integer environmentId;

    @Getter
    @Setter
    protected List<BuildConfiguration> dependencies;

    @Getter
    @Setter
    protected boolean selected;

    @Getter
    @Setter
    protected EnumSet<BcError> errors;

}
