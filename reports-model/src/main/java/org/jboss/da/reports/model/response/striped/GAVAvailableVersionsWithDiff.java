package org.jboss.da.reports.model.response.striped;

import lombok.*;

import java.util.List;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@NoArgsConstructor
@RequiredArgsConstructor
public class GAVAvailableVersionsWithDiff {

    @Getter
    @Setter
    @NonNull
    private String groupId;

    @Getter
    @Setter
    @NonNull
    private String artifactId;

    @Getter
    @Setter
    @NonNull
    private String version;

    @Getter
    @Setter
    @NonNull
    private List<VersionWithDifference> availableVersions;
}
