package grafix;

import java.awt.Dimension;
import java.nio.file.Path;
import org.json.JSONObject;

public class GraphicsFactory {

    /**
     * Loads a grafix.Graphics animation object from a directory of sprite images,
     * using configuration provided in a JSON object, and the target cell size.
     *
     * @param spritesDir Path to the directory containing sprite PNG images.
     * @param cfg JSON configuration object with animation settings.
     * @param cellSize Dimension object specifying the target width and height for sprites.
     * @return A new grafix.Graphics object initialized with the sprites and configuration.
     */
    public Graphics load(Path spritesDir, JSONObject cfg, Dimension cellSize) {
        // Read optional boolean "is_loop" from the config; default to true if missing.
        boolean loop = cfg.optBoolean("is_loop", true);

        // Read optional double "frames_per_sec" from the config; default to 6.0 FPS if missing.
        double fps = cfg.optDouble("frames_per_sec", 6.0);

        // Create and return a grafix.Graphics instance with given parameters.
        return new Graphics(spritesDir, cellSize, loop, fps);
    }
}
