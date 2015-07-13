package org.jboss.da.listings.rest.impl;

import org.jboss.da.listings.rest.api.Root;

public class RootImpl implements Root {

    @Override
    public String getDescription() {
        return "<h1>Dependency analyzer REST</h1>" + "<ul><li><strong>Version:</strong> 1</li>"
                + "<li><strong>Documentation:</strong> https://docs.engineering.redhat.com/display/JP/REST+endpoints+proposal</li></ul>";
    }

}
