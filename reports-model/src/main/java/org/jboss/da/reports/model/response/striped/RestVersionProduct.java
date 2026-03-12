package org.jboss.da.reports.model.response.striped;

import lombok.*;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@NoArgsConstructor
@RequiredArgsConstructor
public class RestVersionProduct {

    @Getter
    @Setter
    @NonNull
    private String version;
}
