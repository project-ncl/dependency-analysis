package org.jboss.da.reports.backend.api;

import java.util.List;
import org.jboss.da.reports.api.GAV;

/**
 *
 * @author Honza Brázdil <janinko.g@gmail.com>
 */
public interface VersionFinder {
    
    List<String> getVersionsFor(GAV gav);
    
    String getBestMatchVersionFor(GAV gav);
}
