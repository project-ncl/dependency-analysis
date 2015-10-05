package org.jboss.da.communication.pnc.authentication;

import org.jboss.da.communication.pnc.impl.AuthenticationException;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

/**
 * Interceptor, which takes care of authenticating to PNC, when the access token is invalid.
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
@Interceptor
@PncAuthenticated
public class PncAuthenticationInterceptor {

    @Inject
    private PNCAuthentication pncAuthentication;

    @AroundInvoke
    public Object intercept(InvocationContext ctx) throws Exception {
        try {
            return ctx.proceed();
        } catch (AuthenticationException e) {
            // TODO solve multiple authentication because of concurrency
            pncAuthentication.authenticate();
            return ctx.proceed();
        }
    }

}
