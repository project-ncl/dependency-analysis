package org.jboss.da.listings.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Jozef Mrazek &lt;jmrazek@redhat.com&gt;
 *
 */
@JsonRootName(value = "contains")
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class ContainsResponse {

    @Getter
    @Setter
    @JsonProperty(required = true, value = "contains")
    protected boolean contains;

    @Getter
    @Setter
    @JsonProperty(required = true, value = "found")
    private List<RestArtifact> found;
}
