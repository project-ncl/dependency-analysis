package org.jboss.da.communication.pom.model;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;
import lombok.Getter;

/**
 * Created by dcheung on 18/09/15.
 */
public class MavenRepositories {

    @XmlElement(name = "repository")
    @Getter
    private List<MavenRepository> repositories;
}
