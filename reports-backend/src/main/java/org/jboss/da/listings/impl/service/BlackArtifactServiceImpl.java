package org.jboss.da.listings.impl.service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.faces.bean.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.da.listings.api.dao.ArtifactDAO;
import org.jboss.da.listings.api.dao.BlackArtifactDAO;
import org.jboss.da.listings.api.dao.GADAO;
import org.jboss.da.listings.api.dao.WhiteArtifactDAO;
import org.jboss.da.listings.api.model.BlackArtifact;
import org.jboss.da.listings.api.model.GA;
import org.jboss.da.listings.api.model.WhiteArtifact;
import org.jboss.da.listings.api.service.BlackArtifactService;
import org.jboss.da.model.rest.GAV;

/**
 * 
 * @author Jozef Mrazek <jmrazek@redhat.com>
 *
 */
@ApplicationScoped
public class BlackArtifactServiceImpl extends ArtifactServiceImpl<BlackArtifact> implements
        BlackArtifactService {

    @Inject
    private BlackArtifactDAO blackArtifactDAO;

    @Inject
    private WhiteArtifactDAO whiteArtifactDAO;

    @Inject
    private WhiteArtifactServiceImpl whiteService;

    @Inject
    private GADAO gaDAO;

    @Override
    protected ArtifactDAO<BlackArtifact> getDAO() {
        return blackArtifactDAO;
    }

    @Override
    public org.jboss.da.listings.api.service.ArtifactService.ArtifactStatus addArtifact(
            String groupId, String artifactId, String version) {

        String nonrhVersion = versionParser.removeRedhatSuffix(version);

        GA ga = gaDAO.findOrCreate(groupId, artifactId);

        BlackArtifact artifact = new BlackArtifact(ga, version);

        if (blackArtifactDAO.findArtifact(groupId, artifactId, version).isPresent()) {
            return ArtifactStatus.NOT_MODIFIED;
        }

        Set<WhiteArtifact> whites = new HashSet<>();
        Optional<WhiteArtifact> rhA = whiteArtifactDAO.findArtifact(groupId, artifactId,
                nonrhVersion);
        rhA.ifPresent(x -> whites.add(rhA.get()));
        Optional<WhiteArtifact> a = whiteArtifactDAO.findArtifact(groupId, artifactId, version);
        a.ifPresent(x -> whites.add(a.get()));

        ArtifactStatus status = ArtifactStatus.ADDED;
        for (WhiteArtifact wa : whites) {
            whiteService.removeArtifact(wa.getGa().getGroupId(), wa.getGa().getArtifactId(),
                    wa.getVersion());
            status = ArtifactStatus.WAS_WHITELISTED;
        }
        blackArtifactDAO.create(artifact);
        return status;
    }

    @Override
    public Optional<BlackArtifact> getArtifact(String groupId, String artifactId, String version) {
        String osgiVersion = versionParser.getNonRedhatOSGiVersion(version);

        return blackArtifactDAO.findArtifact(groupId, artifactId, osgiVersion);
    }

    @Override
    public Optional<BlackArtifact> getArtifact(GAV gav) {
        return getArtifact(gav.getGroupId(), gav.getArtifactId(), gav.getVersion());
    }

    @Override
    public boolean isArtifactPresent(GAV gav) {
        return isArtifactPresent(gav.getGroupId(), gav.getArtifactId(), gav.getVersion());
    }

    @Override
    public boolean isArtifactPresent(String groupId, String artifactId, String version) {
        return getArtifact(groupId, artifactId, version).isPresent();
    }

    @Override
    public boolean removeArtifact(String groupId, String artifactId, String version) {
        Optional<BlackArtifact> artifact = blackArtifactDAO.findArtifact(groupId, artifactId,
                version);
        if (artifact.isPresent()) {
            blackArtifactDAO.delete(artifact.get());
            return true;
        }
        return false;
    }
}
