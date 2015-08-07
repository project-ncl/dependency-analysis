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
public class SCMRequest {

    @Getter
    @NonNull
    private final String scmURL;

    @Getter
    @NonNull
    private final String commitId;

    @Getter
    @NonNull
    private final String pomPath;

    @Getter
    @NonNull
    private final List<Product> products;
}
