package org.jboss.da.communication.aprox.impl;

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

import javax.enterprise.context.ApplicationScoped;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class AproxConnectorImpl implements AproxConnector {

    private final Configuration config = new Configuration();

    @Override
    public Optional<GAVDependencyTree> getDependencyTreeOfRevision(String scmUrl, String revision,
            String pomPath) {
        // TODO Auto-generated method stub
        return Optional.empty();
    }

    @Override
    public Optional<GAVDependencyTree> getDependencyTreeOfGAV(GAV gav)
            throws CommunicationException {

        DepgraphAproxClientModule mod = new DepgraphAproxClientModule();
        try (Aprox aprox = new Aprox(config.getConfig().getAproxServer() + "/api", mod).connect()) {

            ProjectGraphRequest req = mod
                    .newProjectGraphRequest()
                    .withWorkspaceId("graph-export")
                    .withSource("group:public")
                    .withPatcherIds(DepgraphPatcherConstants.ALL_PATCHERS)
                    .withResolve(true)
                    .withGraph(
                            mod.newGraphDescription()
                                    .withRoots(
                                            new SimpleProjectVersionRef(gav.getGroupId(), gav
                                                    .getArtifactId(), gav.getVersion()))
                                    .withPreset("requires").build()).build();

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
