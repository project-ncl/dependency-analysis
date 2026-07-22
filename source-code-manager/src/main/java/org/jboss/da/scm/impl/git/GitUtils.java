package org.jboss.da.scm.impl.git;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility to compose the full git commands.
 */
public class GitUtils {

    public static final String DEFAULT_REMOTE_NAME = "origin";

    public static final String FETCH_HEAD = "FETCH_HEAD";

    private GitUtils() {
    }

    public static List<String> init() {
        return List.of("git", "init");
    }

    public static List<String> addRemote(String remote, String url) {
        return List.of("git", "remote", "add", remote, url);
    }

    public static List<String> fetchRef(String remote, String ref, boolean fetchShallowly) {
        List<String> command = new ArrayList<>(List.of("git", "fetch", remote, ref));
        if (fetchShallowly) {
            command.add("--depth=1");
        }

        return command;
    }

    public static List<String> checkout(String ref) {
        return List.of("git", "checkout", ref);
    }

    public static List<String> clone(String url, boolean cloneShallowly) {
        List<String> command = new ArrayList<>(List.of("git", "clone"));
        if (cloneShallowly) {
            command.add("--depth=1");
        }

        command.add(url);
        command.add(".");

        return command;
    }
}
