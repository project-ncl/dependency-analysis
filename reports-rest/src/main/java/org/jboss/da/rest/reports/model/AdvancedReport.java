package org.jboss.da.rest.reports.model;

import org.jboss.da.communication.model.GAV;
import java.util.Set;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class AdvancedReport {

    @Getter
    @NonNull
    private Report report;

    @Getter
    @NonNull
    private Set<GAV> blacklistArtifacts;

    @Getter
    @NonNull
    private Set<GAV> whitelistArtifacts;

    @Getter
    @NonNull
    private Set<GAV> communityGavsWithBestMatchVersions;

    @Getter
    @NonNull
    private Set<GAV> communityGavsWithBuiltVersions;

    @Getter
    @NonNull
    private Set<GAV> communityGavs;
}
