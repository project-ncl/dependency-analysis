package org.jboss.da.listings.model.rest;

import org.jboss.da.model.rest.GAV;

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
