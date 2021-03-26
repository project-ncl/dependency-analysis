package org.jboss.da.communication.pnc;

import org.jboss.da.common.json.GlobalConfig;
import org.jboss.da.common.json.LookupMode;
import org.jboss.da.common.util.ConfigurationParseException;
import org.jboss.da.communication.repository.api.RepositoryException;
import org.jboss.da.model.rest.GA;
import org.jboss.pnc.client.ArtifactClient21;
import org.jboss.pnc.client.Configuration;
import org.jboss.pnc.client.RemoteCollection;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.common.logging.MDCUtils;
import org.jboss.pnc.dto.response.ArtifactInfo;
import org.jboss.pnc.enums.RepositoryType;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:pkocandr@redhat.com">Petr Kocandrle</a>
 */
@ApplicationScoped
public class PncConnectorImpl implements PncConnector {

    @Inject
    private Logger log;

    private GlobalConfig globalConfig;

    @Inject
    public PncConnectorImpl(org.jboss.da.common.util.Configuration configuration) {
        try {
            globalConfig = configuration.getGlobalConfig();
        } catch (ConfigurationParseException ex) {
            throw new IllegalStateException("Configuration failure, can't parse default repository group", ex);
        }
    }

    private Collection<ArtifactInfo> getArtifacts(String identifierPattern, RepositoryType repoType, LookupMode mode)
            throws RepositoryException {
        ArtifactClient21 artifactClient = getArtifactClient();
        RemoteCollection<ArtifactInfo> artCollection;
        try {
            artCollection = artifactClient.getAllFiltered(
                    identifierPattern,
                    mode.getArtifactQualities(),
                    repoType,
                    Collections.singleton(mode.getBuildCategory()));
        } catch (RemoteResourceException ex) {
            throw new RepositoryException("Error when reading artifacts from PNC: " + ex, ex);
        }
        return artCollection.getAll();
    }

    @Override
    public List<String> getMavenVersions(GA ga, LookupMode mode) throws RepositoryException {
        String identifierPattern = ga.getGroupId() + ':' + ga.getArtifactId() + ":pom:*";
        Collection<ArtifactInfo> arts = getArtifacts(identifierPattern, RepositoryType.MAVEN, mode);

        List<String> versions = new ArrayList<>(arts.size());
        for (ArtifactInfo art : arts) {
            String[] parts = art.getIdentifier().split(":");
            if (parts.length == 4) {
                // TODO filtering by target repository if necessary
                versions.add(parts[3]);
            } else {
                log.error("Cannot read version for artifact with identifier %s", art.getIdentifier());
            }
        }
        return versions;
    }

    @Override
    public List<String> getNpmVersions(String packageName, LookupMode mode) throws RepositoryException {
        String identifierPattern = packageName + ":*";
        Collection<ArtifactInfo> arts = getArtifacts(identifierPattern, RepositoryType.NPM, mode);

        List<String> versions = new ArrayList<>(arts.size());
        for (ArtifactInfo art : arts) {
            String[] parts = art.getIdentifier().split(":");
            if (parts.length == 2) {
                // TODO filtering by target repository if necessary
                versions.add(parts[1]);
            } else {
                log.error("Cannot read version for artifact with identifier %s", art.getIdentifier());
            }
        }
        return versions;
    }

    private ArtifactClient21 getArtifactClient() {
        URI uri = URI.create(globalConfig.getPncUrl());

        Configuration config = getClientConfig(uri.getScheme(), uri.getHost(), uri.getPort());

        return new ArtifactClient21(config);
    }

    private Configuration getClientConfig(String protocol, String host, Integer port) {
        Configuration.ConfigurationBuilder builder = Configuration.builder();
        builder.protocol(protocol);
        builder.host(host);
        builder.port(port);

        builder.mdcToHeadersMappings(MDCUtils.getMDCToHeaderMappings());

        Configuration config = builder.build();
        return config;
    }

}
