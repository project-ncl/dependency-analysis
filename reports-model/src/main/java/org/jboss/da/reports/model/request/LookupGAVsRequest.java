package org.jboss.da.reports.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.ToString;
import org.jboss.da.model.rest.GAV;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class LookupGAVsRequest {

    @Getter
    @NonNull
    private Set<String> productNames = new HashSet<>();

    @Getter
    @NonNull
    private Set<Long> productVersionIds = new HashSet<>();

    @Getter
    @Deprecated
    private String repositoryGroup;

    @Getter
    private Boolean brewPullActive;

    @Getter
    private Boolean temporaryBuild;

    @Getter
    private String versionSuffix;

    @Getter
    @NonNull
    private List<GAV> gavs;

}
