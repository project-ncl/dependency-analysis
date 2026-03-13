package org.jboss.da.communication.auth;

import java.util.Optional;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public interface AuthenticatorService {

    /**
     * Returns access token that was used to authenticate.
     *
     * @return Access token string or empty optional if not authenticated.
     */
    @Deprecated
    Optional<String> accessToken();

    /**
     * Returns unique user id of connected user.
     *
     * @return
     */
    Optional<String> userId();

    /**
     * Returns username of connected user.
     *
     * @return
     */
    Optional<String> username();
}
