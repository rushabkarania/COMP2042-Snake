package com.comp2042;

import javafx.geometry.Point2D;
import org.junit.jupiter.api.Test;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SnakeBodyPainterTest {
    private static BufferedImage sprite(String name) {
        try {
            return javax.imageio.ImageIO.read(SnakeBodyPainterTest.class.getResource(
                    "/resources/views/res/snake" + name + ".png"));
        } catch (java.io.IOException e) {
            throw new AssertionError(e);
        }
    }

    private static final BufferedImage BODY = sprite("body"), TAIL = sprite("tail");

    @Test void latePulseRetainsRemainderAndLongStallStartsFresh() {
        assertEquals(1100, GameController.stepStart(1000, 1116, 100));
        assertEquals(1500, GameController.stepStart(1000, 1500, 100));
    }

    private BufferedImage frame(List<Point2D> path) {
        BufferedImage image = new BufferedImage(320, 320, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            g.scale(32, 32);
            SnakeBodyPainter.paint(new SnakeBodyPainter.Surface() {
                public void joint(double x, double y, double r) {
                    g.setColor(Color.GREEN);
                    g.fill(new java.awt.geom.Ellipse2D.Double(x-r, y-r, 2*r, 2*r));
                }
                public void strip(double x, double y, double angle, double length, double offset, boolean tail) {
                    BufferedImage tile = tail ? TAIL : BODY;
                    double h = tile.getHeight() * (tail ? .95 : 1);
                    Graphics2D out = (Graphics2D) g.create();
                    out.translate(x, y);
                    out.rotate(Math.toRadians(angle));
                    out.clip(new java.awt.geom.Rectangle2D.Double(-.5, -length, 1, length));
                    out.translate(-.5, offset - 1);
                    out.scale(1.0 / tile.getWidth(), 1.0 / h);
                    out.drawImage(tile, 0, 0, null);
                    out.dispose();
                }
            }, path, 10, 10);
        } finally {
            g.dispose();
        }
        return image;
    }

    @Test void tipStaysNarrowAndSpineStaysConnectedThroughEveryStraightFrame() {
        for (int pixel = 0; pixel <= 32; pixel++) {
            double t = pixel / 32.0;
            var path = GameView.bodyPath(List.of(new Point(3, 4), new Point(4, 4)),
                    new Point(5, 4), List.of(new Point(2, 4), new Point(3, 4)), new Point(4, 4), 10, 10, t);
            var image = frame(path);
            int tail = 80 + pixel;
            assertEquals(0, image.getRGB(tail + 4, 144 - 7), "square tail at frame " + pixel);
            for (int x = tail + 3; x < tail + 64; x++)
                assertNotEquals(0, image.getRGB(x, 144), "gap at frame " + pixel);
        }
    }

    @Test void successiveRightAndDownTurnsKeepAnUnbrokenSpine() {
        for (int pixel = 0; pixel <= 32; pixel++) {
            double t = pixel / 32.0;
            var path = GameView.bodyPath(List.of(new Point(3, 3), new Point(3, 4), new Point(4, 4)),
                    new Point(4, 5), List.of(new Point(2, 3), new Point(3, 3), new Point(3, 4)),
                    new Point(4, 4), 10, 10, t);
            var image = frame(path);
            for (int x = 83 + pixel; x <= 112; x++) assertNotEquals(0, image.getRGB(x, 112));
            for (int y = 115; y <= 144; y++) assertNotEquals(0, image.getRGB(112, y));
            for (int x = 112; x <= 144; x++) assertNotEquals(0, image.getRGB(x, 144));
            for (int y = 144; y <= 144 + pixel; y++) assertNotEquals(0, image.getRGB(144, y));
        }
    }

    @Test void wrappedCornerPaintsBothEdgesAndLeavesMiddleEmpty() {
        var image = frame(List.of(new Point2D(8,4), new Point2D(9,4),
                new Point2D(0,4), new Point2D(0,5)));
        assertNotEquals(0, image.getRGB(319,144));
        assertNotEquals(0, image.getRGB(0,144));
        assertNotEquals(0, image.getRGB(16,158));
        assertEquals(0, image.getRGB(160,144));
    }
}
