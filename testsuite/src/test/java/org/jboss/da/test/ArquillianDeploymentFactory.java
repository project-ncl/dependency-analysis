package org.jboss.da.test;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenStrategyStage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArquillianDeploymentFactory {

    public static final String DEPLOYMENT_NAME = "testsuite";

    private static final String TEST_JAR = "testsuite.jar";

    private static final String TEST_EAR = DEPLOYMENT_NAME + ".ear";

    private static final String COMMUNICATION_PLACEHOLDER = "${communication}";

    private static final String COMMON_PLACEHOLDER = "${common}";

    private static final String BC_BCAKEND_PLACEHOLDER = "${bc-backend}";

    private static final String REPORTS_BACKEND_PLACEHOLDER = "${reports-backend}";

    private static final String BC_REST_PLACEHOLDER = "${bc-rest}";

    private static final String REPORTS_REST_PLACEHOLDER = "${reports-rest}";

    private static final String TEST_PLACEHOLDER = "${testsuite}";

    private static final String TYPE_WAR = "war";

    private static final String TYPE_EJB = "ejb";

    private static final String PROJECT_GROUP_ID = "org.jboss.da";

    private final boolean useBuildArchives;

    public ArquillianDeploymentFactory() {
        this.useBuildArchives = System.getProperty("useBuildArchives") != null;
    }

    private MavenStrategyStage mavenResolve(String groupId, String artifactId, String type) {
        return Maven.resolver().loadPomFromFile("../application/pom.xml")
                .resolve(groupId + ":" + artifactId + ":" + type + ":?");
    }

    private JavaArchive getModule(String groupId, String artifactId) {
        return getArchive(groupId, artifactId, "ejb", JavaArchive.class);
    }

    private WebArchive getWebModule(String groupId, String artifactId) {
        return getArchive(groupId, artifactId, "war", WebArchive.class);
    }

    private <T extends Archive> T getArchive(String groupId, String artifactId, String type,
            Class<T> archiveClass) {
        File file = useBuildArchives ? findBuildArchive(groupId, artifactId, type) : mavenResolve(
                groupId, artifactId, type).withoutTransitivity().asSingleFile();
        return ShrinkWrap.createFromZipFile(archiveClass, file);
    }

    private File findBuildArchive(String groupId, String artifactId, final String type) {
        if (PROJECT_GROUP_ID.equals(groupId)) {
            File projectTopLevelDir = new File("").getAbsoluteFile();
            if (!isProjectTopLevelDir(projectTopLevelDir)) {
                projectTopLevelDir = projectTopLevelDir.getParentFile();
                if (!isProjectTopLevelDir(projectTopLevelDir)) {
                    throw new IllegalStateException(
                            "Can not find project top level directory from "
                                    + projectTopLevelDir.getAbsolutePath());
                }
            }
            File projectBuildDir = new File(new File(projectTopLevelDir, artifactId), "target");
            try {
                DirectoryStream.Filter<? super Path> filter = path -> {
                    File file = path.toFile();
                    String name = file.getName();
                    return file.isFile() && name.startsWith(artifactId)
                            && name.endsWith("war".equals(type) ? ".war" : ".jar");
                };
                DirectoryStream<Path> directoryStream = Files.newDirectoryStream(
                        Paths.get(projectBuildDir.getAbsolutePath()), filter);
                Iterator<Path> iterator = directoryStream.iterator();
                return iterator.hasNext() ? iterator.next().toFile() : null;
            } catch (IOException e) {
                throw new RuntimeException("Can not find archive " + groupId + ":" + artifactId
                        + " - error " + e.getMessage(), e);
            }
        }
        throw new UnsupportedOperationException("Archive " + groupId + ":" + artifactId
                + " is not part of this project");
    }

    private boolean isProjectTopLevelDir(File dir) {
        File testModule = dir == null ? null : new File(dir, "testsuite");
        return testModule != null && testModule.exists() && testModule.isDirectory();
    }

    /**
     * This method reads jboss deployment structure and replace placeholders with actual file names of module jars. It return the
     * deployment structure in string. This is needed because deployment structure needs actual file names in .ear and the
     * filenames can vary system to system and even between runs.
     */
    private String prepareDeploymentStructure(String communicationName, String bcBackendName,
            String reportsBackendName, String bcRestName, String reportsRestName,
            String commonJarName) {
        File f = new File("src/test/resources/META-INF/jboss-deployment-structure.xml");
        return replacePlaceholders(f, communicationName, bcBackendName, reportsBackendName,
                bcRestName, reportsRestName, commonJarName);
    }

    private String replacePlaceholders(File f, String communicationName, String bcBackendName,
            String reportsBackendName, String bcRestName, String reportsRestName, String commonName) {
        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
            StringBuilder sb = new StringBuilder();
            for (String line = r.readLine(); line != null; line = r.readLine()) {
                line = line.replace(COMMUNICATION_PLACEHOLDER, communicationName);
                line = line.replace(COMMON_PLACEHOLDER, commonName);
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
            String reportsBackendName, String bcRestName, String reportsRestName, String commonName) {
        File f = new File("src/test/resources/META-INF/application.xml");
        return replacePlaceholders(f, communicationName, bcBackendName, reportsBackendName,
                bcRestName, reportsRestName, commonName);
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
                if (!(useBuildArchives && f.getParent().startsWith("/tmp"))) {
                    libsList.add(f);
                }
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
        JavaArchive communicationJar = getModule(PROJECT_GROUP_ID, "communication");
        JavaArchive bcBackendJar = getModule(PROJECT_GROUP_ID, "bc-backend");
        JavaArchive reportsBackendJar = getModule(PROJECT_GROUP_ID, "reports-backend");
        JavaArchive bcRestJar = getModule(PROJECT_GROUP_ID, "bc-rest");
        JavaArchive commonJar = getModule(PROJECT_GROUP_ID, "common");
        WebArchive reportsRestWar = getWebModule(PROJECT_GROUP_ID, "reports-rest");
        updateRestWarWithReplacements(reportsRestWar);

        JavaArchive testsuiteJar = prepareTestsuiteJar();

        String depStruct = prepareDeploymentStructure(communicationJar.getName(),
                bcBackendJar.getName(), reportsBackendJar.getName(), bcRestJar.getName(),
                reportsRestWar.getName(), commonJar.getName());

        StringAsset deploymentStructure = new StringAsset(depStruct);

        EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, TEST_EAR);
        ear.addAsLibraries(getLibs(PROJECT_GROUP_ID, "communication", TYPE_EJB));
        // ear.addAsLibraries(getLibs(PROJECT_GROUP_ID, "bc-backend", TYPE_EJB));
        // ear.addAsLibraries(getLibs(PROJECT_GROUP_ID, "reports-backend", TYPE_EJB));
        // ear.addAsLibraries(getLibs(PROJECT_GROUP_ID, "bc-rest", TYPE_EJB));
        // ear.addAsLibraries(getLibs(PROJECT_GROUP_ID, "reports-rest", TYPE_WAR));
        ear.addAsModule(communicationJar);
        ear.addAsModule(bcBackendJar);
        ear.addAsModule(reportsBackendJar);
        ear.addAsModule(bcRestJar);
        ear.addAsModule(reportsRestWar);
        ear.addAsModule(commonJar);
        ear.addAsModule(testsuiteJar);
        ear.addAsManifestResource(new File("src/test/resources/META-INF/persistence.xml"));
        ear.setApplicationXML(new StringAsset(prepareApplicationXml(communicationJar.getName(),
                bcBackendJar.getName(), reportsBackendJar.getName(), bcRestJar.getName(),
                reportsRestWar.getName(), commonJar.getName())));
        ear.addAsManifestResource(deploymentStructure, "jboss-deployment-structure.xml");

        if (isCreateArchiveCopy()) {
            writeArchiveToFile(ear, new File(ear.getName()));
        }
        return ear;
    }

    private void updateRestWarWithReplacements(WebArchive reportsRestWar) {
        reportsRestWar.delete("WEB-INF/web.xml");
        reportsRestWar.addAsWebInfResource(new File(
                "src/test/replacements/reports-rest/webapp/WEB-INF/web.xml"));
    }

    private boolean isCreateArchiveCopy() {
        return System.getProperty("createArchiveCopy") != null;
    }

    private void writeArchiveToFile(Archive<?> archive, File file) {
        archive.as(ZipExporter.class).exportTo(file, true);
    }

}
