package org.jboss.da.rest.reports.model;

import org.jboss.da.communication.model.GAV;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@AllArgsConstructor
public class LookupReport {

    @Getter
    @Setter
    @NonNull
    private GAV gav;

    @Getter
    @Setter
    private String bestMatchVersion;

}
