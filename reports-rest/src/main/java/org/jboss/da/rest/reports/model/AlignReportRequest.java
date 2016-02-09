package org.jboss.da.rest.reports.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
public class AlignReportRequest {

    @Getter
    @Setter
    @NonNull
    private Set<Long> products = new HashSet<>();

    @Getter
    @Setter
    private boolean searchUnknownProducts = true;

    @Getter
    @Setter
    @NonNull
    private String scmUrl;

    @Getter
    @Setter
    @NonNull
    private String revision;

    @Getter
    @Setter
    @NonNull
    private List<String> additionalRepos = new ArrayList<>();

    @Getter
    @Setter
    private String pomPath;

}
