package com.comp2042;

/** Ordinary entry point for the packaged application; delegates to the JavaFX application. */
public final class Launcher {
    private Launcher() { }
    /**
     * Starts the JavaFX application through an ordinary Java entry point.
     *
     * @param args command-line arguments forwarded to Main
     */
    public static void main(String[] args) { Main.main(args); }
}
