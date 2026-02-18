package org.jboss.da.rest.listings;

import org.jboss.da.listings.api.model.Artifact;
import org.jboss.da.listings.model.rest.RestArtifact;
import org.jboss.da.listings.model.rest.RestProduct;
import org.jboss.da.listings.model.rest.RestProductGAV;

import javax.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 
 * @author Jozef Mrazek &lt;jmrazek@redhat.com&gt;
 *
 */
@ApplicationScoped
public class RestConvert {

    public <T extends Artifact> List<RestArtifact> toRestArtifacts(Collection<T> artifacts) {
        return artifacts.stream().map(RestConvert::toRestArtifact).collect(Collectors.toList());
    }

    private static RestArtifact toRestArtifact(Artifact a) {
        RestArtifact ra = new RestArtifact();
        ra.setArtifactId(a.getGa().getArtifactId());
        ra.setGroupId(a.getGa().getGroupId());
        ra.setVersion(a.getVersion());
        return ra;
    }

}
