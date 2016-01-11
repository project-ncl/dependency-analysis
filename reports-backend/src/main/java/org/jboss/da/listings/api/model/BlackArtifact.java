package org.jboss.da.listings.api.model;

import javax.persistence.Entity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 
 * @author Jozef Mrazek <jmrazek@redhat.com>
 *
 */
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Entity
public class BlackArtifact extends Artifact {

    public BlackArtifact(GA ga, String version) {
        super(ga, version);
    }

}
