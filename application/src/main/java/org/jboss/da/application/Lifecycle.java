package org.jboss.da.application;

import io.quarkus.logging.Log;
import io.quarkus.runtime.Shutdown;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;

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
