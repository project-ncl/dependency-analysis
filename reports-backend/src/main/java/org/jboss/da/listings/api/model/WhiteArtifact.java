package org.jboss.da.listings.api.model;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 
 * @author Jozef Mrazek <jmrazek@redhat.com>
 *
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "groupId", "artifactId", "version" }))
public class WhiteArtifact extends Artifact {

    public WhiteArtifact(String groupId, String artifactId, String version) {
        super(groupId, artifactId, version);
    }

}
