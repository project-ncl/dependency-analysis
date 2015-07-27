package org.jboss.da.reports.api;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Honza Brázdil <janinko.g@gmail.com>
 */
@RequiredArgsConstructor
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
    private String version;
}
