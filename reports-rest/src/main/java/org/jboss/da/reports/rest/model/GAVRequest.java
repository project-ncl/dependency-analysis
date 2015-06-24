package org.jboss.da.reports.rest.model;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
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
    @XmlElement(required = true, name = "group_id")
    private final String groupId;
    
    @Getter
    @NonNull
    @XmlElement(required = true, name = "artifact_id")
    private final String artifactId;
    
    @Getter
    @NonNull
    @XmlElement(required = true, name = "version")
    private final String version;
    
    @Getter
    @NonNull
    @XmlElement(required = true, name = "products")
    private final List<Product> products;
}
