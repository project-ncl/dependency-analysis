package org.jboss.da.listings.api.model;

import java.io.Serializable;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * 
 * @author Jozef Mrazek &lt;jmrazek@redhat.com&gt;
 *
 */
@ToString
@MappedSuperclass
@EqualsAndHashCode
public abstract class GenericEntity implements Serializable {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

}
