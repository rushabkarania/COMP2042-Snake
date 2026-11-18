package com.comp2042;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import java.awt.*;
import java.util.ArrayList;

/**
 * Stores snake positions and applies movement, growth and collision rules.
 * Sprite views are loaded lazily when requested by the renderer.
 */
public class Snake {
    private Color headColor = Color.web("2eb872");
    private Color bodyColor = Color.web("a3de83");
    private boolean dead = false;
    private final Point head = new Point();
    private final ArrayList<Point> body = new ArrayList<>();
    private final int gx;
    private final int gy;
    private boolean ignoreBorders = false;
    private boolean noBorder;
    private Obstacle obstacle;
    private ImageView imageHead;
    private ImageView imageBody;
    private ImageView imageTurn;
    private ImageView imageTail;
    private int snakeDir = 270;

    /**
     * Creates a left-facing two-cell snake at the board centre.
     *
     * @param x board width in cells
     * @param y board height in cells
     * @param noBorder whether permanent border wrapping is enabled
     * @param obstacle collision-cell provider
     */
    public Snake(int x, int y, boolean noBorder, Obstacle obstacle) {
        this.obstacle = obstacle;
        this.noBorder = noBorder;
        this.head.setLocation(x / 2, y / 2);
        this.body.add(new Point(x / 2 + 1, y / 2));
        this.gx = x;
        this.gy = y;
    }

    /**
     * Attempts one complete movement step, including growth and collision checks.
     * A fatal step marks the snake dead but preserves its last valid positions
     * and sprite direction. Calls after death leave all state unchanged.
     *
     * @param dir U, D, L or R
     * @param coinPosition the coin cell, or null when no coin is present
     * @return true only when a successful move collects the coin
     * @throws IllegalArgumentException if a living snake receives an invalid direction
     */
    public boolean move(char dir, Point coinPosition) {
        return move(dir, coinPosition, true);
    }

    /**
     * Moves one cell with optional growth when a pickup is collected.
     *
     * @param dir U, D, L or R
     * @param coinPosition pickup cell, or null when no pickup is present
     * @param growOnPickup whether collection retains the old tail cell
     * @return true only when a successful move collects the pickup
     * @throws IllegalArgumentException if a living snake receives an unknown direction
     */
    boolean move(char dir, Point coinPosition, boolean growOnPickup) {
        if (dead) return false;

        Point next = new Point(head);
        int nextDirection;
        switch (dir) {
            case 'U' -> { next.translate(0, -1); nextDirection = 0; }
            case 'R' -> { next.translate(1, 0); nextDirection = 90; }
            case 'D' -> { next.translate(0, 1); nextDirection = 180; }
            case 'L' -> { next.translate(-1, 0); nextDirection = 270; }
            default -> throw new IllegalArgumentException("Unknown direction: " + dir);
        }

        boolean outside = next.x < 0 || next.y < 0 || next.x >= gx || next.y >= gy;
        if (outside && !(ignoreBorders || noBorder)) {
            dead = true;
            return false;
        }
        if (outside) {
            next.setLocation(Math.floorMod(next.x, gx), Math.floorMod(next.y, gy));
        }

        boolean collected = next.equals(coinPosition);
        boolean grows = collected && growOnPickup;
        // The tail cell is available only if this move will remove the tail.
        int firstOccupied = grows ? 0 : 1;
        boolean hitsBody = false;
        for (int i = firstOccupied; i < body.size(); i++) {
            if (next.equals(body.get(i))) {
                hitsBody = true;
                break;
            }
        }
        if (obstacle.getObstacles().contains(next) || hitsBody) {
            dead = true;
            return false;
        }

        body.add(new Point(head));
        if (!grows) {
            body.remove(0);
        }
        head.setLocation(next);
        snakeDir = nextDirection;
        return collected;
    }

    /**
     * Places a left-facing two-cell snake on the nearest clear lane to the centre.
     *
     * @param coin pickup cell to avoid, or null
     * @throws IllegalStateException if no safe three-cell lane exists
     */
    void findRespawn(Point coin) { findRespawn(coin, 'L'); }

