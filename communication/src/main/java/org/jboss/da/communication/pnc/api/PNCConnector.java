package org.jboss.da.communication.pnc.api;

import org.jboss.da.communication.pnc.model.BPMTask;
import org.jboss.da.communication.pnc.model.BuildConfiguration;
import org.jboss.da.communication.pnc.model.BuildConfigurationSet;
import org.jboss.da.communication.pnc.model.ProductVersion;
import org.jboss.da.communication.pnc.model.RepositoryConfiguration;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public interface PNCConnector {

    /**
     * Finds BuildConfigurationSet with specific product version id and build configurations ids from pnc
     *
     * @param productVersionId
     * @param buildConfigurationIds
     * @return Optional.empty() if buildConfigurationSet not found, else the BuildConfigurationSet
     * @throws PNCRequestException Thrown if the communication with PNC failed
     */
    Optional<BuildConfigurationSet> findBuildConfigurationSet(int productVersionId,
            List<Integer> buildConfigurationIds) throws PNCRequestException;

    /**
     * Find ProductVersion assigned to a particular product and having a specific
     * version. Since each product has unique product versions, we can only find
     * one such ProductVersion
     * @param productId the product id
     * @param version version to find
     * @return Optional.empty() if productVersion not found, else the productVersion
     * @throws PNCRequestException Thrown if the communication with PNC failed
     */
    Optional<ProductVersion> findProductVersion(int productId, String version)
            throws PNCRequestException;

    /**
     * Gets BuildConfiguration from PNC with the specific name
     *
     * @param name name of the BC
     * @return BC with specified name or empty optional if no such BC was found
     * @throws PNCRequestException Thrown if communication with PNC failed
     */
    Optional<BuildConfiguration> getBuildConfiguration(String name) throws PNCRequestException;

    /**
     * Finds Repository Configuration with specific product version id and build configurations ids from pnc
     *
     * @param url Internal or external url of the repository
     * @return List of Repository configurat if buildConfigurationSet not found, else the BuildConfigurationSet
     * @throws PNCRequestException Thrown if the communication with PNC failed
     */
    List<RepositoryConfiguration> getRepositoryConfigurations(String url)
            throws PNCRequestException;

    /**
     * Gets all BuildConfigurations from PNC with the specific SCM URL and SCM Revision
     *
     * @param repositoryId ID of the Repository Configuration
     * @param scmRevision SCM revision expected in BC
     * @return List of BCs with specified SCM URL and SCM revision or empty list if no BC was found
     * @throws PNCRequestException Thrown if communication with PNC failed
     */
    List<BuildConfiguration> getBuildConfigurations(int repositoryId, String scmRevision)
            throws PNCRequestException;

    /**
     * Gets all BuildConfigurations from PNC with the specific SCM URL and SCM Revision
     *
     * @param scmUrl SCM URL expected in BC
     * @param scmRevision SCM revision expected in BC
     * @return List of BCs with specified SCM URL and SCM revision or empty list if no BC was found
     * @throws PNCRequestException Thrown if communication with PNC failed
     */
    List<BuildConfiguration> getBuildConfigurations(String scmUrl, String scmRevision)
            throws PNCRequestException;

    /**
     * Gets BPM Task from PNC with the specific id
     *
     * @param taskId id of the task
     * @return BPM Task with specified id or empty optional if no such task exists
     * @throws PNCRequestException Thrown if communication with PNC failed
     */
    public Optional<BPMTask> getBPMTask(int taskId) throws PNCRequestException;

}
