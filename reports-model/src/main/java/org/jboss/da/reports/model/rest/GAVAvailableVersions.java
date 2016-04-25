package org.jboss.da.reports.model.rest;

import org.jboss.da.model.rest.GAV;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class GAVAvailableVersions {

    @Getter
    @Setter
    protected String groupId;

    @Getter
    @Setter
    protected String artifactId;

    @Getter
    @Setter
    protected String version;

    @Getter
    @Setter
    protected Set<String> availableVersions;

    public GAVAvailableVersions(GAV gav, Set<String> availableVersions) {
        this(gav.getGroupId(), gav.getArtifactId(), gav.getVersion(), availableVersions);
    }
}
