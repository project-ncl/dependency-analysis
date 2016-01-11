package org.jboss.da.listings.api.model;

import javax.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Jozef Mrazek <jmrazek@redhat.com>
 *
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@Entity
public class Product extends GenericEntity {

    @Setter
    @Getter
    @NonNull
    private String name;

    public Product(String name) {
        this.name = name;
    }

}
