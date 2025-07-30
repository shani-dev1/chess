package classes;

import java.io.BufferedReader; // For reading text files line by line
import java.io.IOException;     // For handling input/output exceptions
import java.nio.file.Files;     // For working with files
import java.nio.file.Path;      // Represents a file path
import java.util.HashMap;       // A basic key-value data structure
import java.util.Map;           // Interface for key-value maps

public class Moves {

    private final int rows;  // Number of rows on the board
    private final int cols;  // Number of columns on the board

    // Map storing allowed moves.
    // Key: classes.Pair of (delta row, delta column) indicating relative move direction and distance
    // Value: A string tag that describes move type, e.g. "capture", "non_capture", or empty meaning normal move
    private final Map<Pair, String> moves = new HashMap<>();

    // Constructor: loads moves from a text file at movesFile path, using given board dimensions
    public Moves(Path movesFile, int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        if (Files.exists(movesFile)) {  // Only attempt loading if file exists
            try (BufferedReader br = Files.newBufferedReader(movesFile)) {
                // Read all lines and process each one
                br.lines().forEach(line -> {
                    String l = line.strip();   // Remove whitespace from start and end
                    if (l.isEmpty() || l.startsWith("#")) return; // Skip empty lines or comment lines starting with #

                    // Split line at first colon into two parts:
                    // first part is coordinates "dr,dc", second part is optional tag like "capture"
                    String[] parts = l.split(":", 2);

                    // Split the coordinates part by comma to get delta row and delta column
                    String[] coords = parts[0].split(",");

                    int dr = Integer.parseInt(coords[0].trim());  // delta row (how far vertically)
                    int dc = Integer.parseInt(coords[1].trim());  // delta column (how far horizontally)

                    // If there is a tag, trim whitespace, otherwise empty string
                    String tag = parts.length > 1 ? parts[1].strip() : "";

                    // Store this move (dr, dc) with its tag in the map
                    moves.put(new Pair(dr, dc), tag);
                });
            } catch (IOException e) {
                throw new RuntimeException(e);  // If reading file fails, stop the program with an error
            }
        }
    }

    /**
     * Checks if a move to destination cell with relative delta (dr, dc) is allowed,
     * depending on whether the destination cell is occupied by another piece.
     *
     * @param dr Relative row difference between source and destination
     * @param dc Relative column difference between source and destination
     * @param dstHasPiece true if destination cell has a piece, false if empty
     * @return true if move is valid under the rules, false otherwise
     */
    public boolean isDstCellValid(int dr, int dc, boolean dstHasPiece) {
        // Find the tag for this relative move (e.g. "capture", "non_capture", or "")
        String tag = moves.get(new Pair(dr, dc));
        if (tag == null) {
            // If no such move exists in the map, it is not allowed
            return false;
        }
        if (tag.isEmpty()) return true; // If tag is empty string, move is always allowed

        // For specific tags, check destination occupancy:
        switch (tag) {
            case "capture":
                return dstHasPiece;    // Allowed only if destination cell has a piece to capture
            case "non_capture":
                return !dstHasPiece;   // Allowed only if destination cell is empty
            default:
                return false;          // Unknown tag is invalid
        }
    }

    /**
     * Checks if the move from srcCell to dstCell is valid,
     * including board bounds, destination validity, and clear path.
     *
     * @param srcCell Source cell position as int array [row, col]
     * @param dstCell Destination cell position as int array [row, col]
     * @param occupiedCells Set of occupied cells (as classes.Pair objects)
     * @return true if move is valid, false otherwise
     */
    public boolean isValid(int[] srcCell, int[] dstCell, java.util.Set<Pair> occupiedCells) {
        int dstR = dstCell[0], dstC = dstCell[1];

        // Check if destination is inside the board limits
        if (dstR < 0 || dstR >= rows || dstC < 0 || dstC >= cols) return false;

        int dr = dstR - srcCell[0];  // Calculate relative row move
        int dc = dstC - srcCell[1];  // Calculate relative column move

        // Check if destination cell is occupied by a piece
        boolean dstHasPiece = occupiedCells.contains(new Pair(dstR, dstC));

        // Check if the move is allowed based on move type and occupancy
        if (!isDstCellValid(dr, dc, dstHasPiece)) return false;

        // Check if path between source and destination is clear (no blocking pieces)
        if (!pathIsClear(srcCell, dstCell, occupiedCells)) return false;

        // If all checks passed, move is valid
        return true;
    }

    /**
     * Helper method to check if the path from source to destination is free of pieces,
     * except for source and destination cells themselves.
     *
     * @param srcCell Source cell [row, col]
     * @param dstCell Destination cell [row, col]
     * @param occupiedCells Set of occupied cells (Pairs)
     * @return true if path is clear, false if blocked
     */
    private boolean pathIsClear(int[] srcCell, int[] dstCell, java.util.Set<Pair> occupiedCells) {
        int dr = dstCell[0] - srcCell[0];
        int dc = dstCell[1] - srcCell[1];

        // For moves to adjacent cells (distance <= 1), no blocking can occur
        if (Math.abs(dr) <= 1 && Math.abs(dc) <= 1) return true;

        int steps = Math.max(Math.abs(dr), Math.abs(dc));  // Number of steps between source and destination
        double stepR = dr / (double) steps;  // Row increment per step (fractional)
        double stepC = dc / (double) steps;  // Column increment per step (fractional)

        // Check each intermediate cell on the path (excluding source and destination)
        for (int i = 1; i < steps; i++) {
            int r = srcCell[0] + (int) Math.round(i * stepR);
            int c = srcCell[1] + (int) Math.round(i * stepC);

            // If any intermediate cell is occupied, path is blocked
            if (occupiedCells.contains(new Pair(r, c))) return false;
        }
        return true;
    }

}