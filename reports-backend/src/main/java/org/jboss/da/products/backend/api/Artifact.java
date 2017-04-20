package org.jboss.da.products.backend.api;

import org.jboss.da.model.rest.GAV;

import lombok.Data;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
public class Artifact {

    private final GAV gav;
}
