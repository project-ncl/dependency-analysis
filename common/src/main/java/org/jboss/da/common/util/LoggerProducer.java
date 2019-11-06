package org.jboss.da.common.util;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public class LoggerProducer {

    @Produces
    public Logger produceLogger(InjectionPoint ip) {
        return LoggerFactory.getLogger(ip.getMember().getDeclaringClass());
    }

    @Produces
    @UserLog
    public Logger produceUserLogger() {
        return LoggerFactory.getLogger("org.jboss.pnc._userlog_.da");
    }

}
