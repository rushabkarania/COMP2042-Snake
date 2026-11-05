package com.comp2042;

import org.junit.jupiter.api.Test;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sound.sampled.AudioSystem;
import static org.junit.jupiter.api.Assertions.*;

class CoinSoundTest {
    private URL resource() { return CoinSound.class.getResource(CoinSound.RESOURCE); }

    @Test
    void bundledResourceIsReadablePcmAudio() throws Exception {
        assertNotNull(resource());
        try (var stream = AudioSystem.getAudioInputStream(resource())) {
            assertTrue(stream.getFrameLength() > 0);
            assertEquals(44100f, stream.getFormat().getSampleRate());
            assertEquals(16, stream.getFormat().getSampleSizeInBits());
            assertTrue(stream.readAllBytes().length > 0);
        }
    }

    @Test
    void loadsOnceAndPlaysOnceForEachRequest() {
        AtomicInteger loads = new AtomicInteger();
        AtomicInteger plays = new AtomicInteger();
        var errors = new ArrayList<String>();
        CoinSound sound = new CoinSound(resource(), source -> {
            assertEquals(resource().toExternalForm(), source);
            loads.incrementAndGet();
            return () -> plays.incrementAndGet();
        }, errors::add);
        assertEquals(0, plays.get());
        sound.play(); sound.play(); sound.play();
        assertEquals(1, loads.get());
        assertEquals(3, plays.get());
        assertTrue(errors.isEmpty());
    }

    @Test
    void missingResourceDoesNotLoadOrInterruptGameplay() {
        var errors = new ArrayList<String>();
        CoinSound sound = new CoinSound(null, source -> {
            fail("Missing resource must not be loaded");
            return null;
        }, errors::add);
        assertDoesNotThrow(sound::play);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("missing"));
    }

    @Test
    void loadingFailureLeavesPlaybackDisabled() {
        var errors = new ArrayList<String>();
        CoinSound sound = new CoinSound(resource(), source -> {
            throw new IllegalArgumentException("Unsupported audio");
        }, errors::add);
        assertDoesNotThrow(sound::play);
        assertDoesNotThrow(sound::play);
        assertEquals(1, errors.size());
    }

    @Test
    void playbackFailureIsReportedOnceAndDisablesFurtherAttempts() {
        var errors = new ArrayList<String>();
        AtomicInteger attempts = new AtomicInteger();
        CoinSound sound = new CoinSound(resource(), source -> () -> {
            attempts.incrementAndGet();
            throw new IllegalStateException("Audio unavailable");
        }, errors::add);
        assertDoesNotThrow(sound::play);
        assertDoesNotThrow(sound::play);
        assertEquals(1, attempts.get());
        assertEquals(1, errors.size());
    }
}
