package org.jboss.da.rest.reports.model;

import org.codehaus.jackson.annotate.JsonUnwrapped;
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
    @JsonUnwrapped
    private GAV gav;

    @Getter
    @Setter
    private String bestMatchVersion;

}
