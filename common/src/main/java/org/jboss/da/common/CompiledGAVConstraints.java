package org.jboss.da.common;

import lombok.experimental.SuperBuilder;
import org.jboss.da.model.rest.Constraints;
import org.jboss.da.model.rest.GAV;
import org.jboss.pnc.common.alignment.ranking.AlignmentPredicate;
import org.jboss.pnc.common.alignment.ranking.AlignmentRanking;
import org.jboss.pnc.common.alignment.ranking.exception.ValidationException;

@SuperBuilder
public class CompiledGAVConstraints extends CompiledConstraints<GAV> {

    @Override
    public int matchSignificance(GAV toMatch) {
        if (getArtifactScope() == null) {
            // null signifies global scope and constraints, has the least significance
            return 1;
        }

        String[] scopeSplit = getArtifactScope().split(":");
        String group = toMatch.getGroupId();
        String artifact = toMatch.getArtifactId();
        String version = toMatch.getVersion();

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

    public static CompiledGAVConstraints from(Constraints constraints) throws ValidationException {
        return builder().artifactScope(constraints.getArtifactScope())
                .ranks(new AlignmentRanking(constraints.getRanks(), null))
                .allowList(new AlignmentPredicate(constraints.getAllowList(), ver -> true))
                .denyList(new AlignmentPredicate(constraints.getDenyList(), ver -> false))
                .build();
    }
}
