package org.jboss.da.reports.model.response;

import org.jboss.da.listings.model.rest.RestProductInput;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
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
