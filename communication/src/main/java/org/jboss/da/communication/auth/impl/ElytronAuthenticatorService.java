package org.jboss.da.communication.auth.impl;

import org.jboss.da.communication.auth.AuthenticatorService;
import org.wildfly.security.auth.server.SecurityDomain;
import org.wildfly.security.auth.server.SecurityIdentity;
import org.wildfly.security.http.oidc.AccessToken;
import org.wildfly.security.http.oidc.OidcSecurityContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * Class based on using SecurityIdentity or OidcSecurityContext to get information from the user
 */
@RequestScoped
public class ElytronAuthenticatorService implements AuthenticatorService {

    @Inject
    HttpServletRequest servletRequest;

    private String preferredUsername;

    @PostConstruct
    public void postSetup() {
        OidcSecurityContext oidcSecurityContext = (OidcSecurityContext) servletRequest
                .getAttribute(OidcSecurityContext.class.getName());
        SecurityIdentity identity = SecurityDomain.getCurrent().getCurrentSecurityIdentity();
        if (oidcSecurityContext == null) {
            // if not using OIDC: either using LDAP or anonymous user
            preferredUsername = identity.getAttributes().getFirst("username");
        } else {
            AccessToken accessToken = oidcSecurityContext.getToken();
            preferredUsername = accessToken.getClaimValueAsString("preferred_username");
        }
    }

    @Override
    public Optional<String> accessToken() {
        return Optional.empty();
    }

    @Override
    public Optional<String> userId() {
        // TODO: this is important because DA uses it for its database
        return Optional.empty();
    }

    @Override
    public Optional<String> username() {
        return Optional.ofNullable(preferredUsername);
    }
}
