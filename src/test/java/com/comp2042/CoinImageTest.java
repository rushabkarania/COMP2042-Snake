package com.comp2042;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CoinImageTest {
    @Test void respawnsReuseDecodedArtworkForEachColour() {
        var obstacles = new Obstacle(20, 20);
        var snake = new Snake(20, 20, false, obstacles);
        var red = new Coin(20, 20, snake, obstacles);
        var gold = new GoldCoin(20, 20, snake, obstacles);
        assertFalse(red.getImage().isError());
        assertSame(red.getImage(), new Coin(20, 20, snake, obstacles).getImage());
        assertSame(gold.getImage(), new GoldCoin(20, 20, snake, obstacles).getImage());
        assertNotSame(red.getImage(), gold.getImage());
    }
}
