import java.awt.Dimension;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Graphics {
    // List of sprite frames (images) for the animation
    private final List<Img> frames = new ArrayList<>();
    // Whether the animation should loop continuously or stop at the last frame
    private final boolean loop;
    // Frames per second for the animation playback speed
    private final double fps;
    // Duration of each frame in milliseconds (1000 ms divided by fps)
    private final double frameDurationMs;
    // Timestamp in milliseconds when the animation started/reset
    private long startMs;
    // Index of the current frame in the frames list
    private int curFrame;

    /**
     * Constructor to load all PNG sprite frames from a folder,
     * resize them to fit cellSize, and initialize animation parameters.
     *
     * @param spritesFolder Path to the folder containing sprite PNG images
     * @param cellSize Desired cell size (width and height) in pixels for resizing sprites
     * @param loop Whether to loop the animation continuously
     * @param fps Frames per second (animation speed)
     */
    public Graphics(Path spritesFolder, Dimension cellSize, boolean loop, double fps) {
        this.loop = loop;
        this.fps = fps;
        this.frameDurationMs = 1000.0 / fps;

        // Load PNG files in alphabetical order from spritesFolder
        // Use Java streams to list and filter files ending with ".png"
        // Read each PNG file into a BuffImg object, resizing to cellSize with aspect ratio preserved
        try (java.util.stream.Stream<java.nio.file.Path> paths = java.nio.file.Files.list(spritesFolder)) {
            paths.filter(p -> p.toString().endsWith(".png"))
                    .sorted()
                    .forEach(p -> frames.add(new BuffImg().read(p.toString(), cellSize, true, null)));
        } catch (java.io.IOException e) {
            // If folder reading or loading any image fails, throw a runtime exception
            throw new RuntimeException("Failed listing sprites folder: " + spritesFolder, e);
        }

        // If no frames were loaded, throw an exception because animation can't run without frames
        if (frames.isEmpty()) {
            throw new IllegalArgumentException("No PNG sprite frames found in folder: " + spritesFolder);
        }
    }

    /**
     * Reset the animation timing to a new start time based on the command's timestamp.
     * Also resets the current frame to the first frame.
     *
     * @param cmd Command object containing the timestamp to reset the animation to
     */
    public void reset(Command cmd) {
        this.startMs = cmd.timestamp;
        this.curFrame = 0;
    }

    /**
     * Update the current frame index based on how much time has elapsed since startMs.
     * If looping is enabled, wraps around frames.
     * Otherwise, stops at the last frame.
     *
     * @param nowMs Current time in milliseconds used to calculate frame progression
     */
    public void update(long nowMs) {
        long elapsed = nowMs - startMs;                   // elapsed time since animation started
        int framesPassed = (int) (elapsed / frameDurationMs);  // how many frames should have passed

        if (loop) {
            // Loop animation by cycling through frames
            curFrame = framesPassed % frames.size();
        } else {
            // Clamp to last frame if animation should not loop
            curFrame = Math.min(framesPassed, frames.size() - 1);
        }
    }

    /**
     * Get the current frame image for rendering.
     *
     * @return The Img object representing the current frame
     */
    public Img getImg() {
        if (frames.isEmpty()) throw new IllegalStateException("No frames loaded for animation.");
        return frames.get(curFrame);
    }

    // Accessor to get frames per second (fps) for tests or info
    public double getFps() { return fps; }

    // Accessor to get all loaded frames (useful for testing)
    public List<Img> getFrames() { return frames; }

    // Accessor to get the current frame index (useful for testing)
    public int getCurFrame() { return curFrame; }
}
