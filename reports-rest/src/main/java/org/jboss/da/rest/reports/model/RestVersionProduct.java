package org.jboss.da.rest.reports.model;

import org.jboss.da.rest.listings.model.RestProductInput;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
@NoArgsConstructor
@RequiredArgsConstructor
public class RestVersionProduct {

    @Getter
    @Setter
    @NonNull
    private String version;

    @Getter
    @Setter
    @NonNull
    private RestProductInput product;
}
