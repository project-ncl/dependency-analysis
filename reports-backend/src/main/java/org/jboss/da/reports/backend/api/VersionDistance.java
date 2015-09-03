package org.jboss.da.reports.backend.api;

/**
 *
 * @author Honza Brázdil <janinko.g@gmail.com>
 */
public class VersionDistance implements Comparable<VersionDistance> {

    private int majorDiff;

    private int minorDiff;

    private int microDiff;

    public VersionDistance(String versionA, String versionB) {
        String[] vA = versionA.split(".");
        String[] vB = versionB.split(".");
        this.majorDiff = Math.abs(Integer.parseInt(vA[0]) - Integer.parseInt(vB[0]));
        this.minorDiff = Math.abs(Integer.parseInt(vA[1]) - Integer.parseInt(vB[1]));
        this.microDiff = Math.abs(Integer.parseInt(vA[2]) - Integer.parseInt(vB[2]));
    }

    @Override
    public int compareTo(VersionDistance t) {
        int d;
        d = majorDiff - t.majorDiff;
        if (d != 0)
            return d;
        d = minorDiff - t.minorDiff;
        if (d != 0)
            return d;
        d = microDiff - t.microDiff;
        if (d != 0)
            return d;
        return 0;
    }

}
