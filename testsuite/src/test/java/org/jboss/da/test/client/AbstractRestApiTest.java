package org.jboss.da.test.client;

import org.apache.http.entity.ContentType;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.da.test.ArquillianDeploymentFactory;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.runner.RunWith;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.jboss.da.common.version.Constants.REST_API_VERSION;
import static org.jboss.da.test.ArquillianDeploymentFactory.DEPLOYMENT_NAME;

@RunWith(Arquillian.class)
@RunAsClient
public abstract class AbstractRestApiTest {

    private static final String DEFAULT_REST_API_VERSION = "v-" + REST_API_VERSION;

    protected final String hostUrl;

    protected final String restApiURL;

    protected final String restApiVersion;

    protected final Path restApiRequestFolder;

    protected final Path restApiExpectedResponseFolder;

    private String defaultRestApiVersionUrlPart;

    @Deployment
    public static EnterpriseArchive createDeployment() {
        return new ArquillianDeploymentFactory().createDeployment();
    }

    public AbstractRestApiTest() {
        this.hostUrl = readHostUrl();
        this.restApiVersion = readRestApiVersion();
        this.restApiURL = readRestApiUrl();
        this.restApiRequestFolder = readRestApiRequestFolder();
        this.restApiExpectedResponseFolder = readRestApiExpectedResponseFolder();
    }

    private Path readRestApiRequestFolder() {
        String versionFolder = convertRestApiVersionToFolderName();
        return Paths.get(readConfigurationValue("testsuite.restApiRequestFolder", "src/test/rest"
                + (versionFolder == null ? "" : "/" + versionFolder) + "/request"));
    }

    private Path readRestApiExpectedResponseFolder() {
        String versionFolder = convertRestApiVersionToFolderName();
        return Paths.get(readConfigurationValue("testsuite.restApiExpectedResponseFolder",
                "src/test/rest" + (versionFolder == null ? "" : "/" + versionFolder)
                        + "/expectedResponse/"));
    }

    protected String readConfigurationValue(String name, String defaultValue) {
        String value = readConfigurationValue(name);
        return value == null || "".equals(value.trim()) ? defaultValue : value;
    }

    protected String readConfigurationValue(String name) {
        return System.getProperty(name);
    }

    private String readHostUrl() {
        return readConfigurationValue("testsuite.hostUrl", "http://localhost:8180");
    }

    private String readRestApiVersion() {
        return readConfigurationValue("testsuite.restApiVersion", DEFAULT_REST_API_VERSION);
    }

    private String readRestApiUrl() {
        return readConfigurationValue("testsuite.restApiUrl", hostUrl + "/" + DEPLOYMENT_NAME
                + "/rest" + (restApiVersion == null ? "" : "/" + restApiVersion));
    }

    private String convertRestApiVersionToFolderName() {
        if (DEFAULT_REST_API_VERSION == null)
            return null;
        if (DEFAULT_REST_API_VERSION.startsWith("v-0"))
            return "v-1.0";
        return DEFAULT_REST_API_VERSION;
    }

    // TODO convert to builder pattern appropriatelly
    protected static class ExpectedResponseFilenameBuilder {

        protected static final String DEFAULT_VARIANT = "";

        private final Path folder;

        private final String path;

        private final String variant;

        private final ContentType contentType;

        public ExpectedResponseFilenameBuilder(Path folder, String path) {
            this(folder, path, ContentType.APPLICATION_JSON);

        }

        public ExpectedResponseFilenameBuilder(Path folder, String path, ContentType contentType) {
            this(folder, path, contentType, DEFAULT_VARIANT);
        }

        public ExpectedResponseFilenameBuilder(Path folder, String path, ContentType contentType,
                String variant) {
            this.folder = folder;
            this.path = path;
            this.contentType = contentType;
            this.variant = variant;
        }

        public File getFile() {
            return getPath().toFile();
        }

        public Path getPath() {
            return folder.resolve(convertPath(path) + convertVariant(variant)
                    + convertContentType(contentType));
        }

        private String convertVariant(String path) {
            return variant.equals(DEFAULT_VARIANT) ? "" : "/" + variant;
        }

        private String convertPath(String path) {
            String convertedPath = path.endsWith("/") ? path.substring(0, path.length() - 1)
                    + "index" : path;
            return convertedPath.startsWith("/") ? convertedPath.substring(1) : convertedPath;
        }

        private String convertContentType(ContentType contentType) {
            if ("text/html".equals(contentType.getMimeType()))
                return ".html";
            return ".json";
        }

    }

    // TODO convert to builder pattern appropriatelly
    protected static class RequestFilenameBuilder {

        protected static final String DEFAULT_VARIANT = "";

        private final Path folder;

        private final String path;

        private final String variant;

        private final ContentType contentType;

        public RequestFilenameBuilder(Path folder, String path) {
            this(folder, path, ContentType.APPLICATION_JSON);

        }

        public RequestFilenameBuilder(Path folder, String path, ContentType contentType) {
            this(folder, path, contentType, DEFAULT_VARIANT);
        }

        public RequestFilenameBuilder(Path folder, String path, ContentType contentType,
                String variant) {
            this.folder = folder;
            this.path = path;
            this.contentType = contentType;
            this.variant = variant;
        }

        public File getFile() {
            return getPath().toFile();
        }

        public Path getPath() {
            return folder.resolve(convertPath(path) + convertVariant(variant)
                    + convertContentType(contentType));
        }

        private String convertVariant(String path) {
            return variant.equals(DEFAULT_VARIANT) ? "" : "/" + variant;
        }

        private String convertPath(String path) {
            String convertedPath = path.endsWith("/") ? path.substring(0, path.length() - 1)
                    + "index" : path;
            return convertedPath.startsWith("/") ? convertedPath.substring(1) : convertedPath;
        }

        private String convertContentType(ContentType contentType) {
            if ("text/html".equals(contentType.getMimeType()))
                return ".html";
            return ".json";
        }

    }
}
