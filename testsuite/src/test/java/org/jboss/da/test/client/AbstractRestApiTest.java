package org.jboss.da.test.client;

import org.apache.http.entity.ContentType;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.ws.rs.core.MediaType;
import org.apache.commons.io.FileUtils;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;

@RunWith(Arquillian.class)
@RunAsClient
public abstract class AbstractRestApiTest {

    protected static final String ENCODING = "utf-8";

    protected final String hostUrl;

    protected final String restApiURL;

    protected final String restApiVersion;

    protected final Path restApiRequestFolder;

    protected final Path restApiExpectedResponseFolder;

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

    protected abstract String readRestApiVersion();

    private String readRestApiUrl() {
        return readConfigurationValue("testsuite.restApiUrl", hostUrl + "/" + getContextRoot()
                + "/rest" + (restApiVersion == null ? "" : "/" + restApiVersion));
    }

    abstract String getContextRoot();

    abstract String convertRestApiVersionToFolderName();

    protected String getFolderName(String apiVersion) {
        if (apiVersion == null)
            return null;
        if (apiVersion.startsWith("v-0"))
            return "v-1.0";
        return apiVersion;
    }

    protected File getJsonRequestFile(String path, String variant) {
        return new RequestFilenameBuilder(restApiRequestFolder, path, ContentType.APPLICATION_JSON,
                variant).getFile();
    }

    protected File getJsonResponseFile(String path, String variant) {
        return new ExpectedResponseFilenameBuilder(restApiExpectedResponseFolder, path,
                ContentType.APPLICATION_JSON, variant).getFile();
    }

    protected ClientRequest createClientRequest(String relativePath, String jsonRequest) {
        ClientRequest request = new ClientRequest(restApiURL + relativePath);
        request.header("Content-Type", APPLICATION_JSON);
        request.body(MediaType.APPLICATION_JSON_TYPE, jsonRequest);
        return request;
    }

    protected ClientResponse<String> assertResponseForRequest(String endpoint, String requestFile)
            throws IOException, Exception {
        File jsonRequestFile = getJsonRequestFile(endpoint, requestFile);
        ClientRequest request = createClientRequest(endpoint,
                FileUtils.readFileToString(jsonRequestFile, ENCODING));
        ClientResponse<String> response = request.post(String.class);
        File expectedResponseFile = getJsonResponseFile(endpoint, requestFile);
        assertEqualsJson(FileUtils.readFileToString(expectedResponseFile).trim(), response
                .getEntity(String.class).trim());
        return response;
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

    // TODO convert to builder pattern apropriatelly
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

    protected void assertEqualsJson(String expected, String actual) {
        try {

            JSONAssert.assertEquals(expected, actual, JSONCompareMode.NON_EXTENSIBLE);
        } catch (JSONException ex) {
            fail("The test wasn't able to compare JSON strings" + ex);
        }
    }

    @Test
    final public void testJsonEquals() throws JSONException {
        String s1 = "[{\"groupId\": \"com.google.guava\", \"artifactId\": \"guava\", \"version\": \"13.0.1\"}]";
        String s2 = "[{\"groupId\": \"com.google.guava\", \"version\": \"13.0.1\", \"artifactId\": \"guava\"}]";
        assertEqualsJson(s1, s2);
    }
}
