/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.da.rest.filter;

import org.jboss.da.communication.auth.AuthenticatorService;
import org.jboss.pnc.api.constants.MDCKeys;
import org.jboss.pnc.common.log.MDCUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import io.opentelemetry.api.trace.Span;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Provider
public class MDCLoggingFilter implements ContainerRequestFilter {

    private Logger logger = LoggerFactory.getLogger(MDCLoggingFilter.class);

    @Inject
    private AuthenticatorService userService;

    @Inject
    private HttpServletRequest sr;

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        MDCUtils.setMDCFromRequestContext(containerRequestContext);
        MDCUtils.addMDCFromOtelHeadersWithFallback(containerRequestContext, Span.current().getSpanContext(), true);
        addAuditMDC();
    }

    public void addAuditMDC() {
        userService.username().ifPresent(username -> MDC.put(MDCKeys.USER_NAME_KEY, username));
        if (sr != null) {
            MDC.put(MDCKeys.SRC_IP_KEY, sr.getRemoteAddr());
            String forwardedFor = sr.getHeader("X-FORWARDED-FOR");
            if (forwardedFor != null) {
                MDC.put(MDCKeys.X_FORWARDED_FOR_KEY, forwardedFor);
            }
        }
    }
}
