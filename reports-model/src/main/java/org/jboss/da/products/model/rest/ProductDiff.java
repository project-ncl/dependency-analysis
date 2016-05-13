package org.jboss.da.products.model.rest;

import org.jboss.da.listings.model.rest.RestProduct;
import org.jboss.da.model.rest.GAV;

import java.util.HashSet;
import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class ProductDiff {

    @Getter
    @Setter
    @NonNull
    private RestProduct leftProduct;

    @Getter
    @Setter
    @NonNull
    private RestProduct rightProduct;

    @Getter
    @Setter
    @NonNull
    private Set<GAV> added = new HashSet<>();

    @Getter
    @Setter
    @NonNull
    private Set<GAV> removed = new HashSet<>();

    @Getter
    @Setter
    @NonNull
    private Set<GADiff> changed = new HashSet<>();

    @Getter
    @Setter
    @NonNull
    private Set<GAV> unchanged = new HashSet<>();
}
