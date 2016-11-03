package org.jboss.da.listings.impl.service;

import org.jboss.da.common.version.VersionParser;
import org.jboss.da.listings.api.dao.ArtifactDAO;
import org.jboss.da.listings.api.model.Artifact;
import org.jboss.da.listings.api.service.ArtifactService;

import javax.inject.Inject;

import java.util.List;

/**
 * 
 * @author Jozef Mrazek &lt;jmrazek@redhat.com&gt;
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
public abstract class ArtifactServiceImpl<T extends Artifact> implements ArtifactService<T> {

    @Inject
    protected VersionParser versionParser;

    protected abstract ArtifactDAO<T> getDAO();

    @Override
    public List<T> getAll() {
        return getDAO().findAll();
    }

}
