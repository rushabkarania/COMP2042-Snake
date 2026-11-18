package com.comp2042;

import javafx.geometry.Point2D;
import java.util.List;

/** Maps the original sprite rows onto the moving tail-to-head path in cell units. */
final class SnakeBodyPainter {
    /**
     * Drawing callbacks expressed in board-cell units.
     */
    interface Surface {
        /**
         * Draws the filler beneath a connected body bend.
         *
         * @param x centre x coordinate in cell units
         * @param y centre y coordinate in cell units
         * @param radius joint radius in cell units
         */
        void joint(double x, double y, double radius);
        /**
         * Draws a section of the body or tail sprite along the path.
         *
         * @param x strip origin x in cell units
         * @param y strip origin y in cell units
         * @param angle clockwise rotation in degrees
         * @param length strip length in cell units
         * @param offset fractional offset into the current sprite cell
         * @param tail true when this strip belongs to the first tail cell
         */
        void strip(double x, double y, double angle, double length, double offset, boolean tail);
    }
    /**
     * Creates a body painter.
     */
    SnakeBodyPainter() {
    }

    /**
     * Draws joints first and textured strips second, including copies across wrapped edges.
     *
     * @param surface drawing callback implementation
     * @param path tail-to-head path in cell coordinates
     * @param width board width in cells
     * @param height board height in cells
     */
    static void paint(Surface surface, List<Point2D> path, int width, int height) {
        // Joints sit underneath the art so they cannot erase the scale pattern.
        for (int pass = 0; pass < 2; pass++) {
            double distance = 0;
            for (int i = 1; i < path.size(); i++) {
                Point2D from = path.get(i - 1), to = path.get(i);
                double dx = wrappedDelta(to.getX() - from.getX(), width);
                double dy = wrappedDelta(to.getY() - from.getY(), height);
                double length = Math.hypot(dx, dy);
                if (length == 0) continue;
                double x = from.getX() + .5, y = from.getY() + .5;
                x -= width * Math.floor(x / width);
                y -= height * Math.floor(y / height);
                double ox = copyOffset(x, x + dx, width);
                double oy = copyOffset(y, y + dy, height);
                for (int col = 0; col <= (ox == 0 ? 0 : 1); col++) {
                    for (int row = 0; row <= (oy == 0 ? 0 : 1); row++) {
                        double sx = x + col * ox, sy = y + row * oy;
                        if (pass == 0) {
                            if (distance > 0) surface.joint(sx, sy, .34 * Math.min(1, distance));
                            if (i == path.size() - 1 || to.equals(path.get(path.size() - 1))) {
                                surface.joint(sx + dx, sy + dy, .34 * Math.min(1, distance + length));
                            }
                        } else {
                            double used = 0;
                            while (length - used > 1e-8) {
                                double d = distance + used;
                                double offset = d - Math.floor(d);
                                double part = Math.min(length - used, 1 - offset);
                                surface.strip(sx + dx * used / length, sy + dy * used / length,
                                        Math.toDegrees(Math.atan2(dy, dx)) + 90, part, offset, d < 1);
                                used += part;
                            }
                        }
                    }
                }
                distance += length;
            }
        }
    }

    private static double copyOffset(double a, double b, int size) {
        if (Math.min(a, b) < .5) return size;
        if (Math.max(a, b) > size - .5) return -size;
        return 0;
    }

    private static double wrappedDelta(double delta, int size) {
        return delta - size * Math.rint(delta / size);
    }
}
