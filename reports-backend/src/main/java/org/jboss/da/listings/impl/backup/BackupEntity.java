package org.jboss.da.listings.impl.backup;

import org.jboss.da.listings.api.model.BlackArtifact;
import org.jboss.da.listings.api.model.WhiteArtifact;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity, which represents whole whitelist and blacklist backup structure
 * 
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
@AllArgsConstructor
@NoArgsConstructor
public class BackupEntity {

    @Getter
    @Setter
    private List<WhiteArtifact> whiteArtifacts;

    @Getter
    @Setter
    private List<BlackArtifact> blackArtifacts;

}
