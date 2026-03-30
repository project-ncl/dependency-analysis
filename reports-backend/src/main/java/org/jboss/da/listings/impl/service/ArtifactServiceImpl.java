package org.jboss.da.listings.impl.service;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.jboss.da.reports.impl.ReportsGeneratorImpl.DEFAULT_SUFFIX;

import java.util.List;

import jakarta.inject.Inject;

import org.jboss.da.listings.api.dao.ArtifactDAO;
import org.jboss.da.listings.api.dao.UserDAO;
import org.jboss.da.listings.api.model.Artifact;
import org.jboss.da.listings.api.model.User;
import org.jboss.da.listings.api.service.ArtifactService;
import org.jboss.pnc.common.version.VersionParser;
import org.slf4j.Logger;

import io.quarkus.security.identity.SecurityIdentity;

/**
 *
 * @author Jozef Mrazek &lt;jmrazek@redhat.com&gt;
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
public abstract class ArtifactServiceImpl<T extends Artifact> implements ArtifactService<T> {

    protected VersionParser versionParser = new VersionParser(DEFAULT_SUFFIX);

    @Inject
    Logger log;

    @Inject
    SecurityIdentity identity;

    @Inject
    UserDAO users;

    protected abstract ArtifactDAO<T> getDAO();

    protected User currentUser() {
        String username = identity.getPrincipal().getName();
        if (isEmpty(username)) {
            throw new IllegalStateException("No logged in user.");
        }
        log.info("Looking for user with name {}", username);
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
