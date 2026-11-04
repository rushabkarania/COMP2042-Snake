package com.comp2042;

import java.awt.Point;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SnakeSpriteSelectorTest {

    @ParameterizedTest
    @CsvSource({
            "5, 4, 5, 6, false, 0",
            "4, 5, 6, 5, false, 90",
            "4, 5, 5, 6, true, 0",
            "4, 5, 5, 4, true, 90",
            "6, 5, 5, 4, true, 180",
            "6, 5, 5, 6, true, 270"
    })
    void selectsSpriteForStraightSegmentsAndCorners(
            int previousX, int previousY,
            int nextX, int nextY,
            boolean expectedCorner, int expectedRotation) {

        Point previous = new Point(previousX, previousY);
        Point current = new Point(5, 5);
        Point next = new Point(nextX, nextY);

        SnakeSpriteSelector.Sprite expected =
                new SnakeSpriteSelector.Sprite(
                        expectedCorner, expectedRotation);

        assertEquals(expected, SnakeSpriteSelector.select(
                previous, current, next, 20, 20));

        // Swapping the neighbours must preserve the visual connections.
        assertEquals(expected, SnakeSpriteSelector.select(
                next, current, previous, 20, 20));
    }

    @ParameterizedTest
    @CsvSource({
            "0, 5, 19, 5, 1, 5, false, 90",
            "19, 5, 18, 5, 0, 5, false, 90",
            "5, 0, 5, 19, 5, 1, false, 0",
            "5, 19, 5, 18, 5, 0, false, 0",
            "0, 5, 19, 5, 0, 4, true, 90",
            "19, 5, 0, 5, 19, 6, true, 270",
            "5, 0, 5, 19, 6, 0, true, 180",
            "5, 19, 5, 0, 4, 19, true, 0"
    })
    void selectsSpriteAcrossBoardBorders(
            int currentX, int currentY,
            int previousX, int previousY,
            int nextX, int nextY,
            boolean expectedCorner, int expectedRotation) {

        SnakeSpriteSelector.Sprite expected =
                new SnakeSpriteSelector.Sprite(
                        expectedCorner, expectedRotation);

        assertEquals(expected, SnakeSpriteSelector.select(
                new Point(previousX, previousY),
                new Point(currentX, currentY),
                new Point(nextX, nextY),
                20, 20));
    }

    @Test
    void usesBoardWidthForHorizontalWrapping() {
        assertEquals(
                new SnakeSpriteSelector.Sprite(false, 90),
                SnakeSpriteSelector.select(
                        new Point(11, 3),
                        new Point(0, 3),
                        new Point(1, 3),
                        12, 8));
    }

    @Test
    void usesBoardHeightForVerticalWrapping() {
        assertEquals(
                new SnakeSpriteSelector.Sprite(false, 0),
                SnakeSpriteSelector.select(
                        new Point(3, 7),
                        new Point(3, 0),
                        new Point(3, 1),
                        12, 8));
    }

    @ParameterizedTest
    @CsvSource({
            "4, 4, 6, 5",
            "3, 5, 6, 5",
            "5, 5, 6, 5",
            "4, 5, 4, 5"
    })
    void rejectsInvalidNeighbourPositions(
            int previousX, int previousY,
            int nextX, int nextY) {

        assertThrows(IllegalArgumentException.class,
                () -> SnakeSpriteSelector.select(
                        new Point(previousX, previousY),
                        new Point(5, 5),
                        new Point(nextX, nextY),
                        20, 20));
    }

    @ParameterizedTest
    @CsvSource({
            "2, 20",
            "20, 2"
    })
    void rejectsUnsupportedBoardDimensions(int width, int height) {
        assertThrows(IllegalArgumentException.class,
                () -> SnakeSpriteSelector.select(
                        new Point(4, 5),
                        new Point(5, 5),
                        new Point(6, 5),
                        width, height));
    }
}