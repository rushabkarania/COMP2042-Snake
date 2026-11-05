package com.comp2042;

import javafx.scene.media.AudioClip;
import java.net.URL;
import java.util.function.Consumer;
import java.util.function.Function;

/** Loads and reuses the short coin sound without making gameplay depend on audio. */
final class CoinSound {
    static final String RESOURCE = "/resources/views/res/coinSound.wav";
    private Runnable playback;
    private final Consumer<String> report;

    /** Loads the bundled sound once for this game controller. */
    CoinSound() {
        this(CoinSound.class.getResource(RESOURCE), source -> {
            AudioClip clip = new AudioClip(source);
            clip.setCycleCount(1);
            return clip::play;
        }, message -> System.err.println(message));
    }

    /**
     * Creates sound playback with replaceable audio loading for tests.
     * @param resource classpath URL, or null if missing
     * @param loader creates playback from the resource URL
     * @param report receives loading or synchronous playback errors
     */
    CoinSound(URL resource, Function<String, Runnable> loader, Consumer<String> report) {
        this.report = report;
        if (resource == null) {
            report.accept("Coin audio disabled: missing " + RESOURCE);
            return;
        }
        try {
            playback = loader.apply(resource.toExternalForm());
        } catch (RuntimeException failure) {
            report.accept("Coin audio disabled: " + failure.getMessage());
        }
    }

    /** Plays once per collection; disables playback after a synchronous audio failure. */
    void play() {
        if (playback == null) return;
        try {
            playback.run();
        } catch (RuntimeException failure) {
            playback = null;
            report.accept("Coin audio disabled: " + failure.getMessage());
        }
    }
}
