package org.jboss.da.rest.listings;

import org.jboss.da.listings.api.model.Artifact;
import org.jboss.da.rest.listings.model.RestArtifact;

import javax.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 
 * @author Jozef Mrazek <jmrazek@redhat.com>
 *
 */
@ApplicationScoped
public class RestConvert {

    public <T extends Artifact> List<RestArtifact> toRestArtifacts(List<T> artifacts) {
        return artifacts.stream().map(RestConvert::toRestArtifact).collect(Collectors.toList());
    }

    public static RestArtifact toRestArtifact(Artifact a) {
        RestArtifact ra = new RestArtifact();
        ra.setArtifactId(a.getArtifactId());
        ra.setGroupId(a.getGroupId());
        ra.setVersion(a.getVersion());
        return ra;
    }

}
