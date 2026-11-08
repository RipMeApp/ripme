package com.rarchives.ripme.utils.browser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * A private, temporary copy of a browser's SQLite database. Browsers keep their cookie
 * database locked or mid-write while running, so it is never opened in place; this also
 * guarantees the browser's data is never modified.
 */
final class TempDatabase implements AutoCloseable {

    private final Path directory;
    private final Path database;

    private TempDatabase(Path directory, Path database) {
        this.directory = directory;
        this.database = database;
    }

    static TempDatabase copyOf(Path source) throws IOException {
        Path dir = Files.createTempDirectory("ripme-cookies-");
        try {
            Path copy = dir.resolve("cookies.db");
            Files.copy(source, copy, StandardCopyOption.REPLACE_EXISTING);
            // Recent writes may still sit in the write-ahead log.
            Path wal = source.resolveSibling(source.getFileName() + "-wal");
            if (Files.isRegularFile(wal)) {
                Files.copy(wal, dir.resolve("cookies.db-wal"), StandardCopyOption.REPLACE_EXISTING);
            }
            return new TempDatabase(dir, copy);
        } catch (IOException | RuntimeException e) {
            deleteRecursively(dir);
            throw e;
        }
    }

    Connection open() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + database.toAbsolutePath());
    }

    @Override
    public void close() {
        deleteRecursively(directory);
    }

    private static void deleteRecursively(Path dir) {
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) {
                    // best effort cleanup of our own temp files
                }
            });
        } catch (IOException ignored) {
            // best effort cleanup of our own temp files
        }
    }

    /** Builds "?,?,?" with the given number of placeholders. */
    static String placeholders(int count) {
        return String.join(",", java.util.Collections.nCopies(count, "?"));
    }
}
