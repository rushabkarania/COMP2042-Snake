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

    @Test void stageTwoSchedulesHedgesBeforeTheyBecomeSolid() {
        Obstacle obstacles=new Obstacle(20,20);Snake snake=new Snake(20,20,false,obstacles);
        Point coin=new Point(17,17);obstacles.requestLayout(40,snake,coin);
        assertTrue(obstacles.getObstacles().isEmpty());assertFalse(obstacles.pendingCells().isEmpty());
        for(int i=0;i<12;i++)obstacles.advance(snake,coin,0);
        assertFalse(obstacles.getObstacles().isEmpty());assertTrue(obstacles.pendingCells().isEmpty());
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
