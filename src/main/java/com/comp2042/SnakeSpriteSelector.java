package com.comp2042;

import java.awt.Point;

/**
 * Determines body-sprite rotation from neighbouring snake positions.
 */
final class SnakeSpriteSelector {

    private SnakeSpriteSelector() {
    }

    /**
     * Selects a straight-body or corner rotation.
     *
     * @param previous the neighbour towards the tail
     * @param current the segment being drawn
     * @param next the neighbour towards the head
     * @param width the board width in cells
     * @param height the board height in cells
     * @return the sprite shape and clockwise rotation in degrees
     * @throws IllegalArgumentException if neighbours are not distinct,
     *         orthogonally adjacent cells, including across a border
     */
    static Sprite select(
            Point previous, Point current, Point next,
            int width, int height) {

        int first = direction(current, previous, width, height);
        int second = direction(current, next, width, height);

        if (first == second) {
            throw new IllegalArgumentException(
                    "Body neighbours must connect on different sides");
        }

        int connections = first | second;

        return switch (connections) {
            case 5 -> new Sprite(false, 0);    // Up and down
            case 10 -> new Sprite(false, 90);  // Left and right
            case 12 -> new Sprite(true, 0);   // Left and down
            case 9 -> new Sprite(true, 90);   // Left and up
            case 3 -> new Sprite(true, 180);  // Right and up
            case 6 -> new Sprite(true, 270);  // Right and down
            default -> throw new IllegalArgumentException(
                    "Invalid body connections");
        };
    }

    private static int direction(
            Point from, Point to, int width, int height) {

        if (width < 3 || height < 3) {
            throw new IllegalArgumentException(
                    "Board dimensions must be at least three");
        }

        if (from.x == to.x) {
            if (to.y == from.y - 1
                    || (from.y == 0 && to.y == height - 1)) {
                return 1; // Up
            }
            if (to.y == from.y + 1
                    || (from.y == height - 1 && to.y == 0)) {
                return 4; // Down
            }
        }

        if (from.y == to.y) {
            if (to.x == from.x + 1
                    || (from.x == width - 1 && to.x == 0)) {
                return 2; // Right
            }
            if (to.x == from.x - 1
                    || (from.x == 0 && to.x == width - 1)) {
                return 8; // Left
            }
        }

        throw new IllegalArgumentException(
                "Neighbour must be one cell away");
    }

    /**
     * Describes whether to use a corner image and its rotation.
     *
     * @param corner true for a corner image; false for a straight body
     * @param rotation clockwise rotation in degrees
     */
    record Sprite(boolean corner, int rotation) {
    }
}