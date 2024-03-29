package org.jboss.da.common;

import lombok.experimental.SuperBuilder;
import org.jboss.da.model.rest.Strategy;
import org.jboss.da.model.rest.GAV;
import org.jboss.pnc.common.alignment.ranking.AlignmentPredicate;
import org.jboss.pnc.common.alignment.ranking.AlignmentRanking;
import org.jboss.pnc.common.alignment.ranking.exception.ValidationException;

@SuperBuilder
public class ScopedGAVStrategy extends ScopedStrategy<GAV> {

    @Override
    public int matchSignificance(GAV gav) {
        if (getArtifactScope() == null) {
            // Null signifies a global scope for a strategy. It has the least non-zero significance.
            return 1;
        }

        String[] scopeSplit = getArtifactScope().split(":");
        String group = gav.getGroupId();
        String artifact = gav.getArtifactId();
        String version = gav.getVersion();

        if (scopeSplit.length == 1 && group.equals(scopeSplit[0])) {
            // gav matched groupID
            return 2;
        }
        if (scopeSplit.length == 2 && group.equals(scopeSplit[0]) && artifact.equals(scopeSplit[1])) {
            // gav matched groupID and artifactID
            return 3;
        }
        if (scopeSplit.length == 3 && group.equals(scopeSplit[0]) && artifact.equals(scopeSplit[1])
                && version.equals(scopeSplit[2])) {
            // gav matched groupID, artifactID and version
            return 4;
        }

        // no match
        return 0;
    }

    public static ScopedGAVStrategy from(Strategy restStrategy) throws ValidationException {
        return builder().artifactScope(restStrategy.getArtifactScope())
                .ranks(new AlignmentRanking(restStrategy.getRanks(), null))
                .allowList(new AlignmentPredicate(restStrategy.getAllowList(), ver -> true))
                .denyList(new AlignmentPredicate(restStrategy.getDenyList(), ver -> false))
                .build();
    }
}
