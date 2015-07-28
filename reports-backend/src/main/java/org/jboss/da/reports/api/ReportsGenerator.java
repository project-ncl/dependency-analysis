package org.jboss.da.reports.api;

import org.jboss.da.communication.aprox.model.GAV;

import java.util.List;

/**
 *
 * @author Honza Brázdil <janinko.g@gmail.com>
 */
public interface ReportsGenerator {

    public ArtifactReport getReport(SCMLocator scml, List<Product> products);

    public ArtifactReport getReport(GAV gav, List<Product> products);

}
