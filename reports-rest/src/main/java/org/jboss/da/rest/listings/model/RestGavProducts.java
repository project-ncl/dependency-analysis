package org.jboss.da.rest.listings.model;

import org.jboss.da.communication.model.GAV;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class RestGavProducts {

    @Getter
    @Setter
    protected String groupId;

    @Getter
    @Setter
    protected String artifactId;

    @Getter
    @Setter
    protected String version;

    @Getter
    @Setter
    protected Set<RestProductInput> products;

    public RestGavProducts(GAV gav, Set<RestProductInput> products) {
        this(gav.getGroupId(), gav.getArtifactId(), gav.getVersion(), products);
    }

}
