import java.util.*;

// Represents a single state of a game piece (e.g., "idle", "move", "jump", etc.)
public class State {
    // Optional: Allowed move logic in this state (could be null for states like idle)
    public Moves moves;

    // Responsible for visual representation of the piece in this state
    public Graphics graphics;

    // Responsible for the physics (position, collision, etc.) of the piece in this state
    public Physics physics;

    // Map of event type (like "move", "jump") to next state
    private final Map<String, State> transitions = new HashMap<>();

    // The name of this state (for debugging/logging)
    public String name;

    // Constructor
    public State(Moves moves, Graphics graphics, Physics physics) {
        this.moves = moves;
        this.graphics = graphics;
        this.physics = physics;
    }

    // ToString for debugging/logging: e.g., "State(idle)"
    @Override
    public String toString() {
        return "State(" + name + ")";
    }

    // Define a state transition for a specific command (event)
    public void setTransition(String event, State target) {
        transitions.put(event.toLowerCase(), target);
    }

    // Get the transition map
    public Map<String, State> getTransitions() {
        return transitions;
    }

    // Reset graphics and physics components when entering this state
    public void reset(Command cmd) {
        graphics.reset(cmd);
        physics.reset(cmd);
    }

    // Handle an incoming command (like "move", "jump", "idle")
    public State onCommand(Command cmd, java.util.Map<Moves.Pair, java.util.List<Piece>> cell2piece) {
        String key = cmd.type.toLowerCase();  // Normalize command type (e.g., "MOVE" → "move")

        // Get the next state for this command
        State next = transitions.get(key);
        if (next == null) return this;   // No such transition → stay in current state

        // Reject invalid move command that doesn't have source & destination
        if ("move".equals(key) && (cmd.params == null || cmd.params.size() < 2)) {
            return this;
        }

        // If it's a move command and legality needs to be checked...
        if ("move".equals(key) && moves != null && cmd.params != null && cmd.params.size() >= 2 && cell2piece != null) {
            Moves.Pair src = (Moves.Pair) cmd.params.get(0);  // Source cell
            Moves.Pair dst = (Moves.Pair) cmd.params.get(1);  // Destination cell

            // Reject if the source cell doesn't match where the piece actually is
            if (!src.equals(physics.getCurrCell())) {
                return this; // Possibly a stale or wrong move
            }

            // Get occupied cells to check for legal path
            java.util.Set<Moves.Pair> occupied = cell2piece.keySet();
            int[] srcArr = new int[]{src.r, src.c};
            int[] dstArr = new int[]{dst.r, dst.c};

            // Reject if move is illegal (blocked or invalid)
            if (!moves.isValid(srcArr, dstArr, occupied)) {
                return this;
            }
        }

        // If passed all checks, move to the next state
        next.reset(cmd);
        return next;
    }

    // Called every frame / tick – updates physics, and may return internal command
    public State update(long nowMs) {
        Command internal = physics.update(nowMs);
        if (internal != null) {
            // Run internal command like "long_rest → idle"
            return onCommand(internal, null);
        }
        return this;
    }

    // Can this piece be captured in this state?
    public boolean canBeCaptured() { return physics.canBeCaptured(); }

    // Can this piece capture other pieces in this state?
    public boolean canCapture() { return physics.canCapture(); }
}
