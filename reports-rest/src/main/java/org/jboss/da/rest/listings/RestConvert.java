package org.jboss.da.rest.listings;

import org.jboss.da.listings.api.model.Artifact;
import org.jboss.da.listings.api.model.ProductVersion;
import org.jboss.da.listings.api.model.ProductVersionArtifactRelationship;
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

    public List<RestProductGAV> toRestProductGAV(ProductVersion pv) {
        List<RestProductGAV> rpgList = new ArrayList<>();
        for (Artifact a : pv.getWhiteArtifacts()) {
            RestProductGAV rpg = new RestProductGAV();
            rpg.setName(pv.getProduct().getName());
            rpg.setVersion(pv.getProductVersion());
            rpg.setSupportStatus(pv.getSupport());
            RestArtifact ra = new RestArtifact();
            ra.setArtifactId(a.getGa().getArtifactId());
            ra.setGroupId(a.getGa().getGroupId());
            ra.setVersion(a.getVersion());
            rpg.setGav(ra);
            rpgList.add(rpg);
        }
        return rpgList;
    }

    public List<RestProductGAV> toRestProductGAVList(List<ProductVersion> productVersions) {
        List<RestProductGAV> rpgl = new ArrayList<>();
        for (ProductVersion p : productVersions) {
            rpgl.addAll(toRestProductGAV(p));
        }
        return rpgl;
    }

    public List<RestProduct> toRestProductList(List<ProductVersion> productVersions) {
        List<RestProduct> lrp = new ArrayList<>();
        for (ProductVersion p : productVersions) {
            lrp.add(toRestProduct(p));
        }
        return lrp;
    }

    public RestProduct toRestProduct(ProductVersion productVersion) {
        RestProduct rp = new RestProduct();
        rp.setId(productVersion.getId());
        rp.setName(productVersion.getProduct().getName());
        rp.setVersion(productVersion.getProductVersion());
        rp.setSupportStatus(productVersion.getSupport());
        return rp;
    }

    public List<RestProductGAV> fromRelationshipToRestProductGAVList(
            List<ProductVersionArtifactRelationship> rl) {
        List<RestProductGAV> rpgList = new ArrayList<>();
        for (ProductVersionArtifactRelationship r : rl) {
            RestProductGAV rpg = new RestProductGAV();
            rpg.setName(r.getProductVersion().getProduct().getName());
            rpg.setVersion(r.getProductVersion().getProductVersion());
            rpg.setSupportStatus(r.getProductVersion().getSupport());
            RestArtifact ra = new RestArtifact();
            ra.setArtifactId(r.getArtifact().getGa().getArtifactId());
            ra.setGroupId(r.getArtifact().getGa().getGroupId());
            ra.setVersion(r.getArtifact().getVersion());
            rpg.setGav(ra);
            rpgList.add(rpg);
        }
        return rpgList;
    }

    private static RestArtifact toRestArtifact(Artifact a) {
        RestArtifact ra = new RestArtifact();
        ra.setArtifactId(a.getGa().getArtifactId());
        ra.setGroupId(a.getGa().getGroupId());
        ra.setVersion(a.getVersion());
        return ra;
    }

}
