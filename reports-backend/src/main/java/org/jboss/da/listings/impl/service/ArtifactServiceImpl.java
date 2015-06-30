package org.jboss.da.listings.impl.service;

import java.util.List;

import org.jboss.da.listings.api.dao.ArtifactDAO;
import org.jboss.da.listings.api.model.Artifact;
import org.jboss.da.listings.api.service.ArtifactService;

/**
 * 
 * @author Jozef Mrazek <jmrazek@redhat.com>
 *
 */
public abstract class ArtifactServiceImpl<T extends Artifact> implements ArtifactService<T> {

    private Class type;

    public ArtifactServiceImpl(Class<T> type) {
        this.type = type;
    }

    protected abstract ArtifactDAO<T> getDAO();

    @Override
    public void addArtifact(String groupId, String artifactId, String version) {
        try {
            T artifact = (T) type.newInstance();
            artifact.setArtifactId(artifactId);
            artifact.setGroupId(groupId);
            artifact.setVersion(version);
            getDAO().create(artifact);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public T getArtifactByGAV(String groupId, String artifactId, String version) {
        return getDAO().findArtifactByGAV(groupId, artifactId, version);
    }

    @Override
    public boolean isArtifactByGAV(String groupId, String artifactId, String version) {
        if (getDAO().findArtifactByGAV(groupId, artifactId, version) != null)
            return true;
        return false;
    }

    @Override
    public List<T> getAll() {
        return getDAO().findAll();
    }
}
