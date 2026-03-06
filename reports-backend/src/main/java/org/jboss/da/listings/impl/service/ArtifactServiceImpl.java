package org.jboss.da.listings.impl.service;

import org.jboss.da.communication.auth.AuthenticatorService;
import org.jboss.da.listings.api.dao.ArtifactDAO;
import org.jboss.da.listings.api.dao.UserDAO;
import org.jboss.da.listings.api.model.Artifact;
import org.jboss.da.listings.api.model.User;
import org.jboss.da.listings.api.service.ArtifactService;
import org.jboss.pnc.common.version.VersionParser;

import javax.inject.Inject;

import java.util.List;

import static org.jboss.da.reports.impl.ReportsGeneratorImpl.DEFAULT_SUFFIX;

/**
 * 
 * @author Jozef Mrazek &lt;jmrazek@redhat.com&gt;
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
public abstract class ArtifactServiceImpl<T extends Artifact> implements ArtifactService<T> {

    protected VersionParser versionParser = new VersionParser(DEFAULT_SUFFIX);

    @Inject
    AuthenticatorService auth;

    @Inject
    private UserDAO users;

    protected abstract ArtifactDAO<T> getDAO();

    protected User currentUser() {
        String username = auth.username().orElseThrow(() -> new IllegalStateException("No logged in user."));

        return users.findUser(username).orElseGet(() -> {
            User u = new User(username);
            users.create(u);
            return u;
        });
    }

    @Override
    public List<T> getAll() {
        return getDAO().findAll();
    }

}
