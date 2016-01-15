package org.jboss.da.rest.reports.model;

import org.jboss.da.communication.model.GAV;
import org.jboss.da.rest.listings.model.RestGavProducts;
import org.jboss.da.rest.listings.model.RestProductInput;

import java.util.Map;
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
