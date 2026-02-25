package org.jboss.da.common.logging;

import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
//import io.quarkus.runtime.Startup;

//@Startup
@ApplicationScoped
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
