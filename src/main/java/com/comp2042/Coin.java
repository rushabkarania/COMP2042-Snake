package com.comp2042;

import javafx.scene.image.Image;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A collectible with a cached sprite, a free board position and a score value.
 */
public class Coin {
    private Point pos;
    private final Sprite sprite;

    /**
     * Cached images for each collectible type.
     */
    enum Sprite {
        /** Ordinary five-point coin. */ RED("red"),
        /** Temporary border-wrap pickup. */ BLUE("blue"),
        /** Fifteen-point bonus coin. */ GOLD("gold"),
        /** Temporary hedge-gate pickup. */ SILVER("silver"),
        /** Temporary rush pickup. */ RAINBOW("rainbow"),
        /** Tail-shortening pickup. */ SHRINK("shrink");

        /**
         * Decoded image shared by all coins with this sprite.
         */
        final Image image;

        /**
         * Loads the bundled coin image.
         *
         * @param name resource filename prefix, without coin.png
         */
        Sprite(String name) {
            String path = "/resources/views/res/" + name + "coin.png";
            try (var stream = java.util.Objects.requireNonNull(Coin.class.getResourceAsStream(path), path)) {
                image = new Image(stream);
            } catch (java.io.IOException failure) {
                throw new IllegalStateException(path, failure);
            }
        }
    }

    /**
     * Places a red coin uniformly on a free cell.
     *
     * @param x board width in cells
     * @param y board height in cells
     * @param snek snake whose occupied cells must be avoided
     * @param obstacle current collision cells to avoid
     * @throws IllegalStateException if the board has no free cell
     */
    public Coin(int x, int y, Snake snek, Obstacle obstacle) {
        this(x, y, snek, obstacle, Sprite.RED);
    }

    /**
     * Places a specified coin uniformly on a free cell.
     *
     * @param x board width in cells
     * @param y board height in cells
     * @param snek snake whose occupied cells must be avoided
     * @param obstacle current collision cells to avoid
     * @param sprite cached collectible sprite
     * @throws IllegalStateException if the board has no free cell
     */
    Coin(int x, int y, Snake snek, Obstacle obstacle, Sprite sprite) {
        this.sprite = sprite;
        pos = choosePosition(x, y, snek, obstacle)
                .orElseThrow(() -> new IllegalStateException("Cannot create a coin on a full board"));
    }

    /**
     * Chooses uniformly from cells unoccupied by the snake or obstacles.
     *
     * @param width board width in cells
     * @param height board height in cells
     * @param snake snake occupying the board
     * @param obstacles current collision cells
     * @return a free position, or an empty optional when the board is full
     */
    static Optional<Point> choosePosition(int width, int height, Snake snake, Obstacle obstacles) {
        var occupied = new HashSet<>(snake.getBody());
        occupied.add(snake.getHead());
        occupied.addAll(obstacles.getObstacles());
        var free = new ArrayList<Point>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Point cell = new Point(x, y);
                if (!occupied.contains(cell)) {
                    free.add(cell);
                }
            }
        }
        if (free.isEmpty()) return Optional.empty();
        return Optional.of(free.get(ThreadLocalRandom.current().nextInt(free.size())));
    }

    /**
     * Places a known collectible for scripted boards and deterministic tests.
     *
     * @param position cell to copy; the caller ensures it is free
     * @param sprite cached collectible sprite
     */
    Coin(Point position, Sprite sprite) {
        this.pos = new Point(java.util.Objects.requireNonNull(position));
        this.sprite = java.util.Objects.requireNonNull(sprite);
    }

    /**
     * Identifies the gameplay effect associated with the sprite.
     *
     * @return collectible type
     */
    CoinSequence.Type type() { return CoinSequence.Type.valueOf(sprite.name()); }

    /**
     * Returns a defensive copy of the collectible position.
     *
     * @return a new point in board-cell coordinates
     */
    public Point getPos() {
        return new Point(pos);
    }

    /**
     * Returns the score awarded for this collectible.
     *
     * @return 15 for gold; 5 for every other sprite
     */
    public int getPoints() {
        return sprite == Sprite.GOLD ? 15 : 5;
    }

    /**
     * Returns the shared image for rendering this collectible.
     *
     * @return cached sprite image
     */
    public Image getImage() {
        return sprite.image;
    }
}

