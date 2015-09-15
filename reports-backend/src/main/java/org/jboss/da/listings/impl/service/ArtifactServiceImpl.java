package org.jboss.da.listings.impl.service;

import org.jboss.da.communication.model.GAV;
import org.jboss.da.listings.api.dao.ArtifactDAO;
import org.jboss.da.listings.api.model.Artifact;
import org.jboss.da.listings.api.service.ArtifactService;
import org.slf4j.Logger;
import java.util.List;
import java.util.regex.Pattern;
import javax.inject.Inject;
import org.jboss.da.common.version.OSGiVersionParser;
import org.jboss.da.reports.backend.impl.VersionFinderImpl;

/**
 * 
 * @author Jozef Mrazek <jmrazek@redhat.com>
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
public abstract class ArtifactServiceImpl<T extends Artifact> implements ArtifactService<T> {

    @Inject
    private Logger log;

    private final Class<T> type;

    protected Pattern redhatSuffixPattern = Pattern
            .compile(VersionFinderImpl.PATTERN_SUFFIX_BUILT_VERSION + "$");

    @Inject
    protected OSGiVersionParser osgiParser;

    public ArtifactServiceImpl(Class<T> type) {
        this.type = type;
    }

    protected abstract ArtifactDAO<T> getDAO();

    @Override
    public List<T> getAll() {
        return getDAO().findAll();
    }

    @Override
    public boolean isArtifactPresent(GAV gav) {
        return isArtifactPresent(gav.getGroupId(), gav.getArtifactId(), gav.getVersion());
    }

    @Override
    public boolean removeArtifact(String groupId, String artifactId, String version) {
        T artifact = getDAO().findArtifact(groupId, artifactId, version);
        if (artifact != null) {
            getDAO().delete(artifact);
            return true;
        }
        return false;
    }
}
