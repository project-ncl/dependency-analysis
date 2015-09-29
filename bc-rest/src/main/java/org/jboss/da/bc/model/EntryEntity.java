package org.jboss.da.bc.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class EntryEntity {

    @Getter
    @Setter
    protected String scmUrl;

    @Getter
    @Setter
    protected String pomPath;

    @Getter
    @Setter
    protected String scmRevision;

    @Getter
    @Setter
    protected String name;

}
