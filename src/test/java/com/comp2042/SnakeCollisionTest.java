package com.comp2042;

import java.awt.Point;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class SnakeCollisionTest {
    private final Obstacle obstacles = new Obstacle(12, 8);

    private Snake snake(boolean wraps) {
        return new Snake(12, 8, wraps, obstacles);
    }

    private static Point destination(Point p, char dir) {
        Point next = new Point(p);
        switch (dir) {
            case 'U' -> next.translate(0, -1);
            case 'R' -> next.translate(1, 0);
            case 'D' -> next.translate(0, 1);
            case 'L' -> next.translate(-1, 0);
            default -> throw new IllegalArgumentException();
        }
        return next;
    }

    private static void goToEdge(Snake snake, char dir) {
        int steps = switch (dir) {
            case 'U' -> 4;
            case 'R' -> 5;
            case 'D' -> 3;
            case 'L' -> 6;
            default -> throw new IllegalArgumentException();
        };
        for (int i = 0; i < steps; i++) {
            assertFalse(snake.move(dir, null));
            assertFalse(snake.getDead());
        }
    }

    @ParameterizedTest
    @CsvSource({"U,6,3,0", "R,7,4,90", "D,6,5,180", "L,5,4,270"})
    void normalMovePreservesLength(char dir, int x, int y, int rotation) {
        Snake snake = snake(false);
        assertFalse(snake.move(dir, null));
        assertEquals(new Point(x, y), snake.getHead());
        assertEquals(List.of(new Point(6, 4)), snake.getBody());
        assertEquals(rotation, snake.getSnakeDir());
        assertFalse(snake.getDead());
    }

    @ParameterizedTest
    @CsvSource({"U,true", "R,true", "D,true", "L,true",
                "U,false", "R,false", "D,false", "L,false"})
    void wrapsUsingEitherModeOrPowerUp(char dir, boolean easyMode) {
        Snake snake = snake(easyMode);
        snake.ignoreBorders(!easyMode);
        goToEdge(snake, dir);
        Point oldHead = snake.getHead();
        Point expected = destination(oldHead, dir);
        expected.setLocation(Math.floorMod(expected.x, 12), Math.floorMod(expected.y, 8));
        assertFalse(snake.move(dir, null));
        assertFalse(snake.getDead());
        assertEquals(expected, snake.getHead());
        assertEquals(List.of(oldHead), snake.getBody());
    }

    @ParameterizedTest
    @CsvSource({"U,true", "R,true", "D,true", "L,true",
                "U,false", "R,false", "D,false", "L,false"})
    void obstacleAtWrappedDestinationKillsWithoutChangingPositions(char dir, boolean easyMode) {
        Snake snake = snake(easyMode);
        snake.ignoreBorders(!easyMode);
        goToEdge(snake, dir);
        Point blocked = destination(snake.getHead(), dir);
        blocked.setLocation(Math.floorMod(blocked.x, 12), Math.floorMod(blocked.y, 8));
        obstacles.getObstacles().add(blocked);
        assertFatalMovePreservesState(snake, dir, blocked);
    }

    @ParameterizedTest
    @ValueSource(chars = {'U', 'R', 'D', 'L'})
    void wallDeathPreservesPositions(char dir) {
        Snake snake = snake(false);
        goToEdge(snake, dir);
        assertFatalMovePreservesState(snake, dir, null);
    }

    @ParameterizedTest
    @ValueSource(chars = {'U', 'R', 'D', 'L'})
    void ordinaryObstacleDeathPreservesPositions(char dir) {
        Snake snake = snake(false);
        Point blocked = destination(snake.getHead(), dir);
        obstacles.getObstacles().add(blocked);
        assertFatalMovePreservesState(snake, dir, null);
    }

    private static void assertFatalMovePreservesState(Snake snake, char dir, Point coin) {
        Point head = snake.getHead();
        List<Point> body = snake.getBody();
        int rotation = snake.getSnakeDir();
        assertFalse(snake.move(dir, coin));
        assertTrue(snake.getDead());
        assertEquals(head, snake.getHead());
        assertEquals(body, snake.getBody());
        assertEquals(rotation, snake.getSnakeDir());
    }

    @Test
    void deadSnakeCannotMoveOrGrow() {
        Snake snake = snake(false);
        obstacles.getObstacles().add(new Point(5, 4));
        assertFatalMovePreservesState(snake, 'L', null);
        for (char dir : new char[]{'U', 'R', 'D', 'L'}) {
            assertFatalMovePreservesState(snake, dir, destination(snake.getHead(), dir));
        }
    }

    @Test
    void invalidDirectionThrowsBeforeChangingState() {
        Snake snake = snake(false);
        Point head = snake.getHead();
        List<Point> body = snake.getBody();
        assertThrows(IllegalArgumentException.class, () -> snake.move('?', null));
        assertEquals(head, snake.getHead());
        assertEquals(body, snake.getBody());
        assertEquals(270, snake.getSnakeDir());
        assertFalse(snake.getDead());
    }

    @Test
    void coinCollectionGrowsExactlyOnceThenNormalMoveKeepsLength() {
        Snake snake = snake(false);
        Point coin = new Point(5, 4);
        assertTrue(snake.move('L', coin));
        assertEquals(List.of(new Point(7, 4), new Point(6, 4)), snake.getBody());
        assertEquals(new Point(5, 4), coin);
        assertFalse(snake.move('U', null));
        assertEquals(2, snake.getBody().size());
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5, 6})
    void interiorCollisionIsDetectedForEveryBodyLength(int bodyLength) {
        Snake snake = snake(false);
        for (int i = 1; i < bodyLength; i++) {
            assertTrue(snake.move('L', destination(snake.getHead(), 'L')));
        }
        assertEquals(bodyLength, snake.getBody().size());
        assertFatalMovePreservesState(snake, 'R', null);
    }

    private Snake curledSnake() {
        Snake snake = snake(false);
        assertTrue(snake.move('U', new Point(6, 3)));
        assertTrue(snake.move('R', new Point(7, 3)));
        return snake; // Tail (7,4), body (6,4),(6,3), head (7,3).
    }

    @Test
    void enteringVacatingTailCellIsAllowed() {
        Snake snake = curledSnake();
        assertFalse(snake.move('D', null));
        assertFalse(snake.getDead());
        assertEquals(new Point(7, 4), snake.getHead());
        assertEquals(List.of(new Point(6, 4), new Point(6, 3), new Point(7, 3)), snake.getBody());
    }

    @Test
    void enteringTailCellIsFatalWhenGrowthWouldKeepTail() {
        Snake snake = curledSnake();
        // Defensive scenario: normal spawning should never put a coin on the tail.
        assertFatalMovePreservesState(snake, 'D', new Point(7, 4));
    }

    @Test
    void disablingPowerUpRestoresWallCollision() {
        Snake snake = snake(false);
        snake.ignoreBorders(true);
        goToEdge(snake, 'U');
        snake.ignoreBorders(false);
        assertFatalMovePreservesState(snake, 'U', null);
    }
}
