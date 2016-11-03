package org.jboss.da.reports.model.rest;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@NoArgsConstructor
public class AlignReport {

    @Getter
    @NonNull
    private final List<RestGA2RestGAV2VersionProducts> internallyBuilt = new ArrayList<>();

    @Getter
    @NonNull
    private final List<RestGA2RestGAV2VersionProductsWithDiff> builtInDifferentVersion = new ArrayList<>();

    @Getter
    @NonNull
    private final List<RestGA2GAVs> notBuilt = new ArrayList<>();

    @Getter
    @NonNull
    private final List<RestGA2GAVs> blacklisted = new ArrayList<>();

}
