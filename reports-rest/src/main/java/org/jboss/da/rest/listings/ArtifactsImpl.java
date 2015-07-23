package org.jboss.da.rest.listings;

import org.jboss.da.listings.api.service.BlackArtifactService;
import org.jboss.da.listings.api.service.WhiteArtifactService;
import org.jboss.da.rest.listings.api.Artifacts;
import org.jboss.da.rest.listings.api.model.ContainsResponse;
import org.jboss.da.rest.listings.api.model.RestArtifact;
import org.jboss.da.rest.listings.api.model.SuccessResponse;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 
 * @author Jozef Mrazek <jmrazek@redhat.com>
 *
 */
public class ArtifactsImpl implements Artifacts {

    @Inject
    private RestListingsConvert convert;

    @Inject
    private WhiteArtifactService whiteService;

    @Inject
    private BlackArtifactService blackService;

    @Override
    public SuccessResponse addWhiteArtifact(RestArtifact artifact) {
        SuccessResponse response = new SuccessResponse();
        response.setSuccess(whiteService.addArtifact(artifact.getGroupId(),
                artifact.getArtifactId(),
                artifact.getVersion()));
        return response;
    }

    @Override
    public SuccessResponse addBlackArtifact(RestArtifact artifact) {
        SuccessResponse response = new SuccessResponse();
        response.setSuccess(blackService.addArtifact(artifact.getGroupId(),
                artifact.getArtifactId(),
                artifact.getVersion()));
        return response;
    }

    @Override
    public ContainsResponse isWhiteArtifactPresent(String groupId, String artifactId, String version) {
        ContainsResponse response = new ContainsResponse();
        response.setContains(whiteService.isArtifactPresent(groupId, artifactId, version));
        return response;
    }

    public ContainsResponse isBlackArtifactPresent(String groupId, String artifactId, String version) {
        ContainsResponse response = new ContainsResponse();
        response.setContains(blackService.isArtifactPresent(groupId, artifactId, version));
        return response;
    }

    @Override
    public Collection<RestArtifact> getAllWhiteArtifacts() {
        List<RestArtifact> artifacts = new ArrayList<RestArtifact>();
        artifacts.addAll(convert.toRestArtifacts(whiteService.getAll()));
        return artifacts;
    }

    @Override
    public Collection<RestArtifact> getAllBlackArtifacts() {
        List<RestArtifact> artifacts = new ArrayList<RestArtifact>();
        artifacts.addAll(convert.toRestArtifacts(blackService.getAll()));
        return artifacts;
    }

    @Override
    public SuccessResponse removeWhiteArtifact(RestArtifact artifact) {
        SuccessResponse response = new SuccessResponse();
        response.setSuccess(whiteService.removeArtifact(artifact.getGroupId(),
                artifact.getArtifactId(),
                artifact.getVersion()));
        return response;
    }

    @Override
    public SuccessResponse removeBlackArtifact(RestArtifact artifact) {
        SuccessResponse response = new SuccessResponse();
        response.setSuccess(blackService.removeArtifact(artifact.getGroupId(),
                artifact.getArtifactId(),
                artifact.getVersion()));
        return response;
    }

}
