package org.jboss.da.reports.model.rest;

import org.jboss.da.model.rest.GAV;

import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
@NoArgsConstructor
public class LookupGAVsRequest {

    @Getter
    @NonNull
    private Set<String> productNames;

    @Getter
    @NonNull
    private Set<Long> productVersionIds;

    @Getter
    @NonNull
    private List<GAV> gavs;

}
