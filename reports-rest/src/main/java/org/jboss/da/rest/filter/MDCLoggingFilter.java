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

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;

import org.jboss.pnc.api.constants.MDCKeys;
import org.jboss.pnc.common.log.MDCUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import io.opentelemetry.api.trace.Span;

@Provider
public class MDCLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger logger = LoggerFactory.getLogger(MDCLoggingFilter.class);
    private static final String REQUEST_EXECUTION_START = "request-execution-start";

    @Override
    public void filter(ContainerRequestContext requestContext) {

        requestContext.setProperty(REQUEST_EXECUTION_START, System.currentTimeMillis());
        MDCUtils.setMDCFromRequestContext(requestContext);
        MDCUtils.addMDCFromOtelHeadersWithFallback(requestContext, Span.current().getSpanContext(), false);

        UriInfo uriInfo = requestContext.getUriInfo();
        Request request = requestContext.getRequest();
        logger.info("Requested {} {}.", request.getMethod(), uriInfo.getRequestUri());
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        Long startTime = (Long) requestContext.getProperty(REQUEST_EXECUTION_START);

        String took;
        if (startTime == null) {
            took = "-1";
        } else {
            took = Long.toString(System.currentTimeMillis() - startTime);
        }

        try (MDC.MDCCloseable ignored = MDC.putCloseable(MDCKeys.REQUEST_TOOK, took);
                MDC.MDCCloseable ignored2 = MDC
                        .putCloseable(MDCKeys.RESPONSE_STATUS, Integer.toString(responseContext.getStatus()));) {
            logger.info("Completed {}, took: {}ms.", requestContext.getUriInfo().getPath(), took);
        }
    }
}
