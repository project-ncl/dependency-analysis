package org.jboss.da.listings.impl.service;

import lombok.Getter;

/**
 *
 * @author Stanislav Knot <sknot@redhat.com>
 */

class GAExistsException extends Exception {

    @Getter
    private String GA;

    public GAExistsException(String GA) {
        this.GA = GA;
    }
}
