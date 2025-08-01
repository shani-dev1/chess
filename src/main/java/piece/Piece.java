package piece;

import classes.Command;
import classes.Pair;
import classes.State;
import enums.EState;
import img.Img;
import board.Board;

import java.util.*;

public class Piece {
    public final String id;     // Unique identifier for this piece (e.g., "KW1" for white king 1)
    public State state;         // Current state of the piece (includes physics, graphics, behavior)

    // Constructor: initializes the piece with an ID and an initial classes.State object
    public Piece(String pieceId, State initState) {
        this.id = pieceId;
        this.state = initState;
    }

    /**
     * Processes a game command targeting this piece, possibly changing its state.
     *
     * @param cmd        classes.Command to process (e.g., move, jump, idle, done)
     * @param cell2piece Mapping of board cells to list of pieces occupying them (for context)
     */
    public void onCommand(Command cmd, Map<Pair, List<Piece>> cell2piece) {
        // Delegate to current state to handle command and possibly return new state
        state = state.onCommand(cmd, cell2piece);
    }

    /**
     * Resets the piece to an "idle" state at the current position.
     *
     * @param startMs The starting timestamp (milliseconds since game start)
     */
    public void reset(long startMs) {
        Pair currentPos = state.physics.getCurrCell(); // Get current cell coordinates
        // Create a new idle command at current position and reset state accordingly
        state.reset(new Command(startMs, id, EState.IDLE, List.of(currentPos)));
    }

    /**
     * Updates the piece state based on the current time.
     *
     * @param nowMs Current time in milliseconds since game start
     */
    public void update(long nowMs) {
        // Update state and assign any new resulting state
        state = state.update(nowMs);
    }

    /**
     * Checks whether this piece blocks movement, according to its physics state.
     *
     * @return true if this piece blocks movement, false otherwise
     */
    public boolean isMovementBlocker() {
        return state.physics.isMovementBlocker();
    }

    /**
     * Draws the piece on the given board at the piece’s current pixel position.
     *
     * @param board The board to draw onto
     * @param nowMs Current time (used for updating graphics if needed)
     */
    public void drawOnBoard(Board board, long nowMs) {
        // Optionally update graphics frame (commented out in this code)
        //state.graphics.update(nowMs);

        // Get pixel position of the piece on the board
        int[] posPix = state.physics.getPosPix();

        // Get the current sprite image to draw from the graphics state
        Img sprite = state.graphics.getImg();

        sprite.drawOn(board.getImg(), posPix[0], posPix[1]);
    }



    /**
     * Returns the current cell coordinates of this piece on the board.
     *
     * @return The current board cell as a classes.Moves.classes.Pair (row, col)
     */
    public Pair currentCell() {
        return state.physics.getCurrCell();
    }

    public String getId() {
        return id;
    }
}
