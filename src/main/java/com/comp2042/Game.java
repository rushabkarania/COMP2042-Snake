package com.comp2042;

import javafx.scene.paint.Color;

public class Game {
    private Color color = Color.web("feffe4");
    private int width;
    private int height;
    private int hearts;
    private int score;

    /** Current stage, based on total points earned this run. */
    public int level() {
        if (scorePoints >= 80) return 3;
        if (scorePoints >= 40) return 2;
        return 1;
    }

    public int hearts() {
        return hearts;
    }

    /** Spend a heart and report whether the player can respawn. */
    public boolean loseHeart() {
        hearts = Math.max(0, hearts - 1);
        return hearts > 0;
    }
    private int maxScore;
    private double multiplier;
    private int scorePoints;
    private double speed;
    private boolean powerUp;
    private boolean noBorder;

    public Game(int x, int y, boolean powerUp, char difficulty) {
        if (difficulty == 'E') {
            speed = 8.5;
            noBorder = true;
        } else if (difficulty == 'N') {
            speed = 8.5;
            noBorder = false;
        } else if (difficulty == 'H') {
            speed = 13;
            noBorder = false;
        }
        this.powerUp = powerUp;
        width = x;
        height = y;
        resetScore();
        this.powerUp = powerUp;
        if (height > 50) {
            multiplier = 10;
        } else {
            multiplier = 20;
        }
    }

    public boolean gameWon() {
        return score >= maxScore;
    }

    public void incScore() {
        score++;
    }

    public void decMaxScore(int num) {
        this.maxScore -= num;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void resetScore() {
        maxScore = width * height - 2;
        score = 0;
        scorePoints = 0;
        hearts = 3;
    }

    public Color getColor() {
        return color;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void addPoints(int n) {
        scorePoints += n;
    }

    public int getScorePoints() {
        return scorePoints;
    }

    public boolean isPowerUp() {
        return powerUp;
    }

    public double getSpeed() {
        return speed;
    }

    public boolean isNoBorder() {
        return noBorder;
    }
}
