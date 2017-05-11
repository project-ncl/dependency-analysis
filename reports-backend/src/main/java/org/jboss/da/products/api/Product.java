package org.jboss.da.products.api;

import org.jboss.da.listings.model.ProductSupportStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@EqualsAndHashCode(exclude = { "status" })
@AllArgsConstructor
public class Product {

    public static final Product UNKNOWN = new Product("Unknown", "Unknown",
            ProductSupportStatus.UNKNOWN);

    private final String name;

    private final String version;

    private final ProductSupportStatus status;

    public Product(String name, String version) {
        this.name = name;
        this.version = version;
        this.status = ProductSupportStatus.UNKNOWN;
    }
}
