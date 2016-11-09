package org.jboss.da.reports.model.rest;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.jboss.da.model.rest.validators.ScmUrl;

public class BuiltReportRequest {

    @Getter
    @Setter
    @NonNull
    @ScmUrl
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
