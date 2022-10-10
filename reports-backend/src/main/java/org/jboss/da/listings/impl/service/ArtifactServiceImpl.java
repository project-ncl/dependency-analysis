package org.jboss.da.listings.impl.service;

import org.jboss.da.common.version.VersionParser;
import org.jboss.da.common.auth.AuthenticatorService;
import org.jboss.da.listings.api.dao.ArtifactDAO;
import org.jboss.da.listings.api.dao.UserDAO;
import org.jboss.da.listings.api.model.Artifact;
import org.jboss.da.listings.api.model.User;
import org.jboss.da.listings.api.service.ArtifactService;

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
        String userId = auth.userId().orElseThrow(() -> new IllegalStateException("No logged in user."));

        User user = users.findUser(userId).orElseGet(() -> {
            User u = new User(username, userId);
            users.create(u);
            return u;
        });
        if (!user.getUsername().equals(username)) {
            user.setUsername(username);
            users.update(user);
        }
        return user;
    }

    @Override
    public List<T> getAll() {
        return getDAO().findAll();
    }

}
