package org.jboss.da.communication.cartographer.impl;

import org.commonjava.cartographer.client.CartoClientException;
import org.commonjava.cartographer.client.CartographerRESTClient;
import org.commonjava.cartographer.graph.discover.patch.DepgraphPatcherConstants;
import org.commonjava.cartographer.request.ProjectGraphRequest;
import org.commonjava.cartographer.request.SingleGraphRequest;
import org.commonjava.cartographer.result.GraphExport;
import org.commonjava.maven.atlas.graph.rel.ProjectRelationship;
import org.commonjava.maven.atlas.graph.rel.RelationshipType;
import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.atlas.ident.ref.SimpleProjectVersionRef;
import org.commonjava.propulsor.client.http.ClientHttpException;
import org.commonjava.util.jhttpc.auth.MemoryPasswordManager;
import org.commonjava.util.jhttpc.auth.PasswordManager;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.common.util.Configuration;
import org.jboss.da.common.util.ConfigurationParseException;
import org.jboss.da.communication.aprox.FindGAVDependencyException;
import org.jboss.da.communication.aprox.api.AproxConnector;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;
import org.jboss.da.communication.cartographer.api.CartographerConnector;
import org.jboss.da.model.rest.GAV;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class CartographerConnectorImpl implements CartographerConnector {

    @Inject
    private Logger log;

    @Inject
    private Configuration config;

    @Inject
    private AproxConnector aproxConnector;

    private CartographerRESTClient cartographer;

    @PostConstruct
    private void postConstruct() {
        try {
            PasswordManager emptyPasswordManager = new MemoryPasswordManager();
            cartographer = new CartographerRESTClient(
                    config.getConfig().getCartographerServerUrl(), emptyPasswordManager);
        } catch (ConfigurationParseException | ClientHttpException e) {
            throw new IllegalStateException("Could not initialize Cartographer Client.", e);
        }
    }

    @Override
    public GAVDependencyTree getDependencyTreeOfGAV(GAV gav) throws CommunicationException,
            FindGAVDependencyException {

        if (!aproxConnector.doesGAVExistInPublicRepo(gav)) {
            throw new FindGAVDependencyException("Could not find: " + gav
                    + " in public repo of Aprox");
        }

        try {

            SimpleProjectVersionRef rootRef = new SimpleProjectVersionRef(gav.getGroupId(),
                    gav.getArtifactId(), gav.getVersion());

            SingleGraphRequest r = new SingleGraphRequest();
            r.setWorkspaceId("export-" + rootRef.toString());
            r.setSource("group:" + config.getConfig().getAproxGroupPublic());
            r.setPatcherIds(DepgraphPatcherConstants.ALL_PATCHERS);
            r.setResolve(true);
            r.setGraph(cartographer.newGraphDescription().withRoots(rootRef).withPreset("requires")
                    .build());

            GraphExport export = cartographer.graph(r);

            if (export == null) {
                log.warn("Analysis of the Dependency Tree of GAV: " + gav + " failed!");
                return new GAVDependencyTree(gav);
            }

            if (export.getRelationships() == null)
                return new GAVDependencyTree(gav);
            else
                return generateGAVDependencyTree(export, gav);

        } catch (ClientHttpException | ConfigurationParseException | CartoClientException e) {
            throw new CommunicationException("Error while trying to communicate with Cartographer",
                    e);
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

    @PreDestroy
    private void preDestroy() {
        try {
            cartographer.close();
        } catch (Exception e) {
            throw new IllegalStateException("Could not safely close Cartographer Client.", e);
        }
    }
}
