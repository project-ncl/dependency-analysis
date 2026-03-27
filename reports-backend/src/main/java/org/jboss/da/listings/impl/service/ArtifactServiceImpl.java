package org.jboss.da.listings.impl.service;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.jboss.da.reports.impl.ReportsGeneratorImpl.DEFAULT_SUFFIX;

import java.io.IOException;
import java.util.List;

import jakarta.inject.Inject;

import org.jboss.da.listings.api.dao.ArtifactDAO;
import org.jboss.da.listings.api.dao.UserDAO;
import org.jboss.da.listings.api.model.Artifact;
import org.jboss.da.listings.api.model.User;
import org.jboss.da.listings.api.service.ArtifactService;
import org.jboss.pnc.common.version.VersionParser;
import org.jboss.pnc.quarkus.client.auth.runtime.PNCClientAuth;

import io.quarkus.oidc.UserInfo;

/**
 *
 * @author Jozef Mrazek &lt;jmrazek@redhat.com&gt;
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
public abstract class ArtifactServiceImpl<T extends Artifact> implements ArtifactService<T> {

    protected VersionParser versionParser = new VersionParser(DEFAULT_SUFFIX);

    @Inject
    UserInfo userInfo;

    @Inject
    PNCClientAuth auth;

    @Inject
    UserDAO users;

    protected abstract ArtifactDAO<T> getDAO();

    protected User currentUser() {
        String username;
        if (auth.getConfiguredType().equals(PNCClientAuth.ClientAuthType.OIDC)) {
            username = userInfo.getDisplayName();
        } else {
            try {
                username = auth.getLDAPCredentials().username();
            } catch (IOException e) {
                throw new IllegalStateException("No logged in user.");
            }
        }
        if (isEmpty(username)) {
            throw new IllegalStateException("No logged in user.");
        }
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
