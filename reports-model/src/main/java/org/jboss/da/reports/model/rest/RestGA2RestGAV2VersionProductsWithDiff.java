package org.jboss.da.reports.model.rest;

import java.util.Comparator;
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
public class RestGA2RestGAV2VersionProductsWithDiff implements
        Comparable<RestGA2RestGAV2VersionProductsWithDiff> {

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
    private Set<RestGAV2VersionProductsWithDiff> gavProducts;

    @Override
    public int compareTo(RestGA2RestGAV2VersionProductsWithDiff o) {
        if (this.groupId.equals(o.groupId))
            return (this.artifactId.compareTo(o.artifactId));
        else {
            return this.groupId.compareTo(o.groupId);
        }
    }
}
