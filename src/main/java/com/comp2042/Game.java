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

    /** Creates a board and validates its dimensions and difficulty. */
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

    /** Returns the stage, including when a bonus skips a threshold. */
    public int level() { return scorePoints >= 80 ? 3 : scorePoints >= 40 ? 2 : 1; }
    /** Returns remaining lives. */
    public int hearts() { return hearts; }
    /** Consumes at most one life; true means another attempt remains. */
    public boolean loseHeart() { hearts = Math.max(0, hearts - 1); return hearts > 0; }
    /** Starts a new run; saved personal records are managed separately. */
    public void resetScore() { scorePoints = 0; hearts = 3; }
    /** Adds nonnegative points without overflowing the score. */
    public void addPoints(int points) {
        if (points < 0) throw new IllegalArgumentException("Points must not be negative");
        scorePoints = (int) Math.min(Integer.MAX_VALUE, (long) scorePoints + points);
    }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Color getColor() { return Color.web("#c8dc98"); }
    public double getMultiplier() { return 24; }
    public int getScorePoints() { return scorePoints; }
    public boolean isPowerUp() { return powerUp; }
    public double getSpeed() { return speed; }
    public boolean isNoBorder() { return noBorder; }
}
