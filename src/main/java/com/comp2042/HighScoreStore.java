package com.comp2042;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Persists the progressive run personal best; storage failures never interrupt play. */
public final class HighScoreStore {
    private final Path file;
    private int best;
    private boolean available = true;

    /**
     * Loads a record; malformed or negative content becomes zero and access failures disable availability.
     *
     * @param file record file path, resolved to an absolute path
     */
    public HighScoreStore(Path file) {
        this.file = file.toAbsolutePath();
        try {
            if (Files.exists(this.file)) best = Math.max(0, Integer.parseInt(Files.readString(this.file).trim()));
        } catch (NumberFormatException failure) {
            best = 0;
        } catch (IOException | SecurityException failure) {
            available = false;
        }
    }

    /**
     * Opens best.txt in snake.data.dir, defaulting to .comp2042-snake under the user home.
     *
     * @param game reserved game context; currently unused because runs share one record
     * @return record store for the configured directory
     */
    public static HighScoreStore forGame(Game game) {
        String directory = System.getProperty("snake.data.dir",
                Path.of(System.getProperty("user.home"), ".comp2042-snake").toString());
        return new HighScoreStore(Path.of(directory, "best.txt"));
    }

    /**
     * Returns the best score retained in memory.
     *
     * @return nonnegative personal best
     */
    public int best() { return best; }
    /**
     * Reports whether loading or the latest attempted write succeeded.
     *
     * @return false after an I/O or security failure
     */
    public boolean available() { return available; }

    /**
     * Retains a higher score and saves it, using atomic replacement where supported; retries unavailable storage.
     *
     * @param score candidate score; values below the existing best do not reduce it
     */
    public void record(int score) {
        if (score <= best && available) return;
        best = Math.max(best, score);
        Path temporary = null;
        try {
            Files.createDirectories(file.getParent());
            temporary = Files.createTempFile(file.getParent(), "record-", ".tmp");
            Files.writeString(temporary, Integer.toString(best));
            try {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
            available = true;
        } catch (IOException | SecurityException failure) {
            available = false;
        } finally {
            if (temporary != null) {
                try { Files.deleteIfExists(temporary); } catch (IOException | SecurityException ignored) { /* Best remains in memory. */ }
            }
        }
    }
}
