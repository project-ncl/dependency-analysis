package org.jboss.da.communication.pnc;

import org.jboss.da.common.json.GlobalConfig;
import org.jboss.da.common.util.ConfigurationParseException;
import org.jboss.da.communication.repository.api.RepositoryException;
import org.jboss.da.model.rest.GA;
import org.jboss.pnc.client.ArtifactClient;
import org.jboss.pnc.client.Configuration;
import org.jboss.pnc.client.RemoteCollection;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.common.logging.MDCUtils;
import org.jboss.pnc.dto.response.ArtifactInfo;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.RepositoryType;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author <a href="mailto:pkocandr@redhat.com">Petr Kocandrle</a>
 */
@ApplicationScoped
public class PncConnectorImpl implements PncConnector {

    private static final EnumSet<ArtifactQuality> persistentQuals = EnumSet
            .of(ArtifactQuality.NEW, ArtifactQuality.VERIFIED, ArtifactQuality.TESTED);
    private static final EnumSet<ArtifactQuality> temporaryQuals = EnumSet
            .of(ArtifactQuality.NEW, ArtifactQuality.VERIFIED, ArtifactQuality.TESTED, ArtifactQuality.TEMPORARY);

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
            boolean temporaryBuild) throws RepositoryException {
        ArtifactClient artifactClient = getArtifactClient();
        EnumSet<ArtifactQuality> qualities = temporaryBuild ? temporaryQuals : persistentQuals;
        RemoteCollection<ArtifactInfo> artCollection;
        try {
            artCollection = artifactClient.getAllFiltered(identifierPattern, qualities, repoType);
        } catch (RemoteResourceException ex) {
            throw new RepositoryException("Error when reading artifacvts from PNC: " + ex, ex);
        }
        return artCollection.getAll();
    }

    @Override
    public List<String> getMavenVersions(GA ga, boolean temporaryBuild) throws RepositoryException {
        String identifierPattern = ga.getGroupId() + ':' + ga.getArtifactId() + ":pom:*";
        Collection<ArtifactInfo> arts = getArtifacts(identifierPattern, RepositoryType.MAVEN, temporaryBuild);

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
    public List<String> getNpmVersions(String packageName, boolean temporaryBuild) throws RepositoryException {
        String identifierPattern = packageName + ":*";
        Collection<ArtifactInfo> arts = getArtifacts(identifierPattern, RepositoryType.NPM, temporaryBuild);

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

    private ArtifactClient getArtifactClient() {
        URI uri = URI.create(globalConfig.getPncUrl());

        Configuration config = getClientConfig(uri.getScheme(), uri.getHost(), uri.getPort());

        return new ArtifactClient(config);
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
