package org.jboss.da.bc.model;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.codehaus.jackson.annotate.JsonUnwrapped;

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
    private Optional<Set<ProjectHiearchy>> dependencies = Optional.of(Collections.emptySet());

    public ProjectHiearchy(ProjectDetail project, boolean nextLevel) {
        this.project = project;
        this.selected = nextLevel;
    }

    public void setDependencies(Optional<Set<ProjectHiearchy>> dependencies) {
        this.dependencies = dependencies;
    }

}
