package com.comp2042;

import java.awt.Point;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CoinPlacementTest {
    @Test
    void choosesOnlyRemainingCellBesideSnakeAndObstacle() {
        Obstacle obstacles = new Obstacle(4, 1);
        Snake snake = new Snake(4, 1, false, obstacles);
        obstacles.getObstacles().add(new Point(0, 0));
        assertEquals(new Point(1, 0), Coin.choosePosition(4, 1, snake, obstacles).orElseThrow());
    }

    @Test
    void collectingFinalCellContinuesWithBonusAndSpaceForAnotherCoin() throws Exception {
        Obstacle obstacles = new Obstacle(4, 1);
        Snake snake = new Snake(4, 1, false, obstacles);
        Game game = new Game(4, 1, true, 'N');
        obstacles.getObstacles().add(new Point(0, 0));
        var path = java.nio.file.Files.createTempDirectory("snake-full-board").resolve("best.txt");
        var session = new GameSession(game, obstacles, new HighScoreStore(path), snake,
                new Coin(new Point(1, 0), Coin.Sprite.RED));
        session.resume();
        assertTrue(session.step('L'));
        assertEquals(GameSession.State.RUNNING, session.state());
        assertEquals(30, game.getScorePoints());
        assertEquals(1, snake.getBody().size());
        assertFalse(snake.getHead().equals(session.coin().getPos()));
        assertFalse(snake.getBody().contains(session.coin().getPos()));
        assertFalse(obstacles.getObstacles().contains(session.coin().getPos()));
        assertTrue(session.notice().contains("Garden cleared"));
    }
}
