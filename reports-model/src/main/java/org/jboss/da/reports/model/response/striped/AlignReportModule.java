package org.jboss.da.reports.model.response.striped;

import lombok.*;
import org.jboss.da.reports.model.response.GAVAvailableVersions;

import java.util.List;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@NoArgsConstructor
@RequiredArgsConstructor
public class AlignReportModule {

    @Getter
    @Setter
    @NonNull
    private String groupId; // module G

    @Getter
    @Setter
    @NonNull
    private String artifactId;// module A

    @Getter
    @Setter
    @NonNull
    private List<GAVAvailableVersions> dependencies;
}
