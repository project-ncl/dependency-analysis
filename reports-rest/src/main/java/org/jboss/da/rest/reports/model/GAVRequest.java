package org.jboss.da.rest.reports.model;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.List;

import lombok.Getter;
import lombok.NonNull;

/**
 *
 * @author Honza Brázdil <janinko.g@gmail.com>
 */
@XmlRootElement(name = "report")
@XmlAccessorType(XmlAccessType.FIELD)
public class GAVRequest {

    @JsonCreator
    public GAVRequest(@JsonProperty("groupId") String groupId,
            @JsonProperty("artifactId") String artifactId, @JsonProperty("version") String version,
            @JsonProperty("products") List<Product> products) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.products = products;
    }

    @Getter
    @NonNull
    @XmlElement(required = true, name = "group_id")
    private final String groupId;

    @Getter
    @NonNull
    @XmlElement(required = true, name = "artifact_id")
    private final String artifactId;

    @Getter
    @NonNull
    @XmlElement(required = true, name = "version")
    private final String version;

    @Getter
    @NonNull
    @XmlElement(required = true, name = "products")
    private final List<Product> products;
}
