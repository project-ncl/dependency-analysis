package org.jboss.da.reports.model.rest;

import java.util.Set;

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
    private Set<RestGAV2VersionProducts> gavProducts;

}
