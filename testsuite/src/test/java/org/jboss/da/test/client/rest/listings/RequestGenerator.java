package org.jboss.da.test.client.rest.listings;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RequestGenerator {

    public RequestGenerator() {
    }

    public String returnProductString(String name, String version, String status) {
        StringBuilder b = new StringBuilder("{ \"name\":\"");
        b.append(name);
        b.append("\", \"version\":\"");
        b.append(version);
        b.append("\"");
        if (status != null) {
            b.append(",\"supportStatus\":\"");
            b.append(status);
            b.append("\"");
        }
        b.append("}");
        return b.toString();
    }

    public String returnWhiteArtifactString(String groupId, String artifactId, String version, long id) {
        StringBuilder b = new StringBuilder("{\"groupId\":\"");
        b.append(groupId);
        b.append("\",\"artifactId\":\"");
        b.append(artifactId);
        b.append("\",\"version\":\"");
        b.append(version);
        b.append("\",\"productId\":\"");
        b.append(id);
        b.append("\"");
        b.append("}");
        return b.toString();
    }

}
