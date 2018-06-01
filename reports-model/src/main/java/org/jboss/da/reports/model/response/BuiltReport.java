package org.jboss.da.reports.model.response;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@NoArgsConstructor
public class BuiltReport {

    @Getter
    @Setter
    @NonNull
    private String groupId;

    @Getter
    @Setter
    @NonNull
    private String artifactId;

    @Getter
    @Setter
    @NonNull
    private String version;

    @Getter
    @Setter
    private String builtVersion;

    @Getter
    @Setter
    @NonNull
    private List<String> availableVersions = new ArrayList<>();

}
