package org.jboss.da.communcation.pnc.authentication;

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
import org.jboss.da.communcation.pnc.ReadResource;

/**
 * Class obtained from pnc example on OAuth example on PNC:
 * https://github.com/project-ncl/pnc/blob/master/examples/oauth-client/src/main/java/org/jboss/pnc/auth/client/SimpleOAuthConnect.java
 */
public class PNCAuthentication {

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

    public static String getAccessToken(String url, String clientId, String username, String password)
            throws ClientProtocolException, IOException {
        Map<String, String> urlParams = new HashMap<String, String>();
        urlParams.put("grant_type", "password");
        urlParams.put("client_id", clientId);
        urlParams.put("username", username);
        urlParams.put("password", password);
        return connect(url, urlParams)[0];
    }

    public static String getrefreshToken(String url, String clientId, String username, String password)
            throws ClientProtocolException, IOException {
        Map<String, String> urlParams = new HashMap<String, String>();
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

        List<BasicNameValuePair> urlParameters = new ArrayList<BasicNameValuePair>();
        for (String key : urlParams.keySet()) {
            urlParameters.add(new BasicNameValuePair(key, urlParams.get(key)));
        }
        httpPost.setEntity(new UrlEncodedFormEntity(urlParameters));
        CloseableHttpResponse response = httpclient.execute(httpPost);

        String refreshToken = "";
        String accessToken = "";
        try {
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            String line = "";
            while ((line = rd.readLine()) != null) {
                if (line.contains("refresh_token")) {
                    String[] respContent = line.split(",");
                    for (int i = 0; i < respContent.length; i++) {
                        String split = respContent[i];
                        if (split.contains("refresh_token")) {
                            refreshToken = split.split(":")[1].substring(1, split.split(":")[1].length() - 1);
                        }
                        if (split.contains("access_token")) {
                            accessToken = split.split(":")[1].substring(1, split.split(":")[1].length() - 1);
                        }
                    }
                }
            }
        } finally {
            response.close();
        }
        return new String[]{accessToken, refreshToken};


    }

    public static String authenticate() {
        try {
            String keycloakServer = ReadResource.getResource("keycloak_server");
            String realm = ReadResource.getResource("keycloak_realm");
            String clientId = ReadResource.getResource("keycloak_clientid");
            String username = ReadResource.getResource("keycloak_username");
            String password = ReadResource.getResource("keycloak_password");

            return PNCAuthentication.getAccessToken(
                    keycloakServer + "/auth/realms/" + realm + "/tokens/grants/access",
                    clientId, username, password);
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
