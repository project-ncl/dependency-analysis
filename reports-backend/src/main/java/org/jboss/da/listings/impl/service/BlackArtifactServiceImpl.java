package org.jboss.da.listings.impl.service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.da.listings.api.dao.ArtifactDAO;
import org.jboss.da.listings.api.dao.BlackArtifactDAO;
import org.jboss.da.listings.api.dao.GADAO;
import org.jboss.da.listings.api.dao.WhiteArtifactDAO;
import org.jboss.da.listings.api.model.Artifact;
import org.jboss.da.listings.api.model.BlackArtifact;
import org.jboss.da.listings.api.model.GA;
import org.jboss.da.listings.api.model.WhiteArtifact;
import org.jboss.da.listings.api.service.BlackArtifactService;
import org.jboss.da.model.rest.GAV;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.jboss.da.common.version.SuffixedVersion;
import org.jboss.da.common.version.VersionParser;

/**
 * 
 * @author Jozef Mrazek &lt;jmrazek@redhat.com&gt;
 *
 */
@ApplicationScoped
public class BlackArtifactServiceImpl extends ArtifactServiceImpl<BlackArtifact> implements BlackArtifactService {

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
    public Set<GAV> prefetchGAs(Set<org.jboss.da.model.rest.GA> gaToPrefetch) {
        if (gaToPrefetch.isEmpty()) {
            return Set.of();
        }
        Set<org.jboss.da.listings.api.model.GA> blocklistedGAs = gaDAO.findGAs(gaToPrefetch);
        if (blocklistedGAs.isEmpty()) {
            return Set.of();
        }
        return blackArtifactDAO.findArtifact(blocklistedGAs).stream().map(Artifact::toGAV).collect(Collectors.toSet());
    }

    @Override
    public boolean isBlocklisted(Set<GAV> cache, org.jboss.da.model.rest.GA ga, String version) {
        SuffixedVersion parsedVersion = versionParser.parse(version);

        GAV unsuffixedGAV = new GAV(ga, parsedVersion.unsuffixedVesion());
        GAV osgiGAV = new GAV(ga, VersionParser.getOSGiVersion(version));

        return cache.contains(unsuffixedGAV) || cache.contains(osgiGAV);
    }

    @Override
    public org.jboss.da.listings.api.service.ArtifactService.ArtifactStatus addArtifact(
            String groupId,
            String artifactId,
            String version) {

        String osgiVersion = VersionParser.getOSGiVersion(version);

        GA ga = gaDAO.findOrCreate(groupId, artifactId);

        BlackArtifact artifact = new BlackArtifact(ga, osgiVersion, currentUser());

        if (blackArtifactDAO.findArtifact(groupId, artifactId, osgiVersion).isPresent()) {
            return ArtifactStatus.NOT_MODIFIED;
        }

        Set<WhiteArtifact> whites = new HashSet<>();
        Optional<WhiteArtifact> rhA = whiteArtifactDAO.findArtifact(groupId, artifactId, osgiVersion);
        rhA.ifPresent(x -> whites.add(rhA.get()));
        Optional<WhiteArtifact> a = whiteArtifactDAO.findArtifact(groupId, artifactId, version);
        a.ifPresent(x -> whites.add(a.get()));

        ArtifactStatus status = ArtifactStatus.ADDED;

        if (!whites.isEmpty()) {
            status = ArtifactStatus.WAS_WHITELISTED;
        }

        blackArtifactDAO.create(artifact);
        return status;
    }

    @Override
    public Optional<BlackArtifact> getArtifact(String groupId, String artifactId, String version) {
        SuffixedVersion parsedVersion = versionParser.parse(version);
        Optional<BlackArtifact> artifact = blackArtifactDAO
                .findArtifact(groupId, artifactId, parsedVersion.unsuffixedVesion());
        if (parsedVersion.isSuffixed() && !artifact.isPresent()) {
            artifact = blackArtifactDAO.findArtifact(groupId, artifactId, VersionParser.getOSGiVersion(version));
        }
        return artifact;
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
        Optional<BlackArtifact> artifact = blackArtifactDAO.findArtifact(groupId, artifactId, version);
        if (artifact.isPresent()) {
            blackArtifactDAO.delete(artifact.get());
            return true;
        }
        return false;
    }

    @Override
    public SortedSet<BlackArtifact> getArtifacts(String groupId, String artifactId) {
        Comparator<BlackArtifact> baComparator = Comparator
                .comparing((BlackArtifact a) -> versionParser.parse(a.getVersion()));
        SortedSet<BlackArtifact> ret = new TreeSet<>(baComparator);
        ret.addAll(blackArtifactDAO.findArtifacts(groupId, artifactId));
        return ret;
    }
}
