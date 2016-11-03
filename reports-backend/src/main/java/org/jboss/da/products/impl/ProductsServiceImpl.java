package org.jboss.da.products.impl;

import org.jboss.da.listings.api.dao.ProductVersionDAO;
import org.jboss.da.listings.api.model.GA;
import org.jboss.da.listings.api.model.ProductVersion;
import org.jboss.da.listings.api.model.WhiteArtifact;
import org.jboss.da.products.api.ArtifactDiff;
import org.jboss.da.products.api.ProductsService;

import javax.inject.Inject;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public class ProductsServiceImpl implements ProductsService {

    @Inject
    private ProductVersionDAO productVersionDAO;

    @Override
    public Set<ArtifactDiff> difference(long leftProduct, long rightProduct) {
        ProductVersion left = productVersionDAO.read(leftProduct);
        ProductVersion right = productVersionDAO.read(rightProduct);
        if(left == null || right == null){
            throw new IllegalArgumentException("One or both of the products (" + leftProduct + ", "
                    + rightProduct + ") doesn't exists.");
        }
        Set<WhiteArtifact> leftArtifacts = left.getWhiteArtifacts();
        Set<WhiteArtifact> rightArtifacts = right.getWhiteArtifacts();
        Set<GA> allGAs = new HashSet<>();
        Map<GA, String> leftGAs = leftArtifacts.stream()
                .peek(o -> allGAs.add(o.getGa()))
                .collect(Collectors.toMap(WhiteArtifact::getGa, WhiteArtifact::getOsgiVersion));
        Map<GA, String> rightGAs = rightArtifacts.stream()
                .peek(o -> allGAs.add(o.getGa()))
                .collect(Collectors.toMap(WhiteArtifact::getGa, WhiteArtifact::getOsgiVersion));
        Set<ArtifactDiff> ret = new HashSet<>();
        for(GA ga : allGAs){
            String leftVersion = leftGAs.get(ga);
            String rightVersion = rightGAs.get(ga);
            ArtifactDiff ad = new ArtifactDiff(leftVersion,
                    new org.jboss.da.model.rest.GA(ga.getGroupId(), ga.getArtifactId()),
                    rightVersion);
            ret.add(ad);
        }
        return ret;
    }
}
