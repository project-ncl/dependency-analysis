package org.jboss.da.test.client;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.http.entity.ContentType;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

public abstract class AbstractClientApiTest {

    @RegisterExtension
    public static final WireMockExtension wireMockRule = WireMockExtension.newInstance()
            .options(options().port(8081).usingFilesUnderDirectory("src/test/resources/wiremock"))
            .build();

    protected static final String ENCODING = "utf-8";

    protected final String hostUrl;

    protected final Path restApiRequestFolder;

    protected final Path restApiExpectedResponseFolder;

    public AbstractClientApiTest() {
        this.hostUrl = readHostUrl();
        this.restApiRequestFolder = readRestApiRequestFolder();
        this.restApiExpectedResponseFolder = readRestApiExpectedResponseFolder();
    }

    private Path readRestApiRequestFolder() {
        String versionFolder = convertRestApiVersionToFolderName();
        return Paths.get(
                readConfigurationValue(
                        "testsuite.restApiRequestFolder",
                        "src/test/rest" + (versionFolder == null ? "" : "/" + versionFolder) + "/request"));
    }

    private Path readRestApiExpectedResponseFolder() {
        String versionFolder = convertRestApiVersionToFolderName();
        return Paths.get(
                readConfigurationValue(
                        "testsuite.restApiExpectedResponseFolder",
                        "src/test/rest" + (versionFolder == null ? "" : "/" + versionFolder) + "/expectedResponse/"));
    }

    protected String readConfigurationValue(String name, String defaultValue) {
        String value = readConfigurationValue(name);
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }

    protected String readConfigurationValue(String name) {
        return System.getProperty(name);
    }

    // TODO convert to builder pattern appropriately
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

        public ExpectedResponseFilenameBuilder(Path folder, String path, ContentType contentType, String variant) {
            this.folder = folder;
            this.path = path;
            this.contentType = contentType;
            this.variant = variant;
        }

        public File getFile() {
            return getPath().toFile();
        }

        public Path getPath() {
            return folder.resolve(convertPath(path) + convertVariant(variant) + convertContentType(contentType));
        }

        private String convertVariant(String path) {
            return variant.equals(DEFAULT_VARIANT) ? "" : "/" + variant;
        }

        private String convertPath(String path) {
            String convertedPath = path.endsWith("/") ? path.substring(0, path.length() - 1) + "index" : path;
            return convertedPath.startsWith("/") ? convertedPath.substring(1) : convertedPath;
        }

        private String convertContentType(ContentType contentType) {
            if ("text/html".equals(contentType.getMimeType()))
                return ".html";
            return ".json";
        }

    }

    // TODO convert to builder pattern appropriately
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

        public RequestFilenameBuilder(Path folder, String path, ContentType contentType, String variant) {
            this.folder = folder;
            this.path = path;
            this.contentType = contentType;
            this.variant = variant;
        }

        public File getFile() {
            return getPath().toFile();
        }

        public Path getPath() {
            return folder.resolve(convertPath(path) + convertVariant(variant) + convertContentType(contentType));
        }

        private String convertVariant(String path) {
            return variant.equals(DEFAULT_VARIANT) ? "" : "/" + variant;
        }

        private String convertPath(String path) {
            String convertedPath = path.endsWith("/") ? path.substring(0, path.length() - 1) + "index" : path;
            return convertedPath.startsWith("/") ? convertedPath.substring(1) : convertedPath;
        }

        private String convertContentType(ContentType contentType) {
            if ("text/html".equals(contentType.getMimeType()))
                return ".html";
            return ".json";
        }

    }

    private String readHostUrl() {
        return readConfigurationValue("testsuite.hostUrl", "localhost:8083");
    }

    protected abstract String readRestApiVersion();

    protected abstract String getContextRoot();

    protected abstract String convertRestApiVersionToFolderName();

    protected String getFolderName(String apiVersion) {
        if (apiVersion == null)
            return null;
        if (apiVersion.startsWith("v-0"))
            return "v-1.0";
        return apiVersion;
    }

    protected File getJsonRequestFile(String path, String variant) {
        return new RequestFilenameBuilder(restApiRequestFolder, path, ContentType.APPLICATION_JSON, variant).getFile();
    }

    protected File getJsonResponseFile(String path, String variant) {
        return new ExpectedResponseFilenameBuilder(
                restApiExpectedResponseFolder,
                path,
                ContentType.APPLICATION_JSON,
                variant).getFile();
    }

    protected void assertEqualsJson(String expected, String actual) {
        try {
            JSONAssert.assertEquals(expected, actual, JSONCompareMode.NON_EXTENSIBLE);
        } catch (JSONException ex) {
            fail("The test wasn't able to compare JSON strings", ex);
        }
    }

    @Test
    final void testJsonEquals() {
        String s1 = "[{\"groupId\": \"com.google.guava\", \"artifactId\": \"guava\", \"version\": \"13.0.1\"}]";
        String s2 = "[{\"groupId\": \"com.google.guava\", \"version\": \"13.0.1\", \"artifactId\": \"guava\"}]";
        assertEqualsJson(s1, s2);
    }
}
