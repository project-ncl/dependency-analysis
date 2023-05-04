package org.jboss.da.communication.pnc;

import org.jboss.da.common.json.GlobalConfig;
import org.jboss.da.common.json.LookupMode;
import org.jboss.da.common.util.ConfigurationParseException;
import org.jboss.da.communication.repository.api.RepositoryException;
import org.jboss.da.model.rest.GA;
import org.jboss.pnc.api.dependencyanalyzer.dto.QualifiedVersion;
import org.jboss.pnc.client.ArtifactClient;
import org.jboss.pnc.client.Configuration;
import org.jboss.pnc.client.RemoteCollection;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.common.logging.MDCUtils;
import org.jboss.pnc.dto.requests.QValue;
import org.jboss.pnc.dto.response.ArtifactInfo;
import org.jboss.pnc.enums.RepositoryType;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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

    private Collection<ArtifactInfo> getArtifacts(
            String identifierPattern,
            RepositoryType repoType,
            LookupMode mode,
            Set<QValue> qualifiers) throws RepositoryException {
        ArtifactClient artifactClient = getArtifactClient();
        RemoteCollection<ArtifactInfo> artCollection;
        try {
            if (qualifiers.isEmpty()) {
                artCollection = artifactClient.getAllFiltered(
                        identifierPattern,
                        mode.getArtifactQualities(),
                        repoType,
                        mode.getBuildCategories(),
                        null);
            } else {
                artCollection = artifactClient.getAllFiltered(
                        identifierPattern,
                        mode.getArtifactQualities(),
                        repoType,
                        mode.getBuildCategories(),
                        qualifiers);
            }
        } catch (RemoteResourceException ex) {
            log.debug("Error when reading artifacts from PNC: " + ex, ex);
            throw new RepositoryException("Error when reading artifacts from PNC: " + ex, ex);
        }
        return artCollection.getAll();
    }

    @Override
    public List<QualifiedVersion> getMavenVersions(GA ga, LookupMode mode, Set<QValue> qualifiers)
            throws RepositoryException {
        String identifierPattern = ga.getGroupId() + ':' + ga.getArtifactId() + ":pom:*";
        Collection<ArtifactInfo> arts = getArtifacts(identifierPattern, RepositoryType.MAVEN, mode, qualifiers);

        List<QualifiedVersion> versions = new ArrayList<>(arts.size());
        for (ArtifactInfo art : arts) {
            String[] parts = art.getIdentifier().split(":");
            if (parts.length == 4) {
                // TODO filtering by target repository if necessary
                versions.add(new QualifiedVersion(parts[3], art.getQualifiers()));
                log.error("Cannot read version for artifact with identifier {}", art.getIdentifier());
            }
        }
        return versions;
    }

    @Override
    public List<QualifiedVersion> getNpmVersions(String packageName, LookupMode mode, Set<QValue> qualifiers)
            throws RepositoryException {
        String identifierPattern = packageName + ":*";
        Collection<ArtifactInfo> arts = getArtifacts(identifierPattern, RepositoryType.NPM, mode, qualifiers);

        List<QualifiedVersion> versions = new ArrayList<>(arts.size());
        for (ArtifactInfo art : arts) {
            String[] parts = art.getIdentifier().split(":");
            if (parts.length == 2) {
                // TODO filtering by target repository if necessary
                versions.add(new QualifiedVersion(parts[1], art.getQualifiers()));
            } else {
                log.error("Cannot read version for artifact with identifier {}", art.getIdentifier());
            }
        }
        return versions;
    }

    private ArtifactClient getArtifactClient() {
        URI uri = URI.create(globalConfig.getPncUrl());

        Configuration config = getClientConfig(uri.getScheme(), uri.getHost(), uri.getPort());

        return new ArtifactClient(config);
    }

    private Configuration getClientConfig(String protocol, String host, int port) {
        Configuration.ConfigurationBuilder builder = Configuration.builder();
        builder.protocol(protocol);
        builder.host(host);
        builder.port(port == -1 ? null : port);

        builder.mdcToHeadersMappings(MDCUtils.HEADER_KEY_MAPPING);

        Configuration config = builder.build();
        return config;
    }

}
