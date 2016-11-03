package org.jboss.da.bc.model.backend;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.jboss.da.bc.model.BcError;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import org.jboss.da.bc.model.BcError;

import org.jboss.da.model.rest.GAV;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
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
    private List<Integer> existingBCs;

    @Getter
    @Setter
    private Optional<String> internallyBuilt = Optional.empty(); // generated, unmodifiable

    @Getter
    @Setter
    private List<String> availableVersions;

    @Getter
    @Setter
    private boolean cloneRepo = true; // required, default true

    @Getter
    @Setter
    @NonNull
    private EnumSet<BcError> errors = EnumSet.noneOf(BcError.class);

    public void addError(BcError e) {
        errors.add(e);
    }

    public ProjectDetail(GAV gav) {
        this.gav = gav;
    }

    public boolean isUseExistingBc() {
        return bcId != null;
    }

    public boolean isBcExists() {
        return existingBCs == null ? false : !existingBCs.isEmpty();
    }

}
