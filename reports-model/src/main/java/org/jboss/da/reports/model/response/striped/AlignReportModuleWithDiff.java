package org.jboss.da.reports.model.response.striped;

import java.util.List;

import lombok.*;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@NoArgsConstructor
@RequiredArgsConstructor
public class AlignReportModuleWithDiff {

    @Getter
    @Setter
    @NonNull
    private String groupId; // module G

    @Getter
    @Setter
    @NonNull
    private String artifactId; // module A

    @Getter
    @Setter
    @NonNull
    private List<GAVAvailableVersionsWithDiff> dependencies;

}
