package org.jboss.da.listings.impl.service;

import org.jboss.da.common.version.OSGiVersionParser;
import org.jboss.da.communication.model.GAV;
import org.jboss.da.listings.api.dao.ArtifactDAO;
import org.jboss.da.listings.api.dao.WhiteArtifactDAO;
import org.jboss.da.listings.api.model.WhiteArtifact;
import org.jboss.da.listings.api.service.BlackArtifactService;
import org.jboss.da.listings.api.service.WhiteArtifactService;
import org.jboss.da.reports.backend.api.VersionFinder;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 
 * @author Jozef Mrazek <jmrazek@redhat.com>
 *
 */
@ApplicationScoped
public class WhiteArtifactServiceImpl extends ArtifactServiceImpl<WhiteArtifact> implements
        WhiteArtifactService {

    @Inject
    private BlackArtifactService blackArtifactService;

    @Inject
    private WhiteArtifactDAO whiteArtifactDAO;

    @Inject
    private OSGiVersionParser osgiParser;

    @Override
    protected ArtifactDAO<WhiteArtifact> getDAO() {
        return whiteArtifactDAO;
    }

    @Override
    public org.jboss.da.listings.api.service.ArtifactService.STATUS addArtifact(String groupId,
            String artifactId, String version) {
        if (!redhatSuffixPattern.matcher(version).find()) {
            throw new IllegalArgumentException("Version " + version
                    + " doesn't contain redhat suffix");
        }

        version = osgiParser.getOSGiVersion(version);

        WhiteArtifact white = new WhiteArtifact(groupId, artifactId, version);
        if (blackArtifactService.isArtifactPresent(groupId, artifactId, version)) {
            return STATUS.IS_BLACKLISTED;
        }
        if (whiteArtifactDAO.findArtifact(groupId, artifactId, version) != null) {
            return STATUS.NOT_MODIFIED;
        }
        whiteArtifactDAO.create(white);
        return STATUS.ADDED;
    }

    @Override
    public List<WhiteArtifact> getArtifacts(String groupId, String artifactId, String version) {
        if(redhatSuffixPattern.matcher(version).find()){
            return Optional.ofNullable(whiteArtifactDAO.findArtifact(groupId, artifactId, version))
                    .map(Collections::singletonList)
                    .orElse(Collections.emptyList());
        }else{
            return whiteArtifactDAO.findRedhatArtifact(groupId, artifactId, version);
        }
    }

    @Override
    public List<WhiteArtifact> getArtifacts(GAV gav) {
        return getArtifacts(gav.getGroupId(), gav.getArtifactId(), gav.getVersion());
    }

    @Override
    public boolean isArtifactPresent(String groupId, String artifactId, String version) {
        return !getArtifacts(groupId, artifactId, version).isEmpty();
    }

}
