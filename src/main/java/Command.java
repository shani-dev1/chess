import java.util.List;

/**
 * A Command represents an instruction sent to a game piece (like a soldier).
 * It includes the time, target piece, command type, and any additional parameters.
 */
public class Command {

    // Time when the command should be executed, in milliseconds since the game started
    public final long timestamp;

    // The ID of the piece this command applies to (e.g., "BW1", "BB2", etc.)
    public final String pieceId;

    // Type of command: could be "move", "jump", "idle", "done", etc.
    public final String type;

    // List of additional parameters for the command.
    // The contents depend on the command type (e.g., direction, distance, etc.)
    public final List<Object> params;

    public Command(long timestamp, String pieceId, String type, List<Object> params) {
        this.timestamp = timestamp;
        this.pieceId = pieceId;
        this.type = type;
        this.params = params;
    }
}
