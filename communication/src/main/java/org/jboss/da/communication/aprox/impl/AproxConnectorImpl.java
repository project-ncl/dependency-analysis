package org.jboss.da.communication.aprox.impl;

import org.apache.commons.io.FileUtils;
import org.apache.maven.scm.ScmException;
import org.commonjava.aprox.client.core.Aprox;
import org.commonjava.aprox.client.core.AproxClientException;
import org.commonjava.aprox.depgraph.client.DepgraphAproxClientModule;
import org.commonjava.cartographer.graph.discover.patch.DepgraphPatcherConstants;
import org.commonjava.cartographer.request.ProjectGraphRequest;
import org.commonjava.cartographer.result.GraphExport;
import org.commonjava.maven.atlas.graph.rel.ProjectRelationship;
import org.commonjava.maven.atlas.graph.rel.RelationshipType;
import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.atlas.ident.ref.SimpleProjectVersionRef;
import org.jboss.da.common.json.DAConfig;
import org.jboss.da.common.util.Configuration;
import org.jboss.da.common.util.ConfigurationParseException;
import org.jboss.da.communication.CommunicationException;
import org.jboss.da.communication.aprox.api.AproxConnector;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;
import org.jboss.da.communication.aprox.model.VersionResponse;
import org.jboss.da.communication.model.GA;
import org.jboss.da.communication.model.GAV;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.communication.pom.PomAnalyzer;
import org.jboss.da.scm.SCM;
import org.jboss.da.scm.SCMType;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class AproxConnectorImpl implements AproxConnector {

    @Inject
    private Configuration config;

    @Inject
    private SCM scmManager;

    @Inject
    private PomAnalyzer pomAnalyzer;

    @Override
    public Optional<GAVDependencyTree> getDependencyTreeOfRevision(String scmUrl, String revision,
            String pomPath) throws ScmException, PomAnalysisException {

        try {
            // git clone
            // TODO: hardcoded to git right now
            File tempDir = Files.createTempDirectory("cloned_repo").toFile();

            try {
                scmManager.cloneRepository(SCMType.GIT, scmUrl, revision, tempDir.toString());

                GAVDependencyTree gavDependencyTree = pomAnalyzer.readRelationships(tempDir,
                        new File(tempDir, pomPath));

                return Optional.ofNullable(gavDependencyTree);
            } finally {
                // cleanup
                FileUtils.deleteDirectory(tempDir);
            }
        } catch (IOException e) {
            throw new ScmException("Could not create temp directory for cloning the repository", e);
        }
    }

    @Override
    public Optional<GAVDependencyTree> getDependencyTreeOfGAV(GAV gav)
            throws CommunicationException {

        DepgraphAproxClientModule mod = new DepgraphAproxClientModule();
        try (Aprox aprox = new Aprox(config.getConfig().getAproxServer() + "/api", mod).connect()) {

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

            if (export == null || export.getRelationships() == null) {
                return Optional.empty();
            }
            return Optional.of(generateGAVDependencyTree(export, gav));
        } catch (AproxClientException | ConfigurationParseException e) {
            throw new CommunicationException(e);
        }
    }

    @Override
    public List<String> getVersionsOfGA(GA ga) throws CommunicationException {
        StringBuilder query = new StringBuilder();
        try {
            DAConfig config = this.config.getConfig();
            query.append(config.getAproxServer());
            query.append("/api/remote/");
            query.append(config.getAproxRemote()).append('/');
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

    private GAVDependencyTree addGAVDependencyTreeToGAVMapper(Map<GAV, GAVDependencyTree> gavMapper, GAV gav) {
        return gavMapper.computeIfAbsent(gav, k -> new GAVDependencyTree(k));
    }

    private GAV generateGAV(ProjectVersionRef ref) {
        return new GAV(ref.getGroupId(), ref.getArtifactId(), ref.getVersionString());
    }
}
