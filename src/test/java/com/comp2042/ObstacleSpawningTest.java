package com.comp2042;

import org.junit.jupiter.api.Test;
import java.awt.Point;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class ObstacleSpawningTest {
    @Test
    void repeatedSilverMovesOneBlockWithoutReducingWinTargetAgain() {
        Obstacle field = new Obstacle(20, 20);
        Snake snake = new Snake(20, 20, false, field);
        assertEquals(4, field.onSilverPickup(snake, snake.getHead(), 4));
        for (int pickup = 0; pickup < 20; pickup++) {
            var before = new HashSet<>(field.getObstacles());
            assertEquals(0, field.onSilverPickup(snake, snake.getHead(), 4));
            var after = new HashSet<>(field.getObstacles());
            assertEquals(4, after.size());
            after.removeAll(before);
            assertEquals(1, after.size());
            assertTrue(after.iterator().next().distanceSq(snake.getHead()) >= 16);
        }
    }

    @Test
    void keepsExistingBlockWhenNoSafeReplacementExists() {
        Obstacle field = new Obstacle(6, 4);
        Snake snake = new Snake(6, 4, false, field);
        Point old = new Point(0, 0);
        field.getObstacles().add(old);
        assertEquals(0, field.onSilverPickup(snake, snake.getHead(), 4));
        assertEquals(java.util.List.of(old), field.getObstacles());
    }

    @Test
    void standardWaveHasOneObstacleInEachQuadrantWithClearance() {
        Obstacle field = new Obstacle(20, 20);
        Snake snake = new Snake(20, 20, false, field);
        Point head = snake.getHead();
        for (int wave = 0; wave < 100; wave++) {
            field.reset();
            assertEquals(4, field.placeObstacles(snake, head, 22));
            var regions = new HashSet<Integer>();
            for (Point cell : field.getObstacles()) {
                assertTrue(cell.x >= 1 && cell.x <= 18 && cell.y >= 3 && cell.y <= 17);
                assertTrue(cell.distanceSq(head) >= 16);
                assertFalse(snake.getBody().contains(cell));
                assertTrue(regions.add((cell.x < 10 ? 0 : 1) + (cell.y < 10 ? 0 : 2)));
            }
            var cells = field.getObstacles();
            for (int i = 0; i < cells.size(); i++) {
                for (int j = i + 1; j < cells.size(); j++) {
                    assertTrue(cells.get(i).distanceSq(cells.get(j)) >= 16);
                }
            }
        }
    }

    @Test
    void addsOnlyMissingRegionsOnRectangularBoard() {
        Obstacle field = new Obstacle(24, 18);
        Snake snake = new Snake(24, 18, false, field);
        Point coin = new Point(4, 5);
        field.getObstacles().add(new Point(3, 4));
        assertEquals(3, field.placeObstacles(snake, coin, 20));
        assertFalse(field.getObstacles().contains(coin));
        assertEquals(0, field.placeObstacles(snake, coin, 20));
    }

    @Test
    void skipsWaveWhenBoardHasNoSuitableSpace() {
        Obstacle field = new Obstacle(6, 4);
        Snake snake = new Snake(6, 4, false, field);
        assertEquals(0, field.placeObstacles(snake, snake.getHead(), 4));
        assertTrue(field.getObstacles().isEmpty());
    }

    @Test
    void winTargetAccountsForEachNewObstacleOnce() {
        Game game = new Game(4, 3, true, 'N');
        game.decMaxScore(0);
        game.decMaxScore(2);
        game.decMaxScore(1);
        for (int i = 0; i < 7; i++) {
            assertFalse(game.gameWon());
            game.incScore();
        }
        assertTrue(game.gameWon());
    }
}
