package com.comp2042;

import static javafx.scene.input.KeyCode.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class MovementTest {
    @Test void quickTurnsRunOnSuccessiveSteps() {
        var turns = new GameController.TurnBuffer();
        turns.offer(LEFT, LEFT);
        turns.offer(LEFT, UP);
        turns.offer(LEFT, DOWN);
        turns.offer(LEFT, RIGHT);
        turns.offer(LEFT, DOWN);
        assertEquals(UP, turns.take(LEFT));
        assertEquals(RIGHT, turns.take(UP));
        assertEquals(RIGHT, turns.take(RIGHT));
    }

    @Test void clearingInputDiscardsPendingTurns() {
        var turns = new GameController.TurnBuffer();
        turns.offer(LEFT, UP);
        turns.clear();
        assertEquals(LEFT, turns.take(LEFT));
    }

    @Test void halfwayFramesUseTheAdjacentCellAcrossEitherBorder() {
        assertEquals(3.5, GameView.interpolate(3, 4, 20, 0.5));
        assertEquals(-0.5, GameView.interpolate(0, 19, 20, 0.5));
        assertEquals(19.5, GameView.interpolate(19, 0, 20, 0.5));
        assertEquals(0, GameView.interpolate(19, 0, 20, 1));
    }

    @Test void shortSnakeKeepsItsBendThroughoutATurn() {
        var corner = new java.awt.Point(5, 5);
        var path = GameView.bodyPath(java.util.List.of(corner), new java.awt.Point(5, 4),
                java.util.List.of(new java.awt.Point(6, 5)), corner, 20, 20, 0.5);
        assertEquals(java.util.List.of(new javafx.geometry.Point2D(5.5, 5),
                new javafx.geometry.Point2D(5, 5), new javafx.geometry.Point2D(5, 4.5)), path);
    }
}
