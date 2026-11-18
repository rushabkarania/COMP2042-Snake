package com.comp2042;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Loads each bundled decoration once and creates independently sized views for the UI. */
final class GardenAssets {
    private static final Map<String, Image> IMAGES = new HashMap<>();
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
    /** Decodes small HUD thumbnails once instead of resampling large coin textures per view. */
    static Image coinThumbnail(CoinSequence.Type type) {
        return IMAGES.computeIfAbsent("coin-" + type.name(), key -> {
            String name = type == CoinSequence.Type.SHRINK ? "shrink" : type.name().toLowerCase(java.util.Locale.ROOT);
            String path = "/resources/views/res/" + name + "coin.png";
            try (var stream = Objects.requireNonNull(GardenAssets.class.getResourceAsStream(path), path)) {
                return new Image(stream, 40, 40, true, true);
            } catch (java.io.IOException failure) { throw new IllegalStateException(path, failure); }
        });
    }
    /** Uses the supplied SnakeLogo.png for both window icons and menu branding. */
    static Image logo(int size) {
        return IMAGES.computeIfAbsent("snake-logo-" + size, key -> {
            String path = "/resources/views/res/SnakeLogo.png";
            try (var stream = Objects.requireNonNull(GardenAssets.class.getResourceAsStream(path), path)) {
                return new Image(stream, size, size, true, true);
            } catch (java.io.IOException failure) { throw new IllegalStateException(path, failure); }
        });
    }
    static ImageView icon(String name, double size) {
        ImageView view = new ImageView(image(name));
        view.setFitWidth(size);
        view.setFitHeight(size);
        view.setPreserveRatio(true);
        return view;
    }
    private GardenAssets() { }
}
