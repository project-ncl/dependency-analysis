package org.jboss.da.reports.model.response;

import java.util.List;

import org.jboss.da.listings.model.rest.RestProductInput;
import org.jboss.da.model.rest.GAV;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LookupReport {

    @JsonUnwrapped
    @NonNull
    private GAV gav;

    private String bestMatchVersion;
    private List<String> availableVersions;
    private boolean blacklisted;
    private List<RestProductInput> whitelisted;

    public LookupReport(GAV gav) {
        this.gav = gav;
    }
}
