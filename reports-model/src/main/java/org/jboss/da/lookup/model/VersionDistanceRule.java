package org.jboss.da.lookup.model;

/**
 * Rules for determining comparative distance of two versions toward a base version.
 */
public enum VersionDistanceRule {
    /**
     * This rule tries to suggest the best replacement version.
     *
     * Rule 1: Version with the same major part as base is closer than version with major part different to base.
     * <p>
     * Rule 2: Version higher than base is considered closer than version lower than base.
     * <p>
     * Rule 3: Version closer to base version by standard order is considered closer.
     * <p>
     * Note: Higher qualifier (e.g. Final) considered better and thus closer than otherwise the same version with lower
     * qualifier (e.g. Alpha). This may not be correct for all qualifiers.
     */
    RECOMMENDED_REPLACEMENT,
    /**
     * This rule orders the version by closeness of their parts.
     *
     * Rule 1: Version which differs in less significant part (major, minor, micro, qualifier, suffix) from the base
     * version is considered closer than version with differs in more significant part.
     * <p>
     * Rule 2: If both version differs in the same most significant numerical part (major, minor, micro) to base
     * version: <br>
     * a) version which has the part numerically closer to base is considered closer. <br>
     * b) if they are the same numerical distance to base, the higher number is considered closer.
     * <p>
     * Rule 3: Version closer to base version by standard order is considered closer.
     * <p>
     * Note: Higher qualifier (e.g. Final) considered better and thus closer than otherwise the same version with lower
     * qualifier (e.g. Alpha). This may not be correct for all qualifiers.
     */
    CLOSEST_BY_PARTS
}
