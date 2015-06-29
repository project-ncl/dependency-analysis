package org.jboss.da.reports.api;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Honza Brázdil <janinko.g@gmail.com>
 */
@RequiredArgsConstructor
public class Product {
    @Getter
    @NonNull
    private String name;
    
    @Getter
    @NonNull
    private String version;
    
}
