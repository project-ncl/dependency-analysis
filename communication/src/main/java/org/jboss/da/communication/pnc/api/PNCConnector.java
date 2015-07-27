package org.jboss.da.communication.pnc.api;

import java.util.List;
import org.jboss.da.communication.pnc.model.BuildConfiguration;
import org.jboss.da.communication.pnc.model.BuildConfigurationSet;
import org.jboss.da.communication.pnc.model.Product;
import org.jboss.da.communication.pnc.model.Project;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
public interface PNCConnector {

    List<BuildConfigurationSet> getBuildConfigurationSets() throws Exception;

    List<BuildConfiguration> getBuildConfigurations() throws Exception;

    List<Product> getProducts() throws Exception;

    List<Project> getProjects() throws Exception;

}
