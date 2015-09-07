package org.jboss.da.communication.pnc.authentication;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.jboss.da.common.json.DAConfig;
import org.jboss.da.common.util.Configuration;
import org.jboss.da.common.util.ConfigurationParseException;

/**
 * Class obtained from pnc example on OAuth example on PNCProducer:
 * https://github.com/project-ncl/pnc/blob/master/examples/oauth-client/src/main/java/org/jboss/pnc/auth/client/SimpleOAuthConnect.java
 */
public class PNCAuthentication {

    Configuration config = new Configuration();

    public static String getAccessToken(String url, Map<String, String> urlParams)
            throws ClientProtocolException, IOException {
        return connect(url, urlParams)[0];
    }

    public static String getRefreshToken(String url, Map<String, String> urlParams)
            throws ClientProtocolException, IOException {
        return connect(url, urlParams)[1];
    }

    public static String[] getTokens(String url, Map<String, String> urlParams)
            throws ClientProtocolException, IOException {
        return connect(url, urlParams);
    }

    public static String getAccessToken(String url, String clientId, String username,
            String password) throws ClientProtocolException, IOException {
        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("grant_type", "password");
        urlParams.put("client_id", clientId);
        urlParams.put("username", username);
        urlParams.put("password", password);
        return connect(url, urlParams)[0];
    }

    public static String getrefreshToken(String url, String clientId, String username,
            String password) throws ClientProtocolException, IOException {
        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("grant_type", "password");
        urlParams.put("client_id", clientId);
        urlParams.put("username", username);
        urlParams.put("password", password);
        return connect(url, urlParams)[1];
    }

    private static String[] connect(String url, Map<String, String> urlParams)
            throws ClientProtocolException, IOException {

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);

        // add header
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

        List<BasicNameValuePair> urlParameters = new ArrayList<>();
        for (Map.Entry<String, String> e : urlParams.entrySet()) {
            urlParameters.add(new BasicNameValuePair(e.getKey(), e.getValue()));
        }
        httpPost.setEntity(new UrlEncodedFormEntity(urlParameters));
        CloseableHttpResponse response = httpclient.execute(httpPost);

        String refreshToken = "";
        String accessToken = "";
        try (BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity()
                .getContent()))) {

            String line;
            while ((line = rd.readLine()) != null) {
                if (line.contains("refresh_token")) {
                    String[] respContent = line.split(",");
                    for (String split : respContent) {
                        if (split.contains("refresh_token")) {
                            refreshToken = split.split(":")[1].substring(1,
                                    split.split(":")[1].length() - 1);
                        }
                        if (split.contains("access_token")) {
                            accessToken = split.split(":")[1].substring(1,
                                    split.split(":")[1].length() - 1);
                        }
                    }
                }
            }
        } finally {
            response.close();
        }
        return new String[] { accessToken, refreshToken };

    }

    public String authenticate() {
        try {
            DAConfig conf = config.getConfig();
            String keycloakServer = conf.getKeycloakServer();
            String realm = conf.getKeycloakRealm();
            String clientId = conf.getKeycloakClientid();
            String username = conf.getKeycloakUsername();
            String password = conf.getKeycloakPassword();

            return PNCAuthentication.getAccessToken(keycloakServer + "/auth/realms/" + realm
                    + "/tokens/grants/access", clientId, username, password);
        } catch (IOException | ConfigurationParseException e) {
            throw new RuntimeException("Failed to authenticate", e);
        }
    }
}
