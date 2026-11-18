package com.comp2042;

import javafx.scene.paint.Color;

/** Score, lives and difficulty settings for an endless run. */
public class Game {
    private final int width;
    private final int height;
    private final double speed;
    private final boolean noBorder;
    private final boolean powerUp;
    private int hearts;
    private int scorePoints;

    /**
     * Creates a board with three lives and the selected base difficulty.
     *
     * @param width board width in cells
     * @param height board height in cells
     * @param powerUp whether the collectible sequence includes power-ups
     * @param difficulty E for easy, N for normal or H for hard
     * @throws IllegalArgumentException if a dimension is not positive or the difficulty is unknown
     */
    public Game(int width, int height, boolean powerUp, char difficulty) {
        if (width < 1 || height < 1) throw new IllegalArgumentException("Board dimensions must be positive");
        this.width = width;
        this.height = height;
        this.powerUp = powerUp;
        speed = switch (difficulty) {
            case 'E' -> 6.5;
            case 'N' -> 8.5;
            case 'H' -> 11;
            default -> throw new IllegalArgumentException("Unknown difficulty: " + difficulty);
        };
        noBorder = difficulty == 'E';
        resetScore();
    }

    /**
     * Returns the stage using score thresholds of 40 and 80.
     *
     * @return 1, 2 or 3
     */
    public int level() { return scorePoints >= 80 ? 3 : scorePoints >= 40 ? 2 : 1; }
    /**
     * Returns the remaining lives.
     *
     * @return number of lives, from 0 to 3
     */
    public int hearts() { return hearts; }
    /**
     * Consumes one life without going below zero.
     *
     * @return true if at least one life remains
     */
    public boolean loseHeart() { hearts = Math.max(0, hearts - 1); return hearts > 0; }
    /** Starts a new run; saved personal records are managed separately. */
    public void resetScore() { scorePoints = 0; hearts = 3; }
    /**
     * Adds nonnegative points, saturating at Integer.MAX_VALUE.
     *
     * @param points points to add
     * @throws IllegalArgumentException if points is negative
     */
    public void addPoints(int points) {
        if (points < 0) throw new IllegalArgumentException("Points must not be negative");
        scorePoints = (int) Math.min(Integer.MAX_VALUE, (long) scorePoints + points);
    }
    /**
     * Returns the board width.
     *
     * @return width in cells
     */
    public int getWidth() { return width; }
    /**
     * Returns the board height.
     *
     * @return height in cells
     */
    public int getHeight() { return height; }
    /**
     * Returns the legacy board background colour.
     *
     * @return light green background colour
     */
    public Color getColor() { return Color.web("#c8dc98"); }
    /**
     * Returns the legacy rendering scale.
     *
     * @return 24 pixels per cell
     */
    public double getMultiplier() { return 24; }
    /**
     * Returns the current run score.
     *
     * @return nonnegative score
     */
    public int getScorePoints() { return scorePoints; }
    /**
     * Reports whether power-up pickups are enabled.
     *
     * @return true when power-ups are enabled
     */
    public boolean isPowerUp() { return powerUp; }
    /**
     * Returns the base speed selected by difficulty.
     *
     * @return movement steps per second before progression and effects
     */
    public double getSpeed() { return speed; }
    /**
     * Reports whether easy difficulty enables permanent border wrapping.
     *
     * @return true for easy difficulty
     */
    public boolean isNoBorder() { return noBorder; }
}
