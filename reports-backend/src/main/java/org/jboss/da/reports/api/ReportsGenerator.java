package org.jboss.da.reports.api;

import java.util.List;
import java.util.Optional;
import org.jboss.da.communication.CommunicationException;
import org.jboss.da.communication.model.GAV;

/**
 *
 * @author Honza Brázdil <janinko.g@gmail.com>
 * @author Jakub Bartecek <jbartece@redhat.com>
 */
public interface ReportsGenerator {

    public Optional<ArtifactReport> getReport(SCMLocator scml, List<Product> products);

    public Optional<ArtifactReport> getReport(GAV gav, List<Product> products);

    /**
     * Creates a report about built/not built/blacklisted artifacts. It performs searches
     * in the whole available repository. No restrictions on the artifacts belonging to the
     * certain product are applied.
     *
     * @param gav Top-level GAV for which is the report generated
     * @return Created report or empty Optional if the requested GAV was not found in the repository
     * @throws CommunicationException when there is a problem with communication with remote services
     */
    public Optional<ArtifactReport> getReport(GAV gav) throws CommunicationException;

}
