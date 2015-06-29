package org.jboss.da.listings.api.model;

import javax.persistence.MappedSuperclass;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * 
 * @author Jozef Mrazek <jmrazek@redhat.com>
 *
 */
@RequiredArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@MappedSuperclass
public class Artifact extends GenericEntity {

    @Getter
    @NonNull
    private String groupId;

    @Getter
    @NonNull
    private String artifactId;

    @Getter
    @NonNull
    private String version;
}
