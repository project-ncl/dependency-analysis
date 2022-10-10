package org.jboss.da.common.auth.impl;

import org.jboss.da.common.auth.AuthenticatorService;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import java.util.Optional;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@RequestScoped
public class KeycloakAuthenticatorService implements AuthenticatorService {

    @Inject
    private HttpServletRequest sr;

    private Optional<AccessToken> token() {
        KeycloakSecurityContext ksc = (KeycloakSecurityContext) sr
                .getAttribute(KeycloakSecurityContext.class.getName());

        if (ksc == null) {
            return Optional.empty();
        }

        return Optional.of(ksc.getToken());
    }

    @Override
    public Optional<String> userId() {
        return token().map(t -> t.getId());
    }

    @Override
    public Optional<String> username() {
        return token().map(t -> t.getPreferredUsername());
    }

    @Override
    public Optional<String> accessToken() {
        KeycloakSecurityContext ksc = (KeycloakSecurityContext) sr
                .getAttribute(KeycloakSecurityContext.class.getName());
        if (ksc == null) {
            return Optional.empty();
        }
        return Optional.of(ksc.getTokenString());
    }
}
