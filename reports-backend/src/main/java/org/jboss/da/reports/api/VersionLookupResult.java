package org.jboss.da.reports.api;

import java.util.List;

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
    private String bestMatchVersion;

    @Getter
    @Setter
    private List<String> availableVersions;
}
