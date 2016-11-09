package org.jboss.da.bc.model.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.jboss.da.model.rest.validators.ScmUrl;

@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class EntryEntity {

    @Getter
    @Setter
    @ScmUrl
    protected String scmUrl;

    @Getter
    @Setter
    @ScmUrl
    protected String externalScmUrl;

    @Getter
    @Setter
    protected String pomPath;

    @Getter
    @Setter
    protected String scmRevision;

    @Getter
    @Setter
    protected String externalScmRevision;

    @Getter
    @Setter
    protected String productVersion;

    @Getter
    @Setter
    protected int id;

    @Getter
    @Setter
    protected List<String> repositories;

}
