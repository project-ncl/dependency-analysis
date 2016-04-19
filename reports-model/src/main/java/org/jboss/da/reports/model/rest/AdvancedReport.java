package org.jboss.da.reports.model.rest;

import org.jboss.da.listings.model.rest.RestGavProducts;
import org.jboss.da.model.rest.GAV;

import java.util.Set;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

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
    private Set<RestGavProducts> whitelistedArtifacts;

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
