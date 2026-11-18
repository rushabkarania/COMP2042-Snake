package com.comp2042;

/**
 * A five-point pickup that activates the timed rush effect in GameSession.
 */
public class RainbowCoin extends Coin {

    /**
     * Places this pickup on a cell clear of the snake and obstacles.
     *
     * @param x board width in cells
     * @param y board height in cells
     * @param snek snake whose occupied cells must be avoided
     * @param obstacle current collision cells to avoid
     * @throws IllegalStateException if the board has no free cell
     */
    public RainbowCoin(int x, int y, Snake snek, Obstacle obstacle) {
        super(x, y, snek, obstacle, Sprite.RAINBOW);
    }

    /**
     * Returns the score awarded for this pickup.
     *
     * @return 5 points
     */
    public int getPoints() {
        int points = 5;
        return points;
    }

    /**
     * Returns the legacy speed bonus; the current session applies its own capped rush speed.
     *
     * @return legacy bonus of 5; not used by the current session speed calculation
     */
    public double getSpeedBonus() {
        double speedBonus = 5;
        return speedBonus;
    }

}
