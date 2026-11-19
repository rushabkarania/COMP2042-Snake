package com.comp2042;

/**
 * A bonus pickup worth fifteen points.
 */
public class GoldCoin extends Coin {

    /**
     * Places this pickup on a cell clear of the snake and obstacles.
     *
     * @param x board width in cells
     * @param y board height in cells
     * @param snek snake whose occupied cells must be avoided
     * @param obstacle current collision cells to avoid
     * @throws IllegalStateException if the board has no free cell
     */
    public GoldCoin(int x, int y, Snake snek, Obstacle obstacle) {
        super(x, y, snek, obstacle, Sprite.GOLD);
    }

    /**
     * Returns the score awarded for this pickup.
     *
     * @return 15 points
     */
    public int getPoints() {
        int points = 15;
        return points;
    }
}
