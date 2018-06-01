package org.jboss.da.reports.model.request;

import org.jboss.da.reports.model.api.SCMLocator;

import java.util.HashSet;
import java.util.Set;
import javax.validation.Valid;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
public class SCMReportRequest {

    @Getter
    @NonNull
    private Set<String> productNames = new HashSet<>();

    @Getter
    @NonNull
    private Set<Long> productVersionIds = new HashSet<>();

    @Getter
    @Setter
    @NonNull
    @Valid
    private SCMLocator scml;

}
