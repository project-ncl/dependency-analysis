package org.jboss.da.common.util;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;

import javax.ws.rs.core.MediaType;

/**
 * 
 * Library class which helps with the HTTP communication
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
public class HttpUtil {

    /**
     * Process HTTP GET request and gets the data as type specified as parameter.
     * Client accepts application/json MIME type.
     *
     * @param clazz Class to which the data are unmarshalled
     * @param url Request URL
     * @return Unmarshalled entity data
     * @throws Exception Thrown if some error occurs in communication with server
     */
    public static <T> T processGetRequest(Class<T> clazz, String url) throws Exception {
        ClientRequest request = new ClientRequest(url);
        request.accept(MediaType.APPLICATION_JSON);

        ClientResponse<T> response = request.get(clazz);
        return response.getEntity();
    }

    /**
     * Process HTTP GET request and gets the data in JSON.
     * Client accepts application/json MIME type.
     * 
     * @param url Request URL
     * @return Unmarshalled entity data
     * @throws Exception Thrown if some error occurs in communication with server
     */
    public static String processGetRequest(String url) throws Exception {
        return processGetRequest(String.class, url);
    }

    /**
     * Process HTTP POST request with JSON data and gets the data as type specified as parameter.
     * Client accepts application/json MIME type.
     *
     * @param clazz Class to which the data are unmarshalled
     * @param requestBody Body of the request in JSON
     * @param url Request URL
     * @return Unmarshalled entity data
     * @throws Exception Thrown if some error occurs in communication with server
     */
    public static <T> T processPostRequest(Class<T> clazz, Object requestBody, String url)
            throws Exception {
        ClientRequest request = new ClientRequest(url);
        request.accept(MediaType.APPLICATION_JSON);
        request.body(MediaType.APPLICATION_JSON, requestBody);

        ClientResponse<T> response = request.post(clazz);
        return response.getEntity();
    }
}
