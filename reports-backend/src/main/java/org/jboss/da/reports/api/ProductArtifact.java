package org.jboss.da.reports.api;

import org.jboss.da.communication.model.GAV;
import org.jboss.da.listings.api.service.ArtifactService.SupportStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Class reprpresenting artifact and product where this artifact is whitelisted.
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
@RequiredArgsConstructor
@NoArgsConstructor
public class ProductArtifact {

    @Getter
    @Setter
    @NonNull
    private String productName;

    @Getter
    @Setter
    @NonNull
    private String productVersion;

    @Getter
    @Setter
    @NonNull
    private SupportStatus supportStatus;

    @Getter
    @Setter
    @NonNull
    private GAV artifact;

}
