package org.jboss.da.rest.reports.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.List;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Honza Brázdil <janinko.g@gmail.com>
 */
@XmlRootElement(name = "report")
@XmlAccessorType(XmlAccessType.FIELD)
@RequiredArgsConstructor
public class GAVRequest {

    @Getter
    @NonNull
    private final String groupId;

    @Getter
    @NonNull
    private final String artifactId;

    @Getter
    @NonNull
    private final String version;

    @Getter
    @NonNull
    private final List<Product> products;
}
