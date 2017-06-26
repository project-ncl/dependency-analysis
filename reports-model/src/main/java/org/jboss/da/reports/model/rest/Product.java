package org.jboss.da.reports.model.rest;

import com.fasterxml.jackson.annotation.JsonRootName;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@JsonRootName(value = "report")
@RequiredArgsConstructor
public class Product {

    @Getter
    @NonNull
    private final String name;

    @Getter
    @NonNull
    private final String version;
}
