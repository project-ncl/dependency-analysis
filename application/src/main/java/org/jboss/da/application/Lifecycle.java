package org.jboss.da.application;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.logging.Log;
import io.quarkus.runtime.Shutdown;
import io.quarkus.runtime.Startup;

@ApplicationScoped
public class Lifecycle {

    @Startup
    void init() {
        Log.info("DA started");
    }

    @Shutdown
    void shutdown() {
        Log.info("DA shutdown");
    }
}
