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
public abstract class CompiledStrategy<T> implements Comparator<T> {
    @Getter
    private final String artifactScope;

    @Getter
    private final AlignmentRanking ranks;

    @Getter
    private final AlignmentPredicate denyList;

    @Getter
    private final AlignmentPredicate allowList;

    /**
     * Returns a number that conveys how much does an artifact identifier conform to an artifactScope. Higher number
     * suggests a better match. The method returns 0 if the identifier doesn't match the artifactScope at all.
     *
     * @param artifactIdentifier artifact identifier
     * @return an integer. Higher value means higher match. No match return 0.
     */
    public abstract int matchSignificance(T artifactIdentifier);

    /**
     * Returns such strategy that do not alter behaviour of version analysis
     *
     * @return strategy that do not affect version analysis
     */
    public static CompiledStrategy<Object> none() {
        return DefaultCompiledStrategy.get();
    }

    @SuperBuilder
    private static class DefaultCompiledStrategy extends CompiledStrategy<Object> {
        @Override
        public int matchSignificance(Object artifactIdentifier) {
            return 0;
        }

        private static DefaultCompiledStrategy get() {
            return builder().artifactScope(null)
                    .ranks(new AlignmentRanking(null, null))
                    .allowList(new AlignmentPredicate(null, ver -> true))
                    .denyList(new AlignmentPredicate(null, ver -> false))
                    .build();
        }
    }

    /**
     * Compare objects of Type T on how significant they match against this strategy's scope. T is usually a GAV.
     *
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
     * @return
     */
    @Override
    public int compare(T o1, T o2) {
        int s1 = matchSignificance(o1);
        int s2 = matchSignificance(o2);

        return Integer.compare(s1, s2);
    }
}
