package com.comp2042;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Loads each bundled decoration once and creates independently sized views for the UI. */
final class GardenAssets {
    private static final Map<String, Image> IMAGES = new HashMap<>();
    /**
     * Loads and caches a bundled garden decoration.
     *
     * @param name PNG basename without its extension
     * @return shared decoded image
     */
    static Image image(String name) {
        return IMAGES.computeIfAbsent(name, key -> {
            String path = "/resources/views/res/garden/" + key + ".png";
            try (var stream = Objects.requireNonNull(GardenAssets.class.getResourceAsStream(path), path)) {
                return new Image(stream);
            } catch (java.io.IOException failure) {
                throw new IllegalStateException("Cannot load " + path, failure);
            }
        });
    }
    /**
     * Loads and caches a coin image constrained to 40 by 40 pixels.
     *
     * @param type collectible whose thumbnail is requested
     * @return shared thumbnail preserving aspect ratio
     */
    static Image coinThumbnail(CoinSequence.Type type) {
        return IMAGES.computeIfAbsent("coin-" + type.name(), key -> {
            String name = type == CoinSequence.Type.SHRINK ? "shrink" : type.name().toLowerCase(java.util.Locale.ROOT);
            String path = "/resources/views/res/" + name + "coin.png";
            try (var stream = Objects.requireNonNull(GardenAssets.class.getResourceAsStream(path), path)) {
                return new Image(stream, 40, 40, true, true);
            } catch (java.io.IOException failure) { throw new IllegalStateException(path, failure); }
        });
    }
    /**
     * Loads and caches the supplied SnakeLogo.png at a requested size.
     *
     * @param size maximum width and height in pixels
     * @return shared logo image preserving aspect ratio
     */
    static Image logo(int size) {
        return IMAGES.computeIfAbsent("snake-logo-" + size, key -> {
            String path = "/resources/views/res/SnakeLogo.png";
            try (var stream = Objects.requireNonNull(GardenAssets.class.getResourceAsStream(path), path)) {
                return new Image(stream, size, size, true, true);
            } catch (java.io.IOException failure) { throw new IllegalStateException(path, failure); }
        });
    }
    /**
     * Creates an independently sized view of a cached garden image.
     *
     * @param name garden PNG basename without extension
     * @param size maximum view width and height in pixels
     * @return new image view preserving aspect ratio
     */
    static ImageView icon(String name, double size) {
        ImageView view = new ImageView(image(name));
        view.setFitWidth(size);
        view.setFitHeight(size);
        view.setPreserveRatio(true);
        return view;
    }
    private GardenAssets() { }
}
