package com.comp2042;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

/** Small original vector controls; independent of installed icon fonts. */
final class UiIcons {
    /**
     * Creates a white vector control icon independent of installed fonts.
     *
     * @param name sound, muted, fullscreen, pause or play; any other value selects close
     * @return new group containing the vector path
     */
    static Group icon(String name) {
        String path = switch (name) {
            case "sound" -> "M3 9H7L12 5V19L7 15H3Z M16 8Q21 12 16 16 M19 5Q26 12 19 19";
            case "muted" -> "M3 9H7L12 5V19L7 15H3Z M17 9L23 15 M23 9L17 15";
            case "fullscreen" -> "M3 9V3H9 M17 3H23V9 M23 17V23H17 M9 23H3V17";
            case "pause" -> "M9 5V21 M17 5V21";
            case "play" -> "M8 4L22 13L8 22Z";
            default -> "M5 5L21 21 M21 5L5 21";
        };
        SVGPath shape = new SVGPath(); shape.setContent(path);
        shape.setFill(Color.TRANSPARENT); shape.setStroke(Color.WHITE); shape.setStrokeWidth(2.2);
        shape.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        return new Group(shape);
    }
    private UiIcons() { }
}
