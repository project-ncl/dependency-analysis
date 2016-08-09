package org.jboss.da.scm.impl;

import org.apache.maven.scm.ScmException;
import org.jboss.da.scm.api.SCM;
import org.jboss.da.scm.api.SCMType;

import javax.inject.Inject;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
public class SCMImpl implements SCM {

    @Inject
    SCMCache cache;

    @Override
    public File cloneRepository(SCMType scmType, String scmUrl, String revision) throws ScmException {
        SCMSpecifier spec = new SCMSpecifier(scmType, scmUrl, revision);

        CompletableFuture<DirectoryReference> fref = cache.get(spec);
        
        if(fref.isDone()){
            try {
                DirectoryReference ref = fref.get();
                Optional<File> ofile = ref.get();
                if(ofile.isPresent())
            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(SCMImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
}
