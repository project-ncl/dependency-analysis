package org.jboss.da.listings.impl.service;

import org.jboss.da.communication.model.GAV;
import org.jboss.da.listings.api.dao.ArtifactDAO;
import org.jboss.da.listings.api.model.Artifact;
import org.jboss.da.listings.api.service.ArtifactService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.List;

/**
 * 
 * @author Jozef Mrazek <jmrazek@redhat.com>
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
public abstract class ArtifactServiceImpl<T extends Artifact> implements ArtifactService<T> {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private Class<T> type;

    public ArtifactServiceImpl(Class<T> type) {
        this.type = type;
    }

    protected abstract ArtifactDAO<T> getDAO();

    @Override
    public T getArtifact(String groupId, String artifactId, String version) {
        return getDAO().findArtifact(groupId, artifactId, version);
    }

    @Override
    public boolean isArtifactPresent(String groupId, String artifactId, String version) {
        return (getDAO().findArtifact(groupId, artifactId, version) != null);
    }

    @Override
    public boolean isArtifactPresent(GAV gav) {
        return (getDAO().findArtifact(gav.getGroupId(), gav.getArtifactId(), gav.getVersion()) != null);
    }

    @Override
    public List<T> getAll() {
        return getDAO().findAll();
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
