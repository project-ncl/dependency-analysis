package org.jboss.da.reports.model.response.striped;

import lombok.*;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@NoArgsConstructor
@RequiredArgsConstructor
public class VersionWithDifference {

    @Getter
    @Setter
    @NonNull
    private String version;

    @Getter
    @Setter
    @NonNull
    private String differenceType;
}
