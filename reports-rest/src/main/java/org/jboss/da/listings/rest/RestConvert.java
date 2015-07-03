package org.jboss.da.listings.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;

import org.jboss.da.listings.api.model.Artifact;
import org.jboss.da.listings.rest.api.model.RestArtifact;

/**
 * 
 * @author Jozef Mrazek <jmrazek@redhat.com>
 *
 */
@Stateless
public class RestConvert {

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
