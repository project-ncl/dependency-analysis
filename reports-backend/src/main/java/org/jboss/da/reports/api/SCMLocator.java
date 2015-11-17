package org.jboss.da.reports.api;

import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Honza Brázdil <janinko.g@gmail.com>
 */
@RequiredArgsConstructor
@AllArgsConstructor
@NoArgsConstructor
public class SCMLocator {

    @Getter
    @NonNull
    private String scmUrl;

    @Getter
    @NonNull
    private String revision;

    @Getter
    @NonNull
    private String pomPath;

    @Getter
    @NonNull
    private List<String> repositories = Collections.emptyList();
}
