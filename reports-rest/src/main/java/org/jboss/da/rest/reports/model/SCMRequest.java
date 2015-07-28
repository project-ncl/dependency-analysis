package org.jboss.da.rest.reports.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
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
public class SCMRequest {

    @Getter
    @NonNull
    @XmlElement(required = true, name = "scm_url")
    private final String scmURL;

    @Getter
    @NonNull
    @XmlElement(required = true, name = "commit_id")
    private final String commitRef;

    @Getter
    @NonNull
    @XmlElement(required = true, name = "pom_path")
    private final String pomPath;

    @Getter
    @NonNull
    @XmlElement(required = true, name = "products")
    private final List<Product> products;
}
