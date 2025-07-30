package keyBoard;

import java.util.Map;

public class KeyboardProcessor {
    private final int rows;
    private final int cols;
    private final Map<String, String> keymap; // key string -> action

    private int cursorR = 0;
    private int cursorC = 0;

    public KeyboardProcessor(int rows, int cols, Map<String, String> keymap) {
        this.rows = rows;
        this.cols = cols;
        this.keymap = keymap;
    }

    /**
     * Process a key input. This method is synchronised to guarantee thread-safety for tests that
     * invoke it from multiple threads concurrently.
     *
     * @param key key identifier (e.g. "w", "up", "enter")
     * @return The mapped action string, or null if key not recognised.
     */
    public synchronized String processKey(String key) {
        String action = keymap.get(key);
        if (action == null) {
            return null; // unknown key – ignore
        }

        switch (action) {
            case "up":
                if (cursorR > 0) cursorR -= 1;
                break;
            case "down":
                if (cursorR < rows - 1) cursorR += 1;
                break;
            case "left":
                if (cursorC > 0) cursorC -= 1;
                break;
            case "right":
                if (cursorC < cols - 1) cursorC += 1;
                break;
            case "choose":
            case "jump":
                // no cursor modification
                break;
            default:
                // non movement action – leave cursor unchanged
                break;
        }
        return action;
    }

    /** Return the current cursor position as an (row,col) int array. */
    public synchronized int[] getCursor() {
        return new int[]{cursorR, cursorC};
    }

    /** For tests – allow direct cursor manipulation. */
    public synchronized void setCursor(int r, int c) {
        cursorR = Math.max(0, Math.min(rows - 1, r));
        cursorC = Math.max(0, Math.min(cols - 1, c));
    }
}