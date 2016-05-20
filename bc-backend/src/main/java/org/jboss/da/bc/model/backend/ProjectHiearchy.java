package org.jboss.da.bc.model.backend;

import java.util.Collections;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.jboss.da.bc.model.DependencyAnalysisStatus;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
@ToString
public class ProjectHiearchy {

    @Getter
    @Setter
    private DependencyAnalysisStatus analysisStatus = DependencyAnalysisStatus.NOT_ANALYSED;

    @Getter
    @Setter
    @JsonUnwrapped
    private ProjectDetail project;

    @Getter
    @Setter
    private boolean selected;

    @Getter
    private Set<ProjectHiearchy> dependencies = Collections.emptySet();

    public ProjectHiearchy(ProjectDetail project, boolean nextLevel) {
        this.project = project;
        this.selected = nextLevel;
    }

    public void setDependencies(Set<ProjectHiearchy> dependencies) {
        this.dependencies = dependencies;
    }

}
