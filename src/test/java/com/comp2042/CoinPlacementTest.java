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
    void collectingLastReachableCoinFillsBoardAndMeetsWinTarget() {
        Obstacle obstacles = new Obstacle(4, 1);
        Snake snake = new Snake(4, 1, false, obstacles);
        Game game = new Game(4, 1, true, 'N');
        obstacles.getObstacles().add(new Point(0, 0));
        game.decMaxScore(1);
        assertFalse(game.gameWon());
        Point finalCoin = Coin.choosePosition(4, 1, snake, obstacles).orElseThrow();
        assertTrue(snake.move('L', finalCoin));
        game.incScore();
        game.addPoints(5);
        assertTrue(game.gameWon());
        assertEquals(5, game.getScorePoints());
        assertTrue(Coin.choosePosition(4, 1, snake, obstacles).isEmpty());
    }
}
