package org.jboss.da.bc.facade;

import org.apache.maven.scm.ScmException;
import org.jboss.da.bc.model.rest.EntryEntity;
import org.jboss.da.bc.model.rest.FinishResponse;
import org.jboss.da.bc.model.rest.InfoEntity;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.pom.PomAnalysisException;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 * @param <I> Type of the payload
 */
public interface BuildConfigurationsFacade<I extends InfoEntity> {

    I startAnalyse(EntryEntity entry) throws ScmException, PomAnalysisException,
            CommunicationException;

    I analyseNextLevel(I bc) throws CommunicationException;

    FinishResponse<I> finishAnalyse(I bc);

}
