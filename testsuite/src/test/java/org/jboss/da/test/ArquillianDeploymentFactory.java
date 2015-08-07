package org.jboss.da.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenStrategyStage;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
public class ArquillianDeploymentFactory {

    private static final String TEST_JAR = "testsuite.jar";

    private static final String TEST_EAR = "testsuite.ear";

    private static final String COMMUNICATION_PLACEHOLDER = "${communication}";

    private static final String BC_BCAKEND_PLACEHOLDER = "${bc-backend}";

    private static final String REPORTS_BACKEND_PLACEHOLDER = "${reports-backend}";

    private static final String BC_REST_PLACEHOLDER = "${bc-rest}";

    private static final String REPORTS_REST_PLACEHOLDER = "${reports-rest}";

    private static final String TEST_PLACEHOLDER = "${testsuite}";

    private static final String TYPE_WAR = "war";

    private static final String TYPE_EJB = "ejb";

    private MavenStrategyStage mavenResolve(String groupId, String artifactId, String type) {
        return Maven.resolver().loadPomFromFile("../application/pom.xml")
                .resolve(groupId + ":" + artifactId + ":" + type + ":?");
    }

    private JavaArchive getModule(String groupId, String artifactId) {
        File file = mavenResolve(groupId, artifactId, "ejb").withoutTransitivity().asSingleFile();
        return ShrinkWrap.createFromZipFile(JavaArchive.class, file);
    }

    private WebArchive getWebModule(String groupId, String artifactId) {
        File file = mavenResolve(groupId, artifactId, "war").withoutTransitivity().asSingleFile();
        return ShrinkWrap.createFromZipFile(WebArchive.class, file);
    }

    /**
     * This method reads jboss deployment structure and replace placeholders with actual file names of module jars. It return the
     * deployment structure in string. This is needed because deployment structure needs actual file names in .ear and the
     * filenames can vary system to system and even between runs.
     */
    private String prepareDeploymentStructure(String communicationName, String bcBackendName,
            String reportsBackendName, String bcRestName, String reportsRestName) {
        File f = new File("src/test/resources/META-INF/jboss-deployment-structure.xml");
        return replacePlaceholders(f, communicationName, bcBackendName, reportsBackendName,
                bcRestName, reportsRestName);
    }

    private String replacePlaceholders(File f, String communicationName, String bcBackendName,
            String reportsBackendName, String bcRestName, String reportsRestName) {
        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
            StringBuilder sb = new StringBuilder();
            for (String line = r.readLine(); line != null; line = r.readLine()) {
                line = line.replace(COMMUNICATION_PLACEHOLDER, communicationName);
                line = line.replace(BC_BCAKEND_PLACEHOLDER, bcBackendName);
                line = line.replace(REPORTS_BACKEND_PLACEHOLDER, reportsBackendName);
                line = line.replace(BC_REST_PLACEHOLDER, bcRestName);
                line = line.replace(REPORTS_REST_PLACEHOLDER, reportsRestName);
                line = line.replace(TEST_PLACEHOLDER, TEST_JAR);
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private String prepareApplicationXml(String communicationName, String bcBackendName,
            String reportsBackendName, String bcRestName, String reportsRestName) {
        File f = new File("src/test/resources/META-INF/application.xml");
        return replacePlaceholders(f, communicationName, bcBackendName, reportsBackendName,
                bcRestName, reportsRestName);
    }

    /**
     * Regex pattern for file name
     */
    private final static Pattern fileNamePattern = Pattern.compile("(^[a-z-]+[a-z])");

    private File[] getLibs(String groupId, String artifactId, String type) {
        File[] libs = mavenResolve(groupId, artifactId, type).withTransitivity().asFile();

        List<File> libsList = new ArrayList<>();
        for (File f : libs) {
            Matcher matcher = fileNamePattern.matcher(f.getName());
            if (matcher.find() && !matcher.group(1).equals(artifactId)) {
                libsList.add(f);
            }
        }
        return libsList.toArray(new File[libsList.size()]);
    }

    private JavaArchive prepareTestsuiteJar() {
        JavaArchive testsuiteJar = ShrinkWrap.create(JavaArchive.class, TEST_JAR);
        testsuiteJar.addPackages(true, "org.jboss.da.test.server");
        testsuiteJar.addAsManifestResource(new File("src/test/resources/META-INF/beans.xml"));
        return testsuiteJar;
    }

    public EnterpriseArchive createDeployment() {
        JavaArchive communicationJar = getModule("org.jboss.da", "communication");
        JavaArchive bcBackendJar = getModule("org.jboss.da", "bc-backend");
        JavaArchive reportsBackendJar = getModule("org.jboss.da", "reports-backend");
        JavaArchive bcRestJar = getModule("org.jboss.da", "bc-rest");
        WebArchive reportsRestJar = getWebModule("org.jboss.da", "reports-rest");

        JavaArchive testsuiteJar = prepareTestsuiteJar();

        String depStruct = prepareDeploymentStructure(communicationJar.getName(),
                bcBackendJar.getName(), reportsBackendJar.getName(), bcRestJar.getName(),
                reportsRestJar.getName());

        StringAsset deploymentStructure = new StringAsset(depStruct);

        EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, TEST_EAR);
        ear.addAsLibraries(getLibs("org.jboss.da", "communication", TYPE_EJB));
        ear.addAsLibraries(getLibs("org.jboss.da", "bc-backend", TYPE_EJB));
        ear.addAsLibraries(getLibs("org.jboss.da", "reports-backend", TYPE_EJB));
        ear.addAsLibraries(getLibs("org.jboss.da", "bc-rest", TYPE_EJB));
        ear.addAsLibraries(getLibs("org.jboss.da", "reports-rest", TYPE_WAR));
        ear.addAsModule(testsuiteJar);
        ear.addAsModule(communicationJar);
        ear.addAsModule(bcBackendJar);
        ear.addAsModule(reportsBackendJar);
        ear.addAsModule(bcRestJar);
        ear.addAsModule(reportsRestJar);
        ear.addAsManifestResource(new File("src/test/resources/META-INF/persistence.xml"));
        ear.setApplicationXML(new StringAsset(prepareApplicationXml(communicationJar.getName(),
                bcBackendJar.getName(), reportsBackendJar.getName(), bcRestJar.getName(),
                reportsRestJar.getName())));
        ear.addAsManifestResource(deploymentStructure, "jboss-deployment-structure.xml");

        return ear;
    }

}