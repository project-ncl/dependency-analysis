package org.jboss.da.common.logging;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Startup;
import javax.inject.Singleton;

@Startup
@Singleton
@Slf4j
public class AppLifecycle {

    @PostConstruct
    public void initialize() {
        log.info("The application is starting");
    }

    @PreDestroy
    public void destroy() {
        log.info("The application is shutting down");
    }
}
