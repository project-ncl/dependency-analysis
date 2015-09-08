package org.jboss.da.reports.api;

import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity whith all builtVersions and bestMatchVersion
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
@AllArgsConstructor
public class VersionLookupResult {

    @Getter
    @Setter
    private Optional<String> bestMatchVersion;

    @Getter
    @Setter
    private List<String> availableVersions;
}
