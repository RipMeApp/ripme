package com.rarchives.ripme.utils.browser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Locates Firefox profile directories by reading {@code profiles.ini}.
 */
final class FirefoxProfiles {

    private FirefoxProfiles() {
    }

    /** Directories that may contain a Firefox {@code profiles.ini}. */
    static List<Path> rootDirs() {
        List<Path> roots = new ArrayList<>();
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String home = System.getProperty("user.home", "");
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                roots.add(Paths.get(appData, "Mozilla", "Firefox"));
            }
        } else if (os.contains("mac")) {
            roots.add(Paths.get(home, "Library", "Application Support", "Firefox"));
        } else {
            roots.add(Paths.get(home, ".mozilla", "firefox"));
            roots.add(Paths.get(home, "snap", "firefox", "common", ".mozilla", "firefox"));
            roots.add(Paths.get(home, ".var", "app", "org.mozilla.firefox", ".mozilla", "firefox"));
        }
        return roots;
    }

    /** All profile directories listed in the given profiles.ini, default profiles first. */
    static List<Path> parseProfilesIni(Path iniFile) throws IOException {
        Path root = iniFile.getParent();
        Set<Path> installDefaults = new LinkedHashSet<>();
        Set<Path> flaggedDefaults = new LinkedHashSet<>();
        Set<Path> others = new LinkedHashSet<>();

        String section = null;
        Map<String, String> values = new HashMap<>();
        for (String raw : Files.readAllLines(iniFile, StandardCharsets.UTF_8)) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith(";") || line.startsWith("#")) {
                continue;
            }
            if (line.startsWith("[") && line.endsWith("]")) {
                collect(root, section, values, installDefaults, flaggedDefaults, others);
                section = line.substring(1, line.length() - 1);
                values = new HashMap<>();
            } else {
                int eq = line.indexOf('=');
                if (eq > 0) {
                    values.put(line.substring(0, eq).trim(), line.substring(eq + 1).trim());
                }
            }
        }
        collect(root, section, values, installDefaults, flaggedDefaults, others);

        Set<Path> ordered = new LinkedHashSet<>(installDefaults);
        ordered.addAll(flaggedDefaults);
        ordered.addAll(others);
        return new ArrayList<>(ordered);
    }

    private static void collect(Path root, String section, Map<String, String> values, Set<Path> installDefaults,
            Set<Path> flaggedDefaults, Set<Path> others) {
        if (section == null) {
            return;
        }
        if (section.startsWith("Install")) {
            String path = values.get("Default");
            if (path != null && !path.isEmpty()) {
                installDefaults.add(resolve(root, path, true));
            }
        } else if (section.startsWith("Profile")) {
            String path = values.get("Path");
            if (path == null || path.isEmpty()) {
                return;
            }
            Path profile = resolve(root, path, !"0".equals(values.get("IsRelative")));
            if ("1".equals(values.get("Default"))) {
                flaggedDefaults.add(profile);
            } else {
                others.add(profile);
            }
        }
    }

    private static Path resolve(Path root, String path, boolean relative) {
        return relative ? root.resolve(path) : Paths.get(path);
    }

    /** Profile directories of all installed Firefox flavours that contain a cookie database. */
    static List<Path> profilesWithCookies() {
        Set<Path> result = new LinkedHashSet<>();
        for (Path root : rootDirs()) {
            Path ini = root.resolve("profiles.ini");
            if (!Files.isRegularFile(ini)) {
                continue;
            }
            try {
                for (Path profile : parseProfilesIni(ini)) {
                    if (Files.isRegularFile(profile.resolve("cookies.sqlite"))) {
                        result.add(profile);
                    }
                }
            } catch (IOException e) {
                // unreadable profiles.ini: skip this root
            }
        }
        return new ArrayList<>(result);
    }
}
