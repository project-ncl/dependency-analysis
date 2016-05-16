package org.jboss.da.products.api;

import org.jboss.da.model.rest.GA;

import lombok.Getter;
import lombok.NonNull;

/**
 *
 * @author jbrazdil
 */
public class ArtifactDiff {

    @Getter
    @NonNull
    private final GA ga;

    @Getter
    private final String leftVersion;

    @Getter
    private final String rightVersion;

    public ArtifactDiff(String leftVersion, GA ga, String rightVersion) {
        if (leftVersion == null && rightVersion == null)
            throw new IllegalArgumentException("Both left and right version can't be null.");

        this.ga = ga;
        this.leftVersion = leftVersion;
        this.rightVersion = rightVersion;
    }

    public ArtifactDiff(GA ga, String rightVersion) {
        this(null, ga, rightVersion);
    }

    public ArtifactDiff(String leftVersion, GA ga) {
        this(leftVersion, ga, null);
    }

    public boolean isAdded() {
        return leftVersion == null;
    }

    public boolean isRemoved() {
        return rightVersion == null;
    }

    public boolean isChanged() {
        return leftVersion != null && rightVersion != null && !leftVersion.equals(rightVersion);
    }

    public boolean isUnchanged() {
        return leftVersion != null && rightVersion != null && leftVersion.equals(rightVersion);
    }
}
