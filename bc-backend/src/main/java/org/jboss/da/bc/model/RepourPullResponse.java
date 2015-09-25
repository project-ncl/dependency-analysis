package org.jboss.da.bc.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class RepourPullResponse {

    @Getter
    @Setter
    private String tag;

    @Getter
    @Setter
    private RepourReposUrl url;

    @Getter
    @Setter
    private String branch;

    public static class RepourReposUrl {

        @Getter
        @Setter
        private String readonly;

        @Getter
        @Setter
        private String readwrite;
    }
}
