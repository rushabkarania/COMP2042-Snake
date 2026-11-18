package com.comp2042;

/** A mint scissors pickup: awards five points and removes up to three tail segments. */
public final class ShrinkCoin extends Coin {
    /**
     * Places a shrink pickup; GameSession removes tail cells when it is collected.
     *
     * @param width board width in cells
     * @param height board height in cells
     * @param snake snake whose occupied cells must be avoided
     * @param obstacles current collision cells
     * @throws IllegalStateException if the board has no free cell
     */
    public ShrinkCoin(int width, int height, Snake snake, Obstacle obstacles) {
        super(width, height, snake, obstacles, Sprite.SHRINK);
    }
}
