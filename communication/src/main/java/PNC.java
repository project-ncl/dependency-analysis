import org.jboss.da.communcation.pnc.ReadResource;
import org.jboss.da.communcation.pnc.authentication.PNCAuthFilter;
import org.jboss.da.communcation.pnc.entity.Product;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;

public class PNC {
    /**
     * Static factory to create an instance of PNCInterface
     *
     * @return instance of PNCInterface
     */
    public static PNCInterface getPNCInstance(boolean isAuthenticated) {
        ResteasyClient client = new ResteasyClientBuilder().build();

        // add authorization header for each REST request
        if (isAuthenticated) {
            client.register(new PNCAuthFilter());
        }
        String pncServer = ReadResource.getResource("pnc_server");
        ResteasyWebTarget target = client.target(pncServer);
        return target.proxy(PNCInterface.class);
    }
}
