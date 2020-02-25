package org.jboss.da.test;

import org.jboss.arquillian.container.test.api.Testable;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Assignable;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.importer.ArchiveImportException;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class ArquillianDeploymentFactory {

    public enum DepType {
        REPORTS
    }

    public enum TestSide {
        CLIENT, SERVER
    }

    public static final String DEPLOYMENT_NAME = "testsuite";

    private static final String TEST_JAR = "testsuite.jar";

    private static final String TEST_EAR = DEPLOYMENT_NAME + ".ear";

    public EnterpriseArchive createDeployment(DepType type, TestSide testSide) {
        File earProjectBuildDir = new File(new File(getProjectTopLevelDir(), "application"), "target");
        File earFile = new File(earProjectBuildDir, "dependency-analysis.ear");
        EnterpriseArchive ear = createFromZipFile(EnterpriseArchive.class, earFile, TEST_EAR);
        updateEar(ear, type);
        switch (testSide) {
            case SERVER:
                updateEarServer(ear);
        }
        if (isCreateArchiveCopy()) {
            writeArchiveToFile(ear, new File("target", ear.getName()));
        }
        return ear;
    }

    private void updateEarServer(EnterpriseArchive ear) {
        File wiremockJar = Maven.resolver()
                .loadPomFromFile("pom.xml")
                .resolve("com.github.tomakehurst:wiremock-standalone")
                .withoutTransitivity()
                .asSingleFile();
        ear.addAsLibraries(wiremockJar);
    }

    private void updateEar(EnterpriseArchive ear, DepType type) {
        updateManifestResource(ear, "persistence.xml");
        updateManifestResource(ear, "application.xml");
        updateManifestResource(ear, "jboss-deployment-structure.xml");
        JavaArchive communicationJar = ear.getAsType(JavaArchive.class, "communication.jar");
        JavaArchive reportsBackendJar = ear.getAsType(JavaArchive.class, "reports-backend.jar");
        WebArchive reportsRestWar;
        switch (type) {
            case REPORTS: {
                reportsRestWar = Testable.archiveToTest(ear.getAsType(WebArchive.class, "reports-rest.war"));
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown dependency type " + type);
        }
        updateArchiveWithReplacements(communicationJar, "communication");
        updateArchiveWithReplacements(reportsBackendJar, "reports-backend");
        updateArchiveWithReplacements(reportsRestWar, "reports-rest");
        ear.addAsModule(createTestsuiteJar());
    }

    private void updateManifestResource(EnterpriseArchive ear, String resource) {
        ear.delete("META-INF/" + resource);
        ear.addAsManifestResource(new File("src/test/resources/META-INF/" + resource));
    }

    private void updateArchiveWithReplacements(Archive archive, String name) {
        try {
            Path replacements = Paths.get("src/test/replacements", name).toAbsolutePath();
            Files.find(replacements, Integer.MAX_VALUE, (p, a) -> a.isRegularFile()).forEach(p -> {
                String relpath = replacements.relativize(p).toString();
                archive.delete(relpath);
                archive.add(new FileAsset(p.toFile()), relpath);
            });
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private JavaArchive createTestsuiteJar() {
        JavaArchive testsuiteJar = ShrinkWrap.create(JavaArchive.class, TEST_JAR);
        testsuiteJar.addPackages(true, "org.jboss.da.test.server");
        testsuiteJar.addAsManifestResource(new File("src/test/resources/META-INF/beans.xml"));
        return testsuiteJar;
    }

    private File getProjectTopLevelDir() {
        File projectTopLevelDir = new File("").getAbsoluteFile();
        if (!isProjectTopLevelDir(projectTopLevelDir)) {
            projectTopLevelDir = projectTopLevelDir.getParentFile();
            if (!isProjectTopLevelDir(projectTopLevelDir)) {
                throw new IllegalStateException(
                        "Can not find project top level directory from " + projectTopLevelDir.getAbsolutePath());
            }
        }
        return projectTopLevelDir;
    }

    private boolean isProjectTopLevelDir(File dir) {
        File testModule = dir == null ? null : new File(dir, "testsuite");
        return testModule != null && testModule.exists() && testModule.isDirectory();
    }

    private boolean isCreateArchiveCopy() {
        return System.getProperty("createArchiveCopy") != null;
    }

    private void writeArchiveToFile(Archive<?> archive, File file) {
        archive.as(ZipExporter.class).exportTo(file, true);
    }

    // copied from org.jboss.shrinkwrap.api.ArchiveFactory.createFromZipFile(final Class<T> type, final File
    // archiveFile) -
    // added parameter archiveName
    /**
     * Creates a new archive of the specified type as imported from the specified {@link File}. The file is expected to
     * be encoded as ZIP (ie. JAR/WAR/EAR). The name of the archive will be set to {@link File#getName()}. The archive
     * will be be backed by the {@link org.jboss.shrinkwrap.api.Configuration} specific to this
     * {@link org.jboss.shrinkwrap.api.ArchiveFactory}.
     *
     * @param type The type of the archive e.g. {@link org.jboss.shrinkwrap.api.spec.WebArchive}
     * @param archiveFile the archiveFile to use
     * @param archiveName the name of created archive
     * @return An {@link Assignable} view
     * @throws IllegalArgumentException If either argument is not supplied, if the specified {@link File} does not
     *         exist, or is not a valid ZIP file
     * @throws org.jboss.shrinkwrap.api.importer.ArchiveImportException If an error occurred during the import process
     */
    public <T extends Assignable> T createFromZipFile(final Class<T> type, final File archiveFile, String archiveName)
            throws IllegalArgumentException, ArchiveImportException {
        // Precondition checks
        if (type == null) {
            throw new IllegalArgumentException("Type must be specified");
        }
        if (archiveFile == null) {
            throw new IllegalArgumentException("File must be specified");
        }
        if (!archiveFile.exists()) {
            throw new IllegalArgumentException("File for import does not exist: " + archiveFile.getAbsolutePath());
        }
        if (archiveFile.isDirectory()) {
            throw new IllegalArgumentException(
                    "File for import must not be a directory: " + archiveFile.getAbsolutePath());
        }

        // Construct ZipFile
        final ZipFile zipFile;
        try {
            zipFile = new ZipFile(archiveFile);
        } catch (final ZipException ze) {
            throw new IllegalArgumentException(
                    "Does not appear to be a valid ZIP file: " + archiveFile.getAbsolutePath());
        } catch (final IOException ioe) {
            throw new RuntimeException(
                    "I/O Error in importing new archive from ZIP: " + archiveFile.getAbsolutePath(),
                    ioe);
        }

        // Import
        return ShrinkWrap.create(type, archiveName).as(ZipImporter.class).importFrom(zipFile).as(type);

    }
}
