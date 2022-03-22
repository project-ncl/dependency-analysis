package org.jboss.da.reports.model.response.striped;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;
import org.jboss.da.model.rest.GAV;

import java.util.List;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Builder
@Jacksonized
@JsonRootName(value = "report")
@AllArgsConstructor
public class Report {

    @Getter
    @NonNull
    private final String groupId;

    @Getter
    @NonNull
    private final String artifactId;

    @Getter
    @NonNull
    private final String version;

    @Getter
    @NonNull
    private final List<String> availableVersions;

    @Getter
    private final String bestMatchVersion;

    @Getter
    private final boolean dependencyVersionsSatisfied;

    @Getter
    @NonNull
    private final List<Report> dependencies;

    @Getter
    private final boolean blacklisted;

    @Getter
    private final int notBuiltDependencies;

    public Report(
            GAV gav,
            List<String> availableVersions,
            String bestMatchVersion,
            boolean dependencyVersionsSatisfied,
            List<Report> dependencies,
            boolean blacklisted,
            int notBuiltDependencies) {
        this(
                gav.getGroupId(),
                gav.getArtifactId(),
                gav.getVersion(),
                availableVersions,
                bestMatchVersion,
                dependencyVersionsSatisfied,
                dependencies,
                blacklisted,
                notBuiltDependencies);
    }

}
