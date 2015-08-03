package org.jboss.da.listings.impl.service;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.jboss.da.listings.api.dao.ArtifactDAO;
import org.jboss.da.listings.api.model.Artifact;
import org.jboss.da.listings.api.service.ArtifactService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Jozef Mrazek <jmrazek@redhat.com>
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
    public boolean addArtifact(String groupId, String artifactId, String version) {
        if (getDAO().findArtifact(groupId, artifactId, version) != null) {
            return false;
        }
        try {
            T artifact = type.newInstance();
            artifact.setArtifactId(artifactId);
            artifact.setGroupId(groupId);
            artifact.setVersion(version);
            getDAO().create(artifact);
            return true;
        } catch (InstantiationException | IllegalAccessException e) {
            log.error("Artifact couldn't be added!", e);
        }
        return false;
    }

    @Override
    public T getArtifact(String groupId, String artifactId, String version) {
        return getDAO().findArtifact(groupId, artifactId, version);
    }

    @Override
    public boolean isArtifactPresent(String groupId, String artifactId, String version) {
        return (getDAO().findArtifact(groupId, artifactId, version) != null);
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
