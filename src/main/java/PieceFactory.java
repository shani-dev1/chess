import java.nio.file.*;
import java.util.*;
import java.awt.Dimension;
import org.json.*;

public class PieceFactory {
    private final Board board;               // Reference to the game board, needed for size info and physics
    private final GraphicsFactory gfxFactory; // Factory object used to create Graphics instances for pieces
    private final PhysicsFactory physFactory; // Factory object used to create Physics instances for pieces
    private final Map<String, State> templates = new HashMap<>(); // Map storing piece templates, keyed by piece type name

    // Constructor initializes the factories and saves the board reference
    public PieceFactory(Board board) {
        this.board = board;
        this.gfxFactory = new GraphicsFactory();
        this.physFactory = new PhysicsFactory(board);
    }

    /**
     * Loads all piece templates from a root directory (piecesRoot).
     * Each subdirectory represents a piece type and should contain state definitions.
     */
    public void generateLibrary(Path piecesRoot) throws Exception {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(piecesRoot)) {
            for (Path sub : stream) {
                if (Files.isDirectory(sub)) {
                    // For each piece type folder, build its state machine and save as template
                    templates.put(sub.getFileName().toString(), buildStateMachine(sub));
                }
            }
        }
    }

    /**
     * Constructs the state machine for one piece type by loading its states, graphics, physics, and transitions.
     * Each state corresponds to a subfolder inside the piece's "states" directory.
     */
    private State buildStateMachine(Path pieceDir) throws Exception {
        int W = board.getWCells();       // Number of horizontal cells on the board
        int H = board.getHCells();       // Number of vertical cells on the board
        Dimension cellPx = new Dimension(board.getCellWPix(), board.getCellHPix()); // Size of each cell in pixels

        Map<String, State> states = new HashMap<>(); // Holds states keyed by their name
        Path statesDir = pieceDir.resolve("states"); // Path to states directory

        if (!Files.exists(statesDir))
            throw new IllegalStateException("Missing states dir: " + statesDir);

        // Iterate over state folders inside "states"
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(statesDir)) {
            for (Path stateDir : stream) {
                if (!Files.isDirectory(stateDir)) continue; // Skip files, process only directories

                String name = stateDir.getFileName().toString(); // Name of the state (e.g. "idle", "move")

                // Load config.json for this state (might contain settings for graphics, physics etc.)
                JSONObject cfg = readJson(stateDir.resolve("config.json"));

                // Load moves from moves.txt if it exists (optional)
                Path movesPath = stateDir.resolve("moves.txt");
                Moves moves = Files.exists(movesPath) ? new Moves(movesPath, H, W) : null; // note: dimensions swapped

                // Load graphics settings and images for the state
                JSONObject gfxCfg = cfg.optJSONObject("graphics") != null ? cfg.optJSONObject("graphics") : new JSONObject();
                Graphics gfx = gfxFactory.load(stateDir.resolve("sprites"), gfxCfg, cellPx);

                // Create physics behavior for the state
                JSONObject physCfg = cfg.optJSONObject("physics") != null ? cfg.optJSONObject("physics") : new JSONObject();
                Physics phys = physFactory.create(new Moves.Pair(0, 0), name, physCfg);

                // Create State object combining moves, graphics, and physics
                State st = new State(moves, gfx, phys);
                st.name = name;

                // Save state to map by name
                states.put(name, st);
            }
        }

        // ─── Load state transitions from transitions.csv if it exists ───────────────
        Path transCsv = pieceDir.resolve("transitions.csv");
        if (Files.exists(transCsv)) {
            List<String> lines = Files.readAllLines(transCsv);
            for (String line : lines) {
                String l = line.strip();
                // Skip comments, empty lines, or header line
                if (l.isEmpty() || l.startsWith("#") || l.toLowerCase().startsWith("from_state")) continue;

                String[] parts = l.split(",");
                if (parts.length < 3) continue;

                String frm = parts[0].trim();           // From state
                String event = parts[1].trim().toLowerCase(); // Event name triggering transition
                String nxt = parts[2].trim();           // To state

                State src = states.get(frm);
                State dst = states.get(nxt);

                // Add transition event->destination if both states exist
                if (src != null && dst != null) {
                    src.setTransition(event, dst);
                }
            }
        }

        // If idle state exists but no transitions set, add basic fallback transitions to move and jump states if they exist
        State idle = states.get("idle");
        if (idle != null && idle.getTransitions().isEmpty()) {
            if (states.containsKey("move")) idle.setTransition("move", states.get("move"));
            if (states.containsKey("jump")) idle.setTransition("jump", states.get("jump"));
        }

        // Return idle state as the entry point of this piece's state machine
        return idle;
    }

    // Helper method: safely read a JSON object from a file path
    private static JSONObject readJson(Path p) {
        try {
            if (Files.exists(p))
                return new JSONObject(Files.readString(p));
        } catch (Exception ignored) {}
        return new JSONObject(); // Return empty JSON if reading fails or file missing
    }

    /**
     * Creates a new Piece instance from a piece type code and starting cell.
     * Clones the state machine template and initializes it for the given cell.
     */
    public Piece createPiece(String code, Moves.Pair cell) {
        State tmpl = templates.get(code);
        if (tmpl == null)
            throw new IllegalArgumentException("Unknown piece type " + code);

        // Clone the template's state machine, configuring physics to start at the given cell
        State idleClone = cloneStateMachine(tmpl, cell);

        // Create the Piece with a unique id (code + cell) and cloned state machine
        Piece piece = new Piece(code + "_" + cell, idleClone);

        // Reset piece state and set initial idle command with start time zero
        piece.reset(0);
        idleClone.reset(new Command(0, piece.id, "idle", List.of(cell)));

        return piece;
    }

    /**
     * Deep clones a state machine starting from the idle state template.
     * For each state, clones its moves, graphics, and creates new physics with the starting cell.
     * Reconstructs all transitions to link to cloned states.
     */
    private State cloneStateMachine(State templateIdle, Moves.Pair cell) {
        Map<State, State> map = new HashMap<>();
        Deque<State> stack = new ArrayDeque<>();
        stack.push(templateIdle);

        // Traverse states in DFS order to clone them
        while (!stack.isEmpty()) {
            State orig = stack.pop();
            if (map.containsKey(orig)) continue;

            // Clone state with same moves and graphics, but new physics starting at 'cell'
            State copy = new State(orig.moves, orig.graphics, physFactory.create(cell, orig.name, new JSONObject()));
            copy.name = orig.name;

            map.put(orig, copy);
            // Add all target states of transitions to stack for cloning
            stack.addAll(orig.getTransitions().values());
        }

        // Re-link transitions in cloned states to point to cloned targets
        for (Map.Entry<State, State> e : map.entrySet()) {
            State orig = e.getKey();
            State copy = e.getValue();
            for (Map.Entry<String, State> tr : orig.getTransitions().entrySet()) {
                copy.setTransition(tr.getKey(), map.get(tr.getValue()));
            }
        }

        // Return the cloned idle state as root of the cloned machine
        return map.get(templateIdle);
    }
}
