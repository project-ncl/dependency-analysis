package org.jboss.da.rest.listings;

import org.jboss.da.listings.api.model.BlackArtifact;
import org.jboss.da.listings.api.service.ArtifactService;
import org.jboss.da.listings.api.service.BlackArtifactService;
import org.jboss.da.listings.model.rest.ContainsResponse;
import org.jboss.da.listings.model.rest.RestArtifact;
import org.jboss.da.listings.model.rest.SuccessResponse;
import org.jboss.da.model.rest.ErrorMessage;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.jboss.da.rest.spi.BlackList;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public class BlackListImpl implements BlackList {

    @Inject
    private RestConvert convert;

    @Inject
    private BlackArtifactService blackService;

    @Override
    public Collection<RestArtifact> getAllBlackArtifacts() {
        List<RestArtifact> artifacts = new ArrayList<>();
        artifacts.addAll(convert.toRestArtifacts(blackService.getAll()));
        return artifacts;
    }

    @Override
    public Response isBlackArtifactPresent(String groupId, String artifactId, String version) {
        if (groupId == null || artifactId == null || version == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage(ErrorMessage.ErrorType.PARAMS_REQUIRED,
                            "All parameters are required",
                            "Parameters groupID, artifactID and version must be specified."))
                    .build();
        }
        ContainsResponse response = new ContainsResponse();

        Optional<BlackArtifact> artifact = blackService.getArtifact(groupId, artifactId, version);
        List<BlackArtifact> artifacts = artifact
                .map(Collections::singletonList)
                .orElse(Collections.emptyList());

        response.setContains(artifact.isPresent());
        response.setFound(convert.toRestArtifacts(artifacts));

        if (artifact.isPresent()) {
            return Response.ok(response).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(response).build();
        }
    }

    @Override
    public Response addBlackArtifact(RestArtifact artifact) {
        SuccessResponse response = new SuccessResponse();
        ArtifactService.ArtifactStatus result = blackService.addArtifact(artifact.getGroupId(),
                artifact.getArtifactId(), artifact.getVersion());
        switch (result) {
            case ADDED:
                response.setSuccess(true);
                return Response.ok(response).build();
            case WAS_WHITELISTED:
                response.setSuccess(true);
                response.setMessage("Artifact was moved from whitelist into blacklist");
                return Response.ok(response).build();
            case NOT_MODIFIED:
                response.setSuccess(false);
                return Response.ok(response).build();
            default:
                return Response
                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(new ErrorMessage(ErrorMessage.ErrorType.UNEXPECTED_SERVER_ERR,
                                "Unexpected server error occurred.", "Result was " + result))
                        .build();
        }
    }

    @Override
    public SuccessResponse removeBlackArtifact(RestArtifact artifact) {
        SuccessResponse response = new SuccessResponse();
        response.setSuccess(blackService.removeArtifact(artifact.getGroupId(),
                artifact.getArtifactId(), artifact.getVersion()));
        return response;
    }

    @Override
    public Collection<RestArtifact> getBlackArtifacts(String groupId, String artifactId) {
        return convert.toRestArtifacts(blackService.getArtifacts(groupId, artifactId));
    }
}
