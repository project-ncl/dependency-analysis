package org.jboss.da.listings.impl.dao;

import javax.ejb.Stateless;
import org.jboss.da.listings.api.dao.WhiteArtifactDAO;
import org.jboss.da.listings.api.model.WhiteArtifact;

/**
 * 
 * @author Jozef Mrazek &lt;jmrazek@redhat.com&gt;
 *
 */
@Stateless
public class WhiteArtifactDAOImpl extends ArtifactDAOImpl<WhiteArtifact> implements WhiteArtifactDAO {

    public WhiteArtifactDAOImpl() {
        super(WhiteArtifact.class);
    }
}
