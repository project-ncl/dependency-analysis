package org.jboss.da.test;

import java.io.IOException;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.pnc.quarkus.client.auth.runtime.PNCClientAuth;

import io.quarkus.test.Mock;

@Mock
@ApplicationScoped
public class PNCClientAuthMock implements PNCClientAuth {

    @Override
    public String getAuthToken() {
        return "1234";
    }

    @Override
    public String getHttpAuthorizationHeaderValue() {
        return "Bearer 1234";
    }

    @Override
    public String getHttpAuthorizationHeaderValueWithCachedToken() {
        return getHttpAuthorizationHeaderValue();
    }

    @Override
    public LDAPCredentials getLDAPCredentials() throws IOException {
        return new LDAPCredentials("user", "password");
    }

    @Override
    public ClientAuthType getConfiguredType() {
        return ClientAuthType.LDAP;
    }
}