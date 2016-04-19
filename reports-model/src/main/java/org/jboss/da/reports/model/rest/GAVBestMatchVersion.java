package org.jboss.da.reports.model.rest;

import org.jboss.da.model.rest.GAV;

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
public class GAVBestMatchVersion {

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
    protected String bestMatchVersion;

    public GAVBestMatchVersion(GAV gav, String bestMatchVersion) {
        this(gav.getGroupId(), gav.getArtifactId(), gav.getVersion(), bestMatchVersion);
    }

}
