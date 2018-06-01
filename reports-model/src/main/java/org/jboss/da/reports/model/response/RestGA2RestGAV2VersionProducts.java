package org.jboss.da.reports.model.response;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@NoArgsConstructor
@RequiredArgsConstructor
public class RestGA2RestGAV2VersionProducts {

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
    private List<RestGAV2VersionProducts> gavProducts;
}
