package org.jboss.da.reports.api;

import org.jboss.da.communication.model.GAV;

import java.util.List;

/**
 *
 * @author Honza Brázdil <janinko.g@gmail.com>
 * @author Jakub Bartecek <jbartece@redhat.com>
 */
public interface ReportsGenerator {

    public ArtifactReport getReport(SCMLocator scml, List<Product> products);

    public ArtifactReport getReport(GAV gav, List<Product> products);

    /**
     * Creates a report about built/not built/blacklisted artifacts. It performs searches
     * in the whole available repository. No restrictions on the artifacts belonging to the
     * certain product are applied.
     *
     * @param gav Top-level GAV for which is the report generated
     * @return Created report or null if the requested GAV was not found in the repository
     */
    public ArtifactReport getReport(GAV gav);

}
