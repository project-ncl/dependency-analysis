package org.jboss.da.listings.impl.service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.faces.bean.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.da.common.version.VersionParser;

import org.jboss.da.listings.api.dao.ArtifactDAO;
import org.jboss.da.listings.api.dao.BlackArtifactDAO;
import org.jboss.da.listings.api.dao.GADAO;
import org.jboss.da.listings.api.dao.WhiteArtifactDAO;
import org.jboss.da.listings.api.model.BlackArtifact;
import org.jboss.da.listings.api.model.GA;
import org.jboss.da.listings.api.model.WhiteArtifact;
import org.jboss.da.listings.api.service.BlackArtifactService;
import org.jboss.da.model.rest.GAV;
import org.jboss.da.model.rest.VersionComparator;

import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * 
 * @author Jozef Mrazek &lt;jmrazek@redhat.com&gt;
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
    private GADAO gaDAO;

    @Override
    protected ArtifactDAO<BlackArtifact> getDAO() {
        return blackArtifactDAO;
    }

    @Override
    public org.jboss.da.listings.api.service.ArtifactService.ArtifactStatus addArtifact(
            String groupId, String artifactId, String version) {

        String osgiVersion = versionParser.getOSGiVersion(version);

        GA ga = gaDAO.findOrCreate(groupId, artifactId);

        BlackArtifact artifact = new BlackArtifact(ga, osgiVersion, currentUser());

        if (blackArtifactDAO.findArtifact(groupId, artifactId, osgiVersion).isPresent()) {
            return ArtifactStatus.NOT_MODIFIED;
        }

        Set<WhiteArtifact> whites = new HashSet<>();
        Optional<WhiteArtifact> rhA = whiteArtifactDAO.findArtifact(groupId, artifactId,
                osgiVersion);
        rhA.ifPresent(x -> whites.add(rhA.get()));
        Optional<WhiteArtifact> a = whiteArtifactDAO.findArtifact(groupId, artifactId, version);
        a.ifPresent(x -> whites.add(a.get()));

        ArtifactStatus status = ArtifactStatus.ADDED;
        
        if(!whites.isEmpty()){
            status = ArtifactStatus.WAS_WHITELISTED;
        }
        
        blackArtifactDAO.create(artifact);
        return status;
    }

    @Override
    public Optional<BlackArtifact> getArtifact(String groupId, String artifactId, String version) {
        String osgiVersion = versionParser.getNonRedhatOSGiVersion(version);
        Optional<BlackArtifact> maybeArtifact = blackArtifactDAO.findArtifact(groupId, artifactId,
                osgiVersion);
        if (VersionParser.isRedhatVersion(version) && !maybeArtifact.isPresent()) {
            maybeArtifact = blackArtifactDAO.findArtifact(groupId, artifactId, version);
        }
        return maybeArtifact;
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

    @Override
    public Set<BlackArtifact> getArtifacts(String groupId, String artifactId) {
        List<BlackArtifact> artifacts = blackArtifactDAO.findArtifacts(groupId, artifactId);
        Set<GA> communityGAs = artifacts.stream()
                .filter(a -> !VersionParser.isRedhatVersion(a.getVersion()))
                .map(BlackArtifact::getGa)
                .collect(Collectors.toSet());

        Comparator<BlackArtifact> baComparator = (a, b) -> VersionComparator.compareVersions(a.getVersion(), b.getVersion());
        return artifacts.stream()
                .filter(a -> !(communityGAs.contains(a.getGa()) && VersionParser.isRedhatVersion(a.getVersion())))
                .collect(Collectors.toCollection(() -> new TreeSet<>(baComparator)));
    }
}
