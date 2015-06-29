package org.jboss.da.communcation.pnc;

import org.jboss.da.common.util.Configuration;
import org.jboss.da.communcation.pnc.authentication.PNCAuthFilter;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

public class PNCProducer {
    Configuration config = new Configuration();
    /**
     * Static factory to create an instance of org.jboss.da.communcation.pnc.PNCInterface
     *
     * @return instance of org.jboss.da.communcation.pnc.PNCInterface
     */
    @Produces
    public PNC getPNCInstance() {
        ResteasyClient client = new ResteasyClientBuilder().build();

        // add authorization header for each REST request
        client.register(new PNCAuthFilter());
        String pncServer = config.getConfig().getPncServer();
        ResteasyWebTarget target = client.target(pncServer);
        return target.proxy(PNC.class);
    }
}
