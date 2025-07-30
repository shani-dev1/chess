package physics;

import physics.IdlePhysics.IdlePhysics;
import physics.IdlePhysics.JumpPhysics;
import physics.IdlePhysics.MovePhysics;
import physics.IdlePhysics.RestPhysics;
import board.Board;
import classes.Pair;
import org.json.JSONObject;

public class PhysicsFactory {
    private final Board board;  // Reference to the game board, needed for position calculations

    // Constructor: receives the board.Board instance to use for physics calculations
    public PhysicsFactory(Board board) {
        this.board = board;
    }

    /**
     * Creates a physics.Physics object based on the given start cell, state name, and configuration JSON.
     *
     * @param startCell The starting cell position on the board (row, col) as a classes.Moves.classes.Pair
     * @param stateName The name of the current state, e.g. "move", "jump", "rest", etc.
     * @param cfg Configuration parameters in JSON format, which can include speed and duration
     * @return A physics.Physics instance appropriate for the given state
     */
    public Physics create(Pair startCell, String stateName, JSONObject cfg) {
        // Read the movement speed from config, default to 0.0 if missing
        double speed = cfg.optDouble("speed_m_per_sec", 0.0);

        Physics phys;  // The physics.Physics object to be created
        String name = stateName.toLowerCase();  // Convert stateName to lowercase for easy comparison

        // Choose physics.Physics subclass based on state name pattern
        if (name.equals("move") || name.endsWith("_move")) {
            // For "move" states, create a IdlePhysics.IdlePhysics.MovePhysics with speed and board
            phys = new MovePhysics(board, speed);
        } else if (name.equals("jump")) {
            // For "jump" state, create IdlePhysics.IdlePhysics.JumpPhysics with duration (converted from ms to seconds)
            phys = new JumpPhysics(board, cfg.optDouble("duration_ms", 100) / 1000.0);
        } else if (name.endsWith("rest") || name.equals("rest")) {
            // For resting states, create IdlePhysics.IdlePhysics.RestPhysics with duration (ms to seconds)
            phys = new RestPhysics(board, cfg.optDouble("duration_ms", 3000) / 1000.0);
        } else {
            // Default fallback: IdlePhysics.IdlePhysics for any other state
            phys = new IdlePhysics(board);
        }

        // Initialize the position of the physics object at the starting cell
        phys.startCell = startCell;

        // Convert the cell coordinates to meters (or units) and set current position
        phys.currPosM = board.cellToM(startCell);

        return phys;  // Return the created and initialized physics.Physics instance
    }
}
