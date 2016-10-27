package org.jboss.da.communication.aprox.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.URL;

@EqualsAndHashCode
@RequiredArgsConstructor
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class Repository {

    @Getter
    @NonNull
    private String name;

    @Getter
    @NonNull
    @URL(message = "Invalid URL address")
    private String url;

}
