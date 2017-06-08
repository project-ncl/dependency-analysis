package org.jboss.da.listings.impl.service;

import lombok.Getter;

/**
 *
 * @author Stanislav Knot &lt;sknot@redhat.com&gt;
 */

class GAExistsException extends Exception {

    @Getter
    private final String GA;

    public GAExistsException(String GA) {
        this.GA = GA;
    }
}
