package com.comp2042;

import javafx.scene.paint.Color;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class Obstacle {
    private Color color = Color.BLACK;
    private ArrayList<Point> obstacles = new ArrayList<>();
    private int x;
    private int y;

    public Obstacle(int x, int y) {
       this.x = x;
       this.y = y;
    }

    private static final int MAX_OBSTACLES = 4;
    private static final int MIN_DISTANCE = 4;

    private int quadrant(Point cell) {
        return (cell.x < x / 2 ? 0 : 1) + (cell.y < y / 2 ? 0 : 2);
    }

    private boolean hasClearance(Point candidate, Point head) {
        if (candidate.distanceSq(head) < MIN_DISTANCE * MIN_DISTANCE) return false;
        for (Point existing : obstacles) {
            if (quadrant(candidate) == quadrant(existing)
                    || candidate.distanceSq(existing) < MIN_DISTANCE * MIN_DISTANCE) return false;
        }
        return true;
    }

    /**
     * Places up to the requested number of obstacles on distinct free cells.
     * @return the number actually added
     */
    public int newObstacles(Snake snake, Coin coin, int amount) {
        return onSilverPickup(snake, coin.getPos(), amount);
    }

    /** Moves one existing block when a safe replacement exists, then fills empty quarters. */
    int onSilverPickup(Snake snake, Point coinPosition, int amount) {
        if (amount <= 0) return 0;
        var previous = new ArrayList<>(obstacles);
        Collections.shuffle(previous);
        for (Point old : previous) {
            obstacles.remove(old);
            // Reject the old cell so a successful relocation always changes the board.
            if (placeObstacles(snake, coinPosition, 1, old) == 1) break;
            obstacles.add(old);
        }
        // Only added cells reduce the win target. Relocation preserves the total.
        return placeObstacles(snake, coinPosition, amount);
    }

    int placeObstacles(Snake snake, Point coinPosition, int amount) {
        return placeObstacles(snake, coinPosition, amount, null);
    }

    private int placeObstacles(Snake snake, Point coinPosition, int amount, Point old) {
        int limit = Math.min(amount, MAX_OBSTACLES - obstacles.size());
        if (limit <= 0) return 0;

        var occupied = new HashSet<>(obstacles);
        occupied.addAll(snake.getBody());
        occupied.add(snake.getHead());
        occupied.add(coinPosition);

        var free = new ArrayList<Point>();
        for (int row = 3; row < y - 2; row++) {
            for (int column = 1; column < x - 1; column++) {
                Point cell = new Point(column, row);
                if (!occupied.contains(cell) && !cell.equals(old)
                        && (old == null || quadrant(cell) == quadrant(old))) free.add(cell);
            }
        }
        Collections.shuffle(free);
        int added = 0;
        Point head = snake.getHead();
        // A collected coin shares the head cell. Keep space for its replacement.
        int available = x * y - occupied.size();
        int reserve = head.equals(coinPosition) ? 1 : 0;
        for (Point candidate : free) {
            if (added >= limit || added >= available - reserve) break;
            if (hasClearance(candidate, head)) {
                obstacles.add(candidate);
                added++;
            }
        }
        return added;
    }

    private Point patrol;
    private int originX;
    private int direction = 1;
    private int delay;
    private int frozenSteps;

    /** Turn an existing block into a horizontal patrol without adding an occupied cell. */
    void startPatrol() {
        if (patrol != null || obstacles.isEmpty()) return;
        patrol = obstacles.get(0);
        originX = patrol.x;
        direction = 1;
        delay = 0;
    }

    void freezePatrol() {
        frozenSteps = 12;
    }

    /** Advance on every second snake step. Reverse at a blocked cell or route endpoint. */
    void advancePatrol(Snake snake, Point coin) {
        if (patrol == null) return;
        if (frozenSteps > 0) {
            frozenSteps--;
            return;
        }
        delay = (delay + 1) % 2;
        if (delay != 0) return;
        Point next = new Point(patrol.x + direction, patrol.y);
        if (next.x < 0 || next.x >= x || Math.abs(next.x - originX) > 2
                || obstacles.contains(next) || next.equals(coin)
                || next.equals(snake.getHead()) || snake.getBody().contains(next)) {
            direction = -direction;
            return;
        }
        patrol.setLocation(next);
    }

    public Color getColor() {
        return color;
    }

    public ArrayList<Point> getObstacles() {
        return obstacles;
    }

    public void reset() {
        obstacles.clear();
        patrol = null;
        frozenSteps = 0;
    }
}
