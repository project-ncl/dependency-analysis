package org.jboss.da.reports.api;

import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Entity whith all builtVersions and bestMatchVersion
 *
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
@AllArgsConstructor
public class VersionLookupResult {

    @Getter
    private final Optional<String> bestMatchVersion;

    @Getter
    private final List<String> availableVersions;
}
