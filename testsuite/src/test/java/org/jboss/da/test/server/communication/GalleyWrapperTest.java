package org.jboss.da.test.server.communication;

import org.apache.maven.scm.ScmException;
import org.commonjava.cartographer.CartoDataException;
import org.commonjava.cartographer.CartographerCore;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.da.common.util.Configuration;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;
import org.jboss.da.communication.pom.GalleyWrapper;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.communication.pom.PomReader;
import org.jboss.da.communication.pom.api.PomAnalyzer;
import org.jboss.da.communication.pom.qualifier.DACartographerCore;
import org.jboss.da.model.rest.GAV;
import org.jboss.da.scm.api.SCM;
import org.jboss.da.scm.api.SCMType;
import org.jboss.da.test.ArquillianDeploymentFactory;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
@RunWith(Arquillian.class)
public class GalleyWrapperTest {

    private static final String VERSION = "0.5.1";

    private static final GAV PARENT_GAV = new GAV("org.jboss.da", "parent", VERSION);

    private static final GAV APPLICATION_GAV = new GAV("org.jboss.da", "application", VERSION);

    private static final GAV TESTSUITE_GAV = new GAV("org.jboss.da", "testsuite", VERSION);

    @Inject
    private SCM scm;

    @Inject
    @DACartographerCore
    private CartographerCore carto;

    @Inject
    private PomReader pomReader;

    @Inject
    private PomAnalyzer pomAnalyzer;

    @Inject
    private Configuration config;

    private File clonedRepository;

    @Deployment
    public static EnterpriseArchive createDeployment() {
        return new ArquillianDeploymentFactory()
                .createDeployment(ArquillianDeploymentFactory.DepType.REPORTS);
    }

    @Before
    public void cloneRepo() throws ScmException {
        clonedRepository = scm.cloneRepository(SCMType.GIT,
                "https://github.com/project-ncl/dependency-analysis.git", VERSION);
    }

    private void printDeptree(GAVDependencyTree tree, String prefix) {
        System.out.println(prefix + tree.getGav());
        String np = prefix + "   ";
        for (GAVDependencyTree d : tree.getDependencies()) {
            printDeptree(d, np);
        }
    }

    @Test
    public void testReadRelationships() throws IOException, PomAnalysisException,
            GalleyMavenException, CartoDataException {
        long start = System.nanoTime();
        GAVDependencyTree readRelationships = pomAnalyzer.readRelationships(clonedRepository,
                "application/pom.xml", Collections.emptyList());
        float time = (System.nanoTime() - start) / 1000000.0f;

        System.out.println("Dependency tree:");
        printDeptree(readRelationships, "");
        System.out.println("Took: " + time);
    }

    @Test
    public void testGetPom() throws IOException, PomAnalysisException {
        try (GalleyWrapper gw = new GalleyWrapper(carto.getGalley(), clonedRepository)) {
            GalleyWrapper.Artifact parent = gw.getPom("pom.xml");

            assertEquals(PARENT_GAV, parent.getGAV());

            GalleyWrapper.Artifact application = gw.getPom("application/pom.xml");

            assertEquals(APPLICATION_GAV, application.getGAV());
        }
    }

    @Test
    public void testGetModules() throws IOException, PomAnalysisException {
        try (GalleyWrapper gw = new GalleyWrapper(carto.getGalley(), clonedRepository)) {
            GalleyWrapper.Artifact parent = gw.getPom("pom.xml");

            Set<GalleyWrapper.Artifact> modules = gw.getModules(parent);
            assertEquals(9, modules.size());

            Set<GAV> moduleGavs = new HashSet<>();

            for (GalleyWrapper.Artifact module : modules) {
                moduleGavs.add(module.getGAV());
            }

            assertTrue(moduleGavs.contains(APPLICATION_GAV));
            assertTrue(moduleGavs.contains(TESTSUITE_GAV));
            assertFalse(moduleGavs.contains(PARENT_GAV));

            Set<GalleyWrapper.Artifact> allModules = gw.getAllModules(parent);
            assertEquals(10, allModules.size());

            Set<GAV> allModuleGavs = new HashSet<>();

            for (GalleyWrapper.Artifact module : allModules) {
                allModuleGavs.add(module.getGAV());
            }

            assertTrue(allModuleGavs.contains(APPLICATION_GAV));
            assertTrue(allModuleGavs.contains(TESTSUITE_GAV));
            assertTrue(allModuleGavs.contains(PARENT_GAV));
        }
    }

    @Test
    public void testGetDependencies() throws IOException, PomAnalysisException {
        try (GalleyWrapper gw = new GalleyWrapper(carto.getGalley(), clonedRepository)) {
            gw.addDefaultLocations(config);
            gw.addLocationsFromPoms(pomReader);

            GalleyWrapper.Artifact common = gw.getPom("common/pom.xml");

            Set<GAV> dependencies = gw.getDependencies(common);
            assertEquals(8, dependencies.size());

            assertTrue(dependencies.contains(new GAV("org.jboss.resteasy", "resteasy-jaxrs",
                    "2.3.10.Final")));
            assertTrue(dependencies.contains(new GAV("org.codehaus.jackson", "jackson-mapper-asl",
                    "1.9.9")));
            assertTrue(dependencies.contains(new GAV("org.projectlombok", "lombok", "1.16.4")));
            assertTrue(dependencies.contains(new GAV("junit", "junit", "4.11")));
        }
    }
}
