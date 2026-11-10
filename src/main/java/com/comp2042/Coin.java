package com.comp2042;

import javafx.scene.image.Image;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class Coin {
    private Point pos;
    private Image image = new Image("resources/views/res/redcoin.png");

    public Coin(int x, int y, Snake snek, Obstacle obstacle) {
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

    public Point getPos() {
        return new Point(pos);
    }

    public int getPoints() {
        int points = 5;
        return points;
    }

    public Image getImage() {
        return image;
    }
}

