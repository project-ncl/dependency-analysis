package org.jboss.da.rest.listings;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;

import org.jboss.da.listings.api.model.Artifact;
import org.jboss.da.rest.listings.api.model.RestArtifact;

/**
 * 
 * @author Jozef Mrazek <jmrazek@redhat.com>
 *
 */
@ApplicationScoped
public class RestListingsConvert {

    public <T extends Artifact> List<RestArtifact> toRestArtifacts(List<T> artifacts) {
        List<RestArtifact> restArtifacts = new ArrayList<RestArtifact>();
        for (Artifact a : artifacts) {
            RestArtifact ra = new RestArtifact();
            ra.setArtifactId(a.getArtifactId());
            ra.setGroupId(a.getGroupId());
            ra.setVersion(a.getVersion());
            restArtifacts.add(ra);
        }
        return restArtifacts;
    }

}
