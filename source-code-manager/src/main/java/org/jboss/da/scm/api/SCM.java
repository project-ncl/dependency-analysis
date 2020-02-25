package org.jboss.da.scm.api;

import java.io.File;
import java.time.Duration;
import java.time.temporal.TemporalAmount;
import org.apache.maven.scm.ScmException;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public interface SCM {

    /**
     * Time to keep the repository in cache. Value is 4 hours.
     */
    public static final TemporalAmount TIME_TO_KEEP = Duration.ofHours(4);

    /**
     * Clone repository and return directory where the repository is located. The returned File object is weakly cached.
     * As long as it is referenced the repository will stay clonned. The repository will stay cloned for at least
     * {@link SCM#TIME_TO_KEEP} after the File object was garbagecollected.
     * 
     * @param scmType
     * @param scmUrl
     * @param revision
     * @return
     * @throws ScmException
     * @see SCM#TIME_TO_KEEP
     */
    File cloneRepository(SCMType scmType, String scmUrl, String revision) throws ScmException;
}
