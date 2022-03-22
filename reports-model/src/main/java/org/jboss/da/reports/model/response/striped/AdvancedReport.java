package org.jboss.da.reports.model.response.striped;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import org.jboss.da.model.rest.GAV;
import org.jboss.da.reports.model.response.GAVAvailableVersions;
import org.jboss.da.reports.model.response.GAVBestMatchVersion;

import java.util.Set;

@Jacksonized
@Builder
@RequiredArgsConstructor
public class AdvancedReport {

    @Getter
    @NonNull
    private Report report;

    @Getter
    @NonNull
    private Set<GAV> blacklistedArtifacts;

    @Getter
    @NonNull
    private Set<GAVBestMatchVersion> communityGavsWithBestMatchVersions;

    @Getter
    @NonNull
    private Set<GAVAvailableVersions> communityGavsWithBuiltVersions;

    @Getter
    @NonNull
    private Set<GAV> communityGavs;
}
