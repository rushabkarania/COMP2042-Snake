package com.comp2042;

import javafx.scene.image.Image;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class Coin {
    private Point pos;
    private final Sprite sprite;

    enum Sprite {
        RED("red"), BLUE("blue"), GOLD("gold"), SILVER("silver"), RAINBOW("rainbow"), SHRINK("shrink");

        final Image image;

        Sprite(String name) {
            String path = "/resources/views/res/" + name + "coin.png";
            try (var stream = java.util.Objects.requireNonNull(Coin.class.getResourceAsStream(path), path)) {
                image = new Image(stream);
            } catch (java.io.IOException failure) {
                throw new IllegalStateException(path, failure);
            }
        }
    }

    public Coin(int x, int y, Snake snek, Obstacle obstacle) {
        this(x, y, snek, obstacle, Sprite.RED);
    }

    Coin(int x, int y, Snake snek, Obstacle obstacle, Sprite sprite) {
        this.sprite = sprite;
        pos = choosePosition(x, y, snek, obstacle)
                .orElseThrow(() -> new IllegalStateException("Cannot create a coin on a full board"));
    }

    /** Chooses uniformly from free cells and returns empty when all cells are occupied. */
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

    /** Places a known collectible for scripted boards and deterministic tests. */
    Coin(Point position, Sprite sprite) {
        this.pos = new Point(java.util.Objects.requireNonNull(position));
        this.sprite = java.util.Objects.requireNonNull(sprite);
    }

    CoinSequence.Type type() { return CoinSequence.Type.valueOf(sprite.name()); }

    public Point getPos() {
        return new Point(pos);
    }

    public int getPoints() {
        return sprite == Sprite.GOLD ? 15 : 5;
    }

    public Image getImage() {
        return sprite.image;
    }
}

