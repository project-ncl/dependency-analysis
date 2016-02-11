package org.jboss.da.rest.reports.model;

import org.jboss.da.rest.listings.model.RestGavProducts;

import java.util.Set;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
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
