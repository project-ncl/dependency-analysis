package org.jboss.da.communication.auth.impl;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.da.communication.auth.AuthenticatorService;

/**
 * To be removed and eventually replaced by Dustin's new PNCAuth Quarkus extension.
 */
@ApplicationScoped
public class QuarkusOidcAuthenticatorService implements AuthenticatorService {

    @Override
    public Optional<String> accessToken() {
        return Optional.empty();
    }

    @Override
    public Optional<String> userId() {
        return Optional.of("");
    }

    @Override
    public Optional<String> username() {
        return Optional.of("");
    }
}
