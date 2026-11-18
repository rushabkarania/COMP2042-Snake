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

    /** Loads a record from a file, treating corrupt content as an empty record. */
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

    /** Uses the user's application data folder, overridable for portable runs and tests. */
    public static HighScoreStore forGame(Game game) {
        String directory = System.getProperty("snake.data.dir",
                Path.of(System.getProperty("user.home"), ".comp2042-snake").toString());
        return new HighScoreStore(Path.of(directory, "best.txt"));
    }

    public int best() { return best; }
    public boolean available() { return available; }

    /** Keeps a higher record in memory and atomically replaces the saved file where supported. */
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
