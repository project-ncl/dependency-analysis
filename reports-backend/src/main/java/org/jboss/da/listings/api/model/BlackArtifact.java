package org.jboss.da.listings.api.model;

import javax.persistence.Entity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 
 * @author Jozef Mrazek &lt;jmrazek@redhat.com&gt;
 *
 */
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Entity
public class BlackArtifact extends Artifact {

    public BlackArtifact(GA ga, String version, User insertedBy) {
        super(ga, version, insertedBy);
    }

}
