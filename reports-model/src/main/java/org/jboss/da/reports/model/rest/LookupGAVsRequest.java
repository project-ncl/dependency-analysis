package org.jboss.da.reports.model.rest;

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
public class LookupGAVsRequest {

    @Getter
    @NonNull
    private Set<String> productNames = new HashSet<>();

    @Getter
    @NonNull
    private Set<Long> productVersionIds = new HashSet<>();

    @Getter
    @NonNull
    private List<GAV> gavs;

}
