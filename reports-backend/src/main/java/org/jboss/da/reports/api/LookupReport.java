package org.jboss.da.reports.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@RequiredArgsConstructor
public class LookupReport {
    @Getter @Setter
    @NotNull
    private GAV gav;

    @Getter @Setter
    @NotNull
    private String bestMatchVersion;
}
