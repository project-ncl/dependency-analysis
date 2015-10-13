package org.jboss.da.communication.pnc.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PNCResponseWrapper<T> {

    @Getter
    @Setter
    T content;
}
