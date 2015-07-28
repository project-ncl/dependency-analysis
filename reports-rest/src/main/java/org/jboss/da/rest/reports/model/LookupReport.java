package org.jboss.da.rest.reports.model;

import org.jboss.da.communication.aprox.model.GAV;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class LookupReport {

    @Getter
    @Setter
    @NonNull
    private GAV gav;

    @Getter
    @Setter
    @NonNull
    private String bestMatchVersion;

}
