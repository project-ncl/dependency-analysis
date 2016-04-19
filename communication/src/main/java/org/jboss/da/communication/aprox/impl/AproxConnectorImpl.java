package org.jboss.da.communication.aprox.impl;

import org.commonjava.indy.model.core.Group;
import org.commonjava.indy.model.core.RemoteRepository;
import org.commonjava.indy.model.core.StoreKey;
import org.commonjava.indy.model.core.StoreType;
import org.commonjava.cartographer.graph.discover.patch.DepgraphPatcherConstants;
import org.commonjava.cartographer.request.ProjectGraphRequest;
import org.commonjava.cartographer.result.GraphExport;
import org.commonjava.indy.client.core.Indy;
import org.commonjava.indy.client.core.IndyClientException;
import org.commonjava.indy.client.core.module.IndyStoresClientModule;
import org.commonjava.indy.depgraph.client.DepgraphIndyClientModule;
import org.commonjava.maven.atlas.graph.rel.ProjectRelationship;
import org.commonjava.maven.atlas.graph.rel.RelationshipType;
import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.atlas.ident.ref.SimpleProjectVersionRef;
import org.jboss.da.common.json.DAConfig;
import org.jboss.da.common.util.Configuration;
import org.jboss.da.common.util.ConfigurationParseException;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.aprox.FindGAVDependencyException;
import org.jboss.da.communication.aprox.api.AproxConnector;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;
import org.jboss.da.communication.aprox.model.Repository;
import org.jboss.da.communication.aprox.model.VersionResponse;
import org.jboss.da.communication.pom.api.PomAnalyzer;
import org.jboss.da.communication.pom.model.MavenProject;
import org.jboss.da.model.rest.GA;
import org.jboss.da.model.rest.GAV;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class AproxConnectorImpl implements AproxConnector {

    @Inject
    private Logger log;

    @Inject
    private Configuration config;

    @Inject
    private PomAnalyzer pomAnalyzer;

    @Override
    public GAVDependencyTree getDependencyTreeOfGAV(GAV gav) throws CommunicationException,
            FindGAVDependencyException {

        if (!doesGAVExistInPublicRepo(gav)) {
            throw new FindGAVDependencyException("Could not find: " + gav
                    + "in public repo of Aprox");
        }

        DepgraphIndyClientModule mod = new DepgraphIndyClientModule();
        try (Indy indy = new Indy(config.getConfig().getAproxServer() + "/api", mod).connect()) {

            SimpleProjectVersionRef rootRef = new SimpleProjectVersionRef(gav.getGroupId(),
                    gav.getArtifactId(), gav.getVersion());

            ProjectGraphRequest req = mod
                    .newProjectGraphRequest()
                    .withWorkspaceId("export-" + rootRef.toString())
                    .withSource("group:public")
                    .withPatcherIds(DepgraphPatcherConstants.ALL_PATCHERS)
                    .withResolve(true)
                    .withGraph(
                            mod.newGraphDescription().withRoots(rootRef).withPreset("requires")
                                    .build()).build();

            GraphExport export = mod.graph(req);
            if (export == null) {
                log.warn("Analysis of the Dependency Tree of GAV: " + gav + " failed!");
                return new GAVDependencyTree(gav);
            }

            if (export.getRelationships() == null)
                return new GAVDependencyTree(gav);
            else
                return generateGAVDependencyTree(export, gav);
        } catch (IndyClientException | ConfigurationParseException e) {
            throw new CommunicationException(e);
        }
    }

    @Override
    public List<String> getVersionsOfGA(GA ga) throws CommunicationException {
        StringBuilder query = new StringBuilder();
        try {
            DAConfig config = this.config.getConfig();
            query.append(config.getAproxServer());
            query.append("/api/group/");
            query.append(config.getAproxGroup()).append('/');
            query.append(ga.getGroupId().replace(".", "/")).append("/");
            query.append(ga.getArtifactId()).append('/');
            query.append("maven-metadata.xml");

            URLConnection connection = new URL(query.toString()).openConnection();

            return parseMetadataFile(connection).getVersioning().getVersions().getVersion();
        } catch (FileNotFoundException ex) {
            return Collections.emptyList();
        } catch (IOException | ConfigurationParseException | CommunicationException e) {
            throw new CommunicationException("Failed to obtain versions for " + ga.toString()
                    + " from approx server with url " + query.toString(), e);
        }
    }

    @Override
    public Optional<MavenProject> getPom(GAV gav) throws CommunicationException {
        Optional<InputStream> is = getPomStream(gav);
        if (is.isPresent()) {
            return pomAnalyzer.readPom(getPomStream(gav).get());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<InputStream> getPomStream(GAV gav) throws CommunicationException {
        StringBuilder query = new StringBuilder();
        try {
            DAConfig cfg = this.config.getConfig();
            query.append(cfg.getAproxServer());
            query.append("/api/group/public/");
            query.append(gav.getGroupId().replace(".", "/")).append("/");
            query.append(gav.getArtifactId()).append('/');
            query.append(gav.getVersion()).append('/');
            query.append(gav.getArtifactId()).append('-').append(gav.getVersion()).append(".pom");

            URLConnection connection = new URL(query.toString()).openConnection();
            return Optional.of(connection.getInputStream());
        } catch (FileNotFoundException ex) {
            return Optional.empty();
        } catch (IOException | ConfigurationParseException e) {
            throw new CommunicationException("Failed to obtain pom for " + gav.toString()
                    + " from approx server with url " + query.toString(), e);
        }
    }

    @Override
    public RepositoryManipulationStatus addRepositoryToGroup(Repository repository)
            throws CommunicationException {
        try (Indy indy = new Indy(config.getConfig().getAproxServer() + "/api",
                new IndyStoresClientModule()).connect()) {
            RemoteRepository repo;
            try {
                repo = indy.stores().load(StoreType.remote, repository.getName(),
                        RemoteRepository.class);
            } catch (IllegalArgumentException e) {
                return RepositoryManipulationStatus.WRONG_NAME_OR_URL;
            }
            if (repo != null && !repo.getUrl().equals(repository.getUrl())) {
                return RepositoryManipulationStatus.NAME_EXIST_DIFFERENT_URL;
            } else if (repo == null) {
                repo = indy.stores().create(
                        new RemoteRepository(repository.getName(), repository.getUrl()),
                        "Add remote repo", RemoteRepository.class);
            }

            Group group = indy.stores().load(StoreType.group, config.getConfig().getAproxGroup(),
                    Group.class);

            group.addConstituent(repo);

            if (indy.stores().update(group, "Add repository to group"))
                return RepositoryManipulationStatus.DONE;
        } catch (IndyClientException | ConfigurationParseException e) {
            throw new CommunicationException(e);
        }
        return null;
    }

    @Override
    public RepositoryManipulationStatus removeRepositoryFromGroup(Repository repository)
            throws CommunicationException {
        try (Indy aprox = new Indy(config.getConfig().getAproxServer() + "/api",
                new IndyStoresClientModule()).connect()) {
            RemoteRepository repo = aprox.stores().load(StoreType.remote, repository.getName(),
                    RemoteRepository.class);

            Group group = aprox.stores().load(StoreType.group, config.getConfig().getAproxGroup(),
                    Group.class);

            if (repo == null) {
                return RepositoryManipulationStatus.NAME_NOT_EXIST;
            }
            if (repo.getUrl().equals(repository.getUrl())) {
                group.removeConstituent(repo);
            } else {
                return RepositoryManipulationStatus.NAME_EXIST_DIFFERENT_URL;
            }

            aprox.stores().update(group, "Remove repository from group");
            return RepositoryManipulationStatus.DONE;
        } catch (IndyClientException | ConfigurationParseException e) {
            throw new CommunicationException(e);
        }
    }

    @Override
    public List<Repository> getAllRepositoriesFromGroup() throws CommunicationException {
        try (Indy aprox = new Indy(config.getConfig().getAproxServer() + "/api",
                new IndyStoresClientModule()).connect()) {
            List<Repository> repoList = new ArrayList<>();
            Group daGroup = aprox.stores().load(StoreType.group,
                    config.getConfig().getAproxGroup(), Group.class);
            for (StoreKey key : daGroup.getConstituents()) {
                if (key.getType() == StoreType.remote) {
                    RemoteRepository repo = aprox.stores().load(StoreType.remote, key.getName(),
                            RemoteRepository.class);
                    repoList.add(new Repository(key.getName(), repo.getUrl()));
                }
            }
            return repoList;
        } catch (IndyClientException | ConfigurationParseException e) {
            throw new CommunicationException(e);
        }
    }

    @Override
    /**
     * Implementation note: dcheung tried to initially use HttpURLConnection
     * and send a 'HEAD' request to the resource. Even though that worked,
     * for some reason this completely makes Arquillian fail to deploy the testsuite.
     * For that reason, dcheung switched to using a simple URL object instead with the
     * try-catch logic.
     *
     * No dcheung doesn't usually talks about himself in the third person..
     */
    public boolean doesGAVExistInPublicRepo(GAV gav) throws CommunicationException {
        StringBuilder query = new StringBuilder();

        try {
            DAConfig config = this.config.getConfig();
            query.append(config.getAproxServer());
            query.append("/api/group/public/");
            query.append(gav.getGroupId().replace(".", "/")).append("/");
            query.append(gav.getArtifactId()).append('/');
            query.append(gav.getVersion()).append('/');
            query.append(gav.getArtifactId()).append("-").append(gav.getVersion()).append(".pom");

            URLConnection connection = new URL(query.toString()).openConnection();
            try {
                connection.getInputStream().close();
                // if we've reached here, then it means the pom exists
                return true;
            } catch (FileNotFoundException e) {
                // if we've reached here, the resource is not available
                return false;
            }

        } catch (IOException | ConfigurationParseException e) {
            throw new CommunicationException("Failed to establish a connection with Aprox: "
                    + query.toString(), e);
        }
    }

    private VersionResponse parseMetadataFile(URLConnection connection) throws IOException,
            CommunicationException {
        try (InputStream in = connection.getInputStream()) {
            JAXBContext jaxbContext = JAXBContext.newInstance(VersionResponse.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (VersionResponse) jaxbUnmarshaller.unmarshal(in);
        } catch (JAXBException e) {
            throw new CommunicationException("Failed to parse metadataFile", e);
        }
    }

    private GAVDependencyTree generateGAVDependencyTree(GraphExport export, GAV rootGAV) {

        // keep a map of gav -> GAVDependencyTree object
        Map<GAV, GAVDependencyTree> gavMapper = new HashMap<>();

        // create the root GAVDependencyTree and return it
        GAVDependencyTree root = new GAVDependencyTree(rootGAV);
        gavMapper.put(rootGAV, root);

        for (ProjectRelationship<?, ?> rel : export.getRelationships()) {
            if (rel.getType().equals(RelationshipType.DEPENDENCY)) {

                ProjectVersionRef declaring = rel.getDeclaring();
                ProjectVersionRef dependencyArtifact = rel.getTargetArtifact();

                GAV declaringGAV = generateGAV(declaring);
                GAV dependencyGAV = generateGAV(dependencyArtifact);

                GAVDependencyTree declaringDT = addGAVDependencyTreeToGAVMapper(gavMapper,
                        declaringGAV);
                GAVDependencyTree dependencyDT = addGAVDependencyTreeToGAVMapper(gavMapper,
                        dependencyGAV);

                // set the dependency relationship between GAVDependencyTree here
                declaringDT.addDependency(dependencyDT);
            }
        }
        return root;
    }

    private GAVDependencyTree addGAVDependencyTreeToGAVMapper(
            Map<GAV, GAVDependencyTree> gavMapper, GAV gav) {
        return gavMapper.computeIfAbsent(gav, k -> new GAVDependencyTree(k));
    }

    private GAV generateGAV(ProjectVersionRef ref) {
        return new GAV(ref.getGroupId(), ref.getArtifactId(), ref.getVersionString());
    }
}
