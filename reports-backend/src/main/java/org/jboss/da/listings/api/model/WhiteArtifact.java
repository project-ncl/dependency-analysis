package org.jboss.da.listings.api.model;

import javax.persistence.Entity;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Jozef Mrazek &lt;jmrazek@redhat.com&gt;
 *
 */
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Entity
public class WhiteArtifact extends Artifact {

    @Setter
    @Getter
    private boolean is3rdParty;

    @Setter
    @Getter
    @NonNull
    private String osgiVersion;

    public WhiteArtifact(GA ga, String version, User insertedBy, String osgiVersion, boolean is3rdParty) {
        super(ga, version, insertedBy);
        this.is3rdParty = is3rdParty;
        this.osgiVersion = osgiVersion;
    }

}
