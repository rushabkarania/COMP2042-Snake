package com.comp2042;

/** A mint scissors pickup: awards five points and removes up to three tail segments. */
public final class ShrinkCoin extends Coin {
    public ShrinkCoin(int width, int height, Snake snake, Obstacle obstacles) {
        super(width, height, snake, obstacles, Sprite.SHRINK);
    }
}
