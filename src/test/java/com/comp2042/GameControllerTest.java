package com.comp2042;

import javafx.scene.input.KeyCode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameControllerTest {

    @ParameterizedTest
    @CsvSource({
            "UP, DOWN",
            "DOWN, UP",
            "LEFT, RIGHT",
            "RIGHT, LEFT"
    })
    void rejectsOppositeDirections(
            KeyCode currentDirection,
            KeyCode requestedDirection) {

        assertFalse(GameController.isDirectionAllowed(
                currentDirection, requestedDirection));
    }

    @ParameterizedTest
    @CsvSource({
            "UP, LEFT",
            "UP, RIGHT",
            "DOWN, LEFT",
            "DOWN, RIGHT",
            "LEFT, UP",
            "LEFT, DOWN",
            "RIGHT, UP",
            "RIGHT, DOWN"
    })
    void allowsPerpendicularTurns(
            KeyCode currentDirection,
            KeyCode requestedDirection) {

        assertTrue(GameController.isDirectionAllowed(
                currentDirection, requestedDirection));
    }

    @ParameterizedTest
    @CsvSource({
            "UP, UP",
            "DOWN, DOWN",
            "LEFT, LEFT",
            "RIGHT, RIGHT"
    })
    void allowsContinuingInSameDirection(
            KeyCode currentDirection,
            KeyCode requestedDirection) {

        assertTrue(GameController.isDirectionAllowed(
                currentDirection, requestedDirection));
    }

    @ParameterizedTest
    @CsvSource({
            "LEFT, P",
            "LEFT, TAB",
            "LEFT, ESCAPE",
            "LEFT, SPACE"
    })
    void rejectsNonArrowKeysAsDirections(
            KeyCode currentDirection,
            KeyCode requestedDirection) {

        assertFalse(GameController.isDirectionAllowed(
                currentDirection, requestedDirection));
    }
}