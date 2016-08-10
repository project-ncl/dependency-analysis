package org.jboss.da.listings.api.model;

import javax.persistence.Entity;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Jozef Mrazek <jmrazek@redhat.com>
 *
 */
@RequiredArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@EqualsAndHashCode(callSuper = true)
@Entity
public class GA extends GenericEntity {

    @Setter
    @Getter
    @NonNull
    private String groupId;

    @Setter
    @Getter
    @NonNull
    private String artifactId;
}
