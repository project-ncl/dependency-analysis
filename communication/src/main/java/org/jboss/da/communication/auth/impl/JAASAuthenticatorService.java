package org.jboss.da.communication.auth.impl;

/**
 * Authenticator service used for testing purposes.
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
// @RequestScoped
// @Alternative
// public class JAASAuthenticatorService implements AuthenticatorService {
//
// @Inject
// private HttpServletRequest sr;
//
// private static Optional<String> user = Optional.empty();
//
// private Optional<String> user() {
// System.out.println("srajda: " + sr + " user " + sr.getRemoteUser());
// if (sr.getRemoteUser() == null) {
// return user;
// }
// return Optional.ofNullable(sr.getRemoteUser());
// }
//
// @Override
// public Optional<String> userId() {
// return user().map(u -> Integer.toString(u.hashCode(), 16));
// }
//
// @Override
// public Optional<String> username() {
// return user();
// }
//
// @Override
// public Optional<String> accessToken() {
// if (!user().isPresent()) {
// return Optional.empty();
// }
// return Optional.of("--NO-TOKEN-AVAILABLE--");
// }
//
// /**
// * Force logged in user.
// *
// * @param username
// */
// public static void setUser(String username) {
// user = Optional.ofNullable(username);
// }
// }
