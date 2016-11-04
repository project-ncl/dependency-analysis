package org.jboss.da.communication.auth;

import java.util.Optional;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
public interface AuthenticatorService {

    /**
     * Returns access token that was used to authenticate.
     *
     * @return Acess token string or empty optional if not authenticated.
     */
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
