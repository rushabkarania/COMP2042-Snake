package com.comp2042;

/**
 * A five-point pickup that activates temporary border wrapping in GameSession.
 */
public class BlueCoin extends Coin {

    /**
     * Places this pickup on a cell clear of the snake and obstacles.
     *
     * @param x board width in cells
     * @param y board height in cells
     * @param snek snake whose occupied cells must be avoided
     * @param obstacle current collision cells to avoid
     * @throws IllegalStateException if the board has no free cell
     */
    public BlueCoin(int x, int y, Snake snek, Obstacle obstacle) {
        super(x, y, snek, obstacle, Sprite.BLUE);
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

}
