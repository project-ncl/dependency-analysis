package org.jboss.da.reports.model.response.striped;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.jboss.da.reports.model.response.RestGA2GAVs;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@AllArgsConstructor
public class AlignReport {

    @Getter
    @NonNull
    private final List<AlignReportModule> internallyBuilt;

    @Getter
    @NonNull
    private final List<AlignReportModuleWithDiff> builtInDifferentVersion;

    @Getter
    @NonNull
    private final List<RestGA2GAVs> notBuilt;

    @Getter
    @NonNull
    private final List<RestGA2GAVs> blacklisted;

}
