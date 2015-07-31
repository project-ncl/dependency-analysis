package org.jboss.da.rest.reports.model;

import org.jboss.da.communication.aprox.model.GAV;
import lombok.Getter;
import lombok.NonNull;
import lombok.AllArgsConstructor;
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
