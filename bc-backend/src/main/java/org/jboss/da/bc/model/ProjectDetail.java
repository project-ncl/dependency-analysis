package org.jboss.da.bc.model;

import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.jboss.da.communication.model.GAV;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
@ToString
public class ProjectDetail {

    @Getter
    @Setter
    private String scmUrl; // required

    @Getter
    @Setter
    private String scmRevision; // required, all

    @Getter
    @Setter
    private Integer projectId; // required, non-first

    @Getter
    @Setter
    @NonNull
    private String buildScript = ""; // required

    @Getter
    @Setter
    private String name; // generated, modifiable

    @Getter
    @Setter
    private String description; // generated, modifiable

    @Getter
    @NonNull
    private final GAV gav; // generated, unmodifiable

    @Getter
    @Setter
    private Integer environmentId; // required

    @Getter
    @Setter
    private Integer bcId;

    @Getter
    @Setter
    private boolean bcExists = false; // generated, unmodifiable

    @Getter
    @Setter
    private boolean useExistingBc = false; // required, default true when bcExists

    @Getter
    @Setter
    private Optional<String> internallyBuilt = Optional.empty(); // generated, unmodifiable

    @Getter
    @Setter
    private boolean cloneRepo = true; // required, default true

    public ProjectDetail(GAV gav) {
        this.gav = gav;
    }

}
