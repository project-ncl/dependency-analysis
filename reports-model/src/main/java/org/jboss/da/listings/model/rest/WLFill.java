package org.jboss.da.listings.model.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.Collections;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@XmlRootElement(name = "productArtifact")
@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class WLFill {

    @Getter
    @Setter
    private String scmUrl;

    @Getter
    @Setter
    private String revision;

    @Getter
    @Setter
    private String pomPath;

    @Getter
    @Setter
    private List<String> repositories = Collections.emptyList();

    @Getter
    private long productId;
}
