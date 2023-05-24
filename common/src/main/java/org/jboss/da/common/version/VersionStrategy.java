package org.jboss.da.common.version;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.jboss.pnc.common.alignment.ranking.AlignmentPredicate;
import org.jboss.pnc.common.alignment.ranking.AlignmentRanking;

import java.util.List;

@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class VersionStrategy {

    @Getter
    private final AlignmentRanking ranks;

    @Getter
    private final AlignmentPredicate denyList;

    @Getter
    private final AlignmentPredicate allowList;

    /**
     * Returns such strategy that does not alter behaviour of version analysis
     *
     * @return strategy that does not affect version analysis
     */
    public static VersionStrategy none() {
        return VersionStrategy.builder()
                .ranks(new AlignmentRanking(null, null))
                .allowList(new AlignmentPredicate(null, ver -> true))
                .denyList(new AlignmentPredicate(null, ver -> false))
                .build();
    }

    public static VersionStrategy from(List<String> ranks, String allowList, String denyList) {
        return VersionStrategy.builder()
                .ranks(new AlignmentRanking(ranks, null))
                .allowList(new AlignmentPredicate(allowList, ver -> true))
                .denyList(new AlignmentPredicate(denyList, ver -> false))
                .build();
    }
}
