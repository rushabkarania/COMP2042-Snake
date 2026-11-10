package com.comp2042;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameResetTest {
    @Test
    void newRoundRestoresOriginalWinTargetAndClearsPoints() {
        Game game = new Game(4, 3, true, 'N');
        game.decMaxScore(3);
        game.addPoints(25);
        game.incScore();
        game.resetScore();

        assertEquals(0, game.getScorePoints());
        for (int collected = 0; collected < 10; collected++) {
            assertFalse(game.gameWon());
            game.incScore();
        }
        assertTrue(game.gameWon());
    }
}
