package com.comp2042;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameResetTest {
    @Test void resetRestoresLivesAndPointsAfterAnArbitrarilyLongRun() {
        Game game = new Game(4, 3, true, 'N');
        game.addPoints(10000);
        game.loseHeart();
        assertEquals(3, game.level());
        game.resetScore();
        assertEquals(0, game.getScorePoints());
        assertEquals(3, game.hearts());
        assertEquals(1, game.level());
    }
    @Test void invalidSettingsAndNegativePointsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Game(0, 20, true, 'N'));
        assertThrows(IllegalArgumentException.class, () -> new Game(20, 20, true, '?'));
        assertThrows(IllegalArgumentException.class, () -> new Game(20,20,true,'N').addPoints(-1));
    }
    @Test void scoreCannotOverflowIntoANegativeLevel() {
        Game game = new Game(20,20,true,'N');
        game.addPoints(Integer.MAX_VALUE); game.addPoints(15);
        assertEquals(Integer.MAX_VALUE, game.getScorePoints());
        assertEquals(3, game.level());
    }
}
