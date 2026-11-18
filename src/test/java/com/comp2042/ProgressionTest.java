package com.comp2042;

import java.awt.Point;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProgressionTest {
    @Test void crossesThresholdsEvenWhenPickupSkipsExactScore() {
        Game game = new Game(20, 20, true, 'N');
        game.addPoints(39);
        assertEquals(1, game.level());
        game.addPoints(5);
        assertEquals(2, game.level());
        game.addPoints(36);
        assertEquals(3, game.level());
    }

    @Test void thirdCollisionEndsRunAndResetRestoresLivesAndLevel() {
        Game game = new Game(20, 20, true, 'N');
        game.addPoints(85);
        assertTrue(game.loseHeart());
        assertTrue(game.loseHeart());
        assertFalse(game.loseHeart());
        assertEquals(85, game.getScorePoints());
        game.resetScore();
        assertEquals(3, game.hearts());
        assertEquals(1, game.level());
    }

    @Test void frozenPatrolResumesAndReversesAtCoin() {
        Obstacle obstacles = new Obstacle(20, 20);
        Point block = new Point(4, 4);
        obstacles.getObstacles().add(block);
        Snake snake = new Snake(20, 20, false, obstacles);
        Point coin = new Point(6, 4);
        obstacles.startPatrol();
        obstacles.freezePatrol();
        for (int i = 0; i < 12; i++) obstacles.advancePatrol(snake, coin);
        assertEquals(new Point(4, 4), block);
        for (int i = 0; i < 2; i++) obstacles.advancePatrol(snake, coin);
        assertEquals(new Point(5, 4), block);
        for (int i = 0; i < 4; i++) obstacles.advancePatrol(snake, coin);
        assertEquals(new Point(4, 4), block);
    }

    @Test void respawnHasClearBodyAndFirstStep() {
        Obstacle obstacles = new Obstacle(20, 20);
        obstacles.getObstacles().add(new Point(1, 10));
        Snake snake = new Snake(20, 20, false, obstacles);
        Point coin = new Point(4, 10);
        snake.findRespawn(coin);
        assertFalse(snake.getBody().contains(coin));
        assertFalse(snake.move('L', coin));
        assertFalse(snake.getDead());
    }
}
