package com.comp2042;

import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class HighScoreStoreTest {
    @TempDir Path directory;
    @Test void recordsSurviveReloadAndNeverDecrease() {
        Path file = directory.resolve("nested/best.txt");
        var store = new HighScoreStore(file);
        assertEquals(0, store.best());
        store.record(40); store.record(5);
        assertEquals(40, new HighScoreStore(file).best());
        store.record(85);
        assertEquals(85, new HighScoreStore(file).best());
        assertTrue(store.available());
    }
    @Test void corruptAndNegativeRecordsAreRecoverable() throws Exception {
        Path file = directory.resolve("best.txt");
        Files.writeString(file, "not a score");
        var store = new HighScoreStore(file);
        assertEquals(0, store.best());
        store.record(15);
        assertEquals(15, new HighScoreStore(file).best());
        Files.writeString(file, "-99");
        assertEquals(0, new HighScoreStore(file).best());
    }
    @Test void unwritableLocationRetainsRecordInMemory() throws Exception {
        Path parent = directory.resolve("a-file"); Files.writeString(parent, "blocking parent");
        var store = new HighScoreStore(parent.resolve("best.txt"));
        assertDoesNotThrow(() -> store.record(100));
        assertEquals(100, store.best());
        assertFalse(store.available());
    }
    @Test void independentStoreFilesDoNotOverwriteEachOther() {
        var easy = new HighScoreStore(directory.resolve("easy.txt"));
        var hard = new HighScoreStore(directory.resolve("hard.txt"));
        easy.record(100); hard.record(20);
        assertEquals(100, easy.best()); assertEquals(20, hard.best());
    }
}
