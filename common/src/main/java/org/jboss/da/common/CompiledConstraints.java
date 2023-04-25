package org.jboss.da.common;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.jboss.pnc.common.alignment.ranking.AlignmentPredicate;
import org.jboss.pnc.common.alignment.ranking.AlignmentRanking;

import java.util.Comparator;

@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class CompiledConstraints<T> implements Comparator<T> {
    @Getter
    private final String artifactScope;

    @Getter
    private final AlignmentRanking ranks;

    @Getter
    private final AlignmentPredicate denyList;

    @Getter
    private final AlignmentPredicate allowList;

    public abstract int matchSignificance(T match);

    /**
     * Returns such constraints that do not alter behaviour of version analysis
     *
     * @return constraint that do not affect version analysis
     */
    public static CompiledConstraints<Object> none() {
        return DefaultCompiledConstraints.get();
    }

    @SuperBuilder
    private static class DefaultCompiledConstraints extends CompiledConstraints<Object> {
        @Override
        public int matchSignificance(Object match) {
            return 0;
        }

        private static DefaultCompiledConstraints get() {
            return builder().artifactScope(null)
                    .ranks(new AlignmentRanking(null, null))
                    .allowList(new AlignmentPredicate(null, ver -> true))
                    .denyList(new AlignmentPredicate(null, ver -> false))
                    .build();
        }
    }

    @Override
    public int compare(T o1, T o2) {
        return Comparator.comparingInt(this::matchSignificance).compare(o1, o2);
    }
}
