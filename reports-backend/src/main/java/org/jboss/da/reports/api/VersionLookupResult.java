package org.jboss.da.reports.api;

import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity whith all builtVersions and bestMatchVersion
 *
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
@AllArgsConstructor
public class VersionLookupResult {

    @Getter
    private Optional<String> bestMatchVersion;

    @Getter
    private List<String> availableVersions;
}
