import java.util.Map;

public class KeyboardProcessor {
    // Number of rows and columns in the grid this keyboard controls
    private final int rows;
    private final int cols;

    // Map from key strings (e.g. "w", "up") to action strings (e.g. "up", "choose")
    private final Map<String, String> keymap;

    // Current cursor position (row and column)
    private int cursorR = 0;
    private int cursorC = 0;

    // Constructor to initialize with grid size and key mapping
    public KeyboardProcessor(int rows, int cols, Map<String, String> keymap) {
        this.rows = rows;
        this.cols = cols;
        this.keymap = keymap;
    }

    /**
     * Processes a key input string and updates cursor position accordingly.
     * This method is synchronized to ensure thread safety when called concurrently.
     *
     * @param key The key identifier (e.g., "w", "up", "enter")
     * @return The action string mapped to the key, or null if the key is unrecognized.
     */
    public synchronized String processKey(String key) {
        // Translate Hebrew keys to Latin equivalents before lookup
        key = translateHebrewKey(key);

        // Lookup action from keymap
        String action = keymap.get(key);
        if (action == null) return null; // Unknown key, ignore

        // Update cursor position based on action, while keeping cursor inside bounds
        switch (action) {
            case "up"    -> cursorR = Math.max(0, cursorR - 1);
            case "down"  -> cursorR = Math.min(rows - 1, cursorR + 1);
            case "left"  -> cursorC = Math.max(0, cursorC - 1);
            case "right" -> cursorC = Math.min(cols - 1, cursorC + 1);
            // "choose" and "jump" actions do not move the cursor
        }
        return action;
    }

    /**
     * Translates Hebrew key characters to corresponding Latin keys for uniform handling.
     *
     * @param key The key string input.
     * @return The translated key string, or the original if no translation applies.
     */
    private String translateHebrewKey(String key) {
        return switch (key) {
            case "ש" -> "a";
            case "ד" -> "s";
            case "ג" -> "d";
            case "'" -> "w";
            case "כ" -> "f";
            case "ע" -> "g";
            default -> key;
        };
    }

    /**
     * Returns the current cursor position as an array: [row, column].
     * Synchronized for thread safety.
     */
    public synchronized int[] getCursor() {
        return new int[]{cursorR, cursorC};
    }

    /**
     * Allows direct setting of the cursor position (used mainly for tests).
     * The position is clamped within valid bounds.
     *
     * @param r Row index
     * @param c Column index
     */
    public synchronized void setCursor(int r, int c) {
        cursorR = Math.max(0, Math.min(rows - 1, r));
        cursorC = Math.max(0, Math.min(cols - 1, c));
    }
}