    /**
     * Places and orients a two-cell snake with a clear first step; does not clear the death flag.
     *
     * @param coin pickup cell to avoid, or null
     * @param direction U, D, L or R for the first movement
     * @throws IllegalArgumentException if direction is unknown
     * @throws IllegalStateException if no safe three-cell lane exists
     */
    void findRespawn(Point coin, char direction) {
        int dx = switch (direction) { case 'L' -> -1; case 'R' -> 1; case 'U', 'D' -> 0;
            default -> throw new IllegalArgumentException("Unknown direction: " + direction); };
        int dy = direction == 'U' ? -1 : direction == 'D' ? 1 : 0;
        var candidates = new ArrayList<Point>();
        for (int row = 0; row < gy; row++) for (int column = 0; column < gx; column++) candidates.add(new Point(column, row));
        Point centre = new Point(gx / 2, gy / 2);
        candidates.sort(java.util.Comparator.comparingInt(p -> Math.abs(p.x-centre.x)+Math.abs(p.y-centre.y)));
        for (Point candidate : candidates) {
            Point behind = new Point(candidate.x-dx, candidate.y-dy);
            Point ahead = new Point(candidate.x+dx, candidate.y+dy);
            var cells = java.util.List.of(behind, candidate, ahead);
            if (cells.stream().anyMatch(p -> p.x < 0 || p.y < 0 || p.x >= gx || p.y >= gy)) continue;
            if (cells.contains(coin) || !java.util.Collections.disjoint(cells, obstacle.getObstacles())) continue;
            head.setLocation(candidate);
            body.clear(); body.add(behind);
            snakeDir = switch (direction) { case 'U' -> 0; case 'R' -> 90; case 'D' -> 180; default -> 270; };
            return;
        }
        throw new IllegalStateException("No safe respawn lane");
    }

    /**
     * Removes tail segments while retaining a head and at least one body cell.
     *
     * @param amount maximum number of tail segments to remove
     * @return actual number of removed segments
     * @throws IllegalArgumentException if amount is negative
     */
    public int shrink(int amount) {
        if (amount < 0) throw new IllegalArgumentException("Shrink amount must not be negative");
        int removed = Math.min(amount, Math.max(0, body.size() - 1));
        body.subList(0, removed).clear();
        return removed;
    }

    /**
     * Returns the legacy straight-body orientation for a segment.
     *
     * @param bodyCount body index counted from the tail
     * @return 0 for the first or a vertical segment; 270 for a horizontal segment
     */
    public int getBodyDir(int bodyCount) {
        if (bodyCount == 0) {
            return 0;
        } else if (body.get(bodyCount).getY() == body.get(bodyCount - 1).getY()) {
            return 270;
        } else {
            return 0;
        }
    }

    /**
     * Returns a deep copy of body positions ordered from tail towards head.
     *
     * @return copied list and points, excluding the head
     */
    public ArrayList<Point> getBody() {
        return body.stream().map(Point::new).collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    /**
     * Returns a defensive copy of the head position.
     *
     * @return head cell coordinates
     */
    public Point getHead() {
        return new Point(head);
    }

    /**
     * Returns the fallback head colour.
     *
     * @return head colour
     */
    public Color getHeadColor() {
        return headColor;
    }

    /**
     * Returns the fallback body colour.
     *
     * @return body colour
     */
    public Color getBodyColor() {
        return bodyColor;
    }

    /**
     * Reports whether a fatal collision has occurred.
     *
     * @return true after a fatal movement attempt
     */
    public boolean getDead() {
        return dead;
    }

    /**
     * Enables or disables temporary wrapping independently of easy difficulty.
     *
     * @param ign true to allow wrapping during a temporary effect
     */
    public void ignoreBorders(boolean ign) {
        ignoreBorders = ign;
    }

    /**
     * Lazily creates and reuses the head sprite view.
     *
     * @return shared head image view
     */
    public ImageView getImageViewHead() {
        if (imageHead == null) {
            imageHead = new ImageView(new Image("resources/views/res/snakehead.png"));
        }
        return imageHead;
    }

    /**
     * Lazily creates and reuses the straight body sprite view.
     *
     * @return shared straight body image view
     */
    public ImageView getImageViewBody() {
        if (imageBody == null) {
            imageBody = new ImageView(new Image("resources/views/res/snakebody.png"));
        }
        return imageBody;
    }

    /**
     * Lazily creates and reuses the corner sprite view.
     *
     * @return shared corner image view
     */
    public ImageView getImageViewTurn() {
        if (imageTurn == null) {
            imageTurn = new ImageView(new Image("resources/views/res/snaketurnleft.png"));
        }
        return imageTurn;
    }

    /**
     * Returns the current sprite orientation.
     *
     * @return 0 for up, 90 for right, 180 for down or 270 for left
     */
    public int getSnakeDir() {
        return snakeDir;
    }

    /**
     * Lazily creates and reuses the tail sprite view.
     *
     * @return shared tail image view
     */
    public ImageView getImageViewTail() {
        if (imageTail == null) {
            imageTail = new ImageView(new Image("resources/views/res/snaketail.png"));
        }
        return imageTail;
    }
}
