package org.jboss.da.communication.aprox.impl;

import org.jboss.da.communication.aprox.api.AproxConnector;
import org.jboss.da.communication.aprox.model.GA;
import org.jboss.da.communication.aprox.model.GAV;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;

import javax.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class AproxConnectorImpl implements AproxConnector {

    @Override
    public GAVDependencyTree getDependencyTreeOfRevision(String scmUrl, String revision,
            String pomPath) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GAVDependencyTree getDependencyTreeOfGAV(GAV gav) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getVersionsOfGA(GA ga) {
        // TODO Auto-generated method stub
        return null;
    }

}
