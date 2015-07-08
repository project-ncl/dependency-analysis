package org.jboss.da.listings.rest.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.jboss.da.listings.api.service.BlackArtifactService;
import org.jboss.da.listings.api.service.WhiteArtifactService;
import org.jboss.da.listings.rest.RestConvert;
import org.jboss.da.listings.rest.api.Artifacts;
import org.jboss.da.listings.rest.api.model.ContainsResponse;
import org.jboss.da.listings.rest.api.model.RestArtifact;
import org.jboss.da.listings.rest.api.model.SuccessResponse;

/**
 * 
 * @author Jozef Mrazek <jmrazek@redhat.com>
 *
 */
public class ArtifactsImpl implements Artifacts {

    @Inject
    private RestConvert convert;

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

}
