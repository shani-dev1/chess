import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Game {
    // Custom exception to indicate the board setup is invalid
    public static class InvalidBoard extends RuntimeException {}

    // List of all pieces (game units) in the game
    public final List<Piece> pieces;

    // The main game board
    public Board board;

    // A clone of the board used for drawing/updating visuals separately
    Board curr_board = null;

    // Game start time in nanoseconds (used for timing game events)
    private final long startNs;

    // Time factor multiplier to speed up or slow down game time (for testing)
    private long timeFactor = 1;

    // Queue for commands coming from user input (thread-safe)
    public final BlockingQueue<Command> userInputQueue = new LinkedBlockingQueue<>();

    // Map from board cells (coordinates) to pieces currently occupying them
    public final Map<Pair, List<Piece>> pos = new HashMap<>();

    // Map from piece ID (string) to Piece object, for quick lookup
    public final Map<String, Piece> pieceById = new HashMap<>();

    // Fields to store selected pieces by two players (IDs of selected pieces)
    public String selected_id_1 = null;
    public String selected_id_2 = null;

    // Keyboard input processors for two players
    private KeyboardProcessor kp1, kp2;

    // Keyboard input producers for two players (threads generating commands)
    private KeyboardProducer kbProd1, kbProd2;

    // Constructor to initialize the game with pieces and a board
    public Game(List<Piece> pieces, Board board) {
        if (!validate(pieces)) throw new InvalidBoard();  // Validate board setup
        this.pieces = new ArrayList<>(pieces);
        this.board = board;
        this.startNs = System.nanoTime();  // Record game start time
        for (Piece p : pieces) pieceById.put(p.id, p);  // Map pieces by their IDs
        this.curr_board = board.cloneBoard();           // Clone the board for drawing
    }

    // Validate the initial pieces setup:
    // - no two pieces of the same side on the same cell
    // - both white and black kings exist on the board
    private boolean validate(List<Piece> pieces) {
        Map<Pair, Character> occupantSide = new HashMap<>();
        boolean wKing = false, bKing = false;
        for (Piece p : pieces) {
            Pair cell = p.currentCell();
            char side = p.id.charAt(1); // 'W' or 'B' indicating white or black
            Character prev = occupantSide.get(cell);
            if (prev != null && prev == side) {
                return false; // Found duplicate piece of same side on cell
            }
            occupantSide.put(cell, side);
            if (p.id.startsWith("KW")) wKing = true;  // Check for white king
            if (p.id.startsWith("KB")) bKing = true;  // Check for black king
        }
        return wKing && bKing;  // Valid only if both kings present
    }

    // Return a clone of the main board (used for rendering etc.)
    public Board clone_board() {
        return this.board.cloneBoard();
    }

    // Return the elapsed game time in milliseconds, adjusted by timeFactor
    public long game_time_ms() {
        return ((System.nanoTime() - startNs) / 1_000_000) * timeFactor;
    }

    // Set the time speed multiplier (1 = normal speed)
    public void setTimeFactor(long factor) {
        this.timeFactor = factor;
    }

    /* ---------------- win detection --------------- */
    // Check if the game is won: win when fewer than 2 kings remain on board
    public boolean _is_win() {
        long kings = pieces.stream()
                .filter(p -> p.id.startsWith("KW") || p.id.startsWith("KB"))
                .count();
        return kings < 2;
    }

    /* ---------------- simplified game loop (no graphics) ------------ */
    // Runs the main game loop for numIterations (0 = infinite)
    // If withGraphics=true, it would update visuals (code commented out here)
    public void _run_game_loop(int numIterations, boolean withGraphics) {
        int counter = 0;
        this._draw();  // Draw initial state
        this._show();  // Show initial board

        while (!_is_win()) {
            long now = game_time_ms();

            // Update each piece with current time
            for (Piece p : new ArrayList<>(pieces)) {
                p.update(now);
            }

            // Update the mapping of cells to pieces
            _update_cell2piece_map();

            // Process all pending user input commands
            while (!userInputQueue.isEmpty()) {
                Command cmd = userInputQueue.poll();
                _process_input(cmd);
            }

            // Graphics update (commented out here)
//            if (withGraphics) {
//                this._drow();
//                this._show();
//            }

            // Handle collisions and piece captures on the board
            _resolve_collisions();

            // Stop if number of iterations reached
            if (numIterations > 0 && ++counter >= numIterations) {
                break;
            }
        }
    }

    // Updates the 'pos' map from board cells to pieces occupying those cells
    public void _update_cell2piece_map() {
        pos.clear();
        for (Piece p : pieces) {
            pos.computeIfAbsent(p.currentCell(), k -> new ArrayList<>()).add(p);
        }
    }

    // Process a single input command affecting pieces on the board
    public void _process_input(Command cmd) {
        Piece mover = pieceById.get(cmd.pieceId);
        if (mover == null) return; // Ignore commands for non-existing pieces
        mover.onCommand(cmd, pos);
    }

    // Resolve collisions: if multiple pieces occupy the same cell,
    // only the "winner" (most recent mover) remains, others removed if capturable
    public void _resolve_collisions() {
        Map<Pair, List<Piece>> occupied = new HashMap<>();
        for (Piece p : pieces) {
            occupied.computeIfAbsent(p.currentCell(), k -> new ArrayList<>()).add(p);
        }
        for (Map.Entry<Pair, List<Piece>> entry : occupied.entrySet()) {
            List<Piece> plist = entry.getValue();
            if (plist.size() < 2) continue; // No collision if less than 2 pieces

            // Determine the winner based on physics start time (most recent)
            Piece winner = plist.stream()
                    .max(Comparator.comparingLong(p -> p.state.physics.getStartMs()))
                    .orElse(null);
            if (winner == null || !winner.state.canCapture()) continue;

            // Remove all other capturable pieces on the cell except the winner
            for (Piece p : new ArrayList<>(plist)) {
                if (p == winner) continue;
                if (p.state.canBeCaptured()) pieces.remove(p);
            }
        }
    }

    /* ---------------- keyboard helpers ---------------- */
    // Starts threads that listen for keyboard input from two players
    public void startUserInputThread() {
        // Key mappings for player 1
        Map<String, String> p1Map = Map.of(
                "up", "up", "down", "down", "left", "left", "right", "right",
                "enter", "select", "+", "jump"
        );
        // Key mappings for player 2
        Map<String, String> p2Map = Map.of(
                "w", "up", "s", "down", "a", "left", "d", "right",
                "f", "select", "g", "jump"
        );

        // Create keyboard processors for both players
        kp1 = new KeyboardProcessor(board.getHCells(), board.getWCells(), p1Map);
        kp2 = new KeyboardProcessor(board.getHCells(), board.getWCells(), p2Map);

        // Create producers that will read input and push commands into the queue
        kbProd1 = new KeyboardProducer(this, userInputQueue, kp1, 1);
        kbProd2 = new KeyboardProducer(this, userInputQueue, kp2, 2);

        // Start the input threads
        kbProd1.start();
        kbProd2.start();
    }

    // Announces which player won based on remaining kings
    private void _announce_win() {
        boolean blackWins = pieces.stream().anyMatch(p -> p.id.startsWith("KB"));
        String text = blackWins ? "Black wins!" : "White wins!";
        System.out.println(text);
    }

    // Accessors for testing or external control of keyboard producers
    public KeyboardProducer getKbProd1() { return kbProd1; }
    public KeyboardProducer getKbProd2() { return kbProd2; }

    // Stops the keyboard input threads
    public void stopUserInputThreads() {
        if (kbProd1 != null) kbProd1.stopProducer();
        if (kbProd2 != null) kbProd2.stopProducer();
    }

    // Runs the full game (starts input, resets pieces, and runs the game loop)
    public void run() {
        startUserInputThread();

        long startMs = game_time_ms();

        // Debug print initial piece states
        for (Piece p : pieces) {
            System.out.println("id " + p.id + "  startCell  " + p.state.physics.startCell + "  endCell  " + p.state.physics.endCell);
        }

        // Reset pieces to initial state at start time
        for (Piece p : pieces) {
            p.reset(startMs);
        }

        // Run the main game loop indefinitely (0 = infinite iterations)
        _run_game_loop(0, true);

        // Announce winner and stop input threads
        _announce_win();
        if (kbProd1 != null) kbProd1.stopProducer();
        if (kbProd2 != null) kbProd2.stopProducer();
    }

    // Draw current game state on the cloned board
    private void _draw() {
        curr_board.getImg();

        for (Piece p : pieces) {
            p.drawOnBoard(curr_board, game_time_ms());
        }

        // The following commented code is a plan for drawing selection rectangles
        // and printing logs when markers move (not implemented here):

//        r, c = kp.get_cursor()
//                # draw rectangle
//        y1 = r * self.board.cell_H_pix
//        x1 = c * self.board.cell_W_pix
//        y2 = y1 + self.board.cell_H_pix - 1
//        x2 = x1 + self.board.cell_W_pix - 1
//        color = (0, 255, 0) if player == 1 else (255, 0, 0)
//        self.curr_board.img.draw_rect(x1, y1, x2, y2, color)
//
//                # only print if moved
//                prev = getattr(self, last)
//        if prev != (r, c):
//        logger.debug("Marker P%s moved to (%s, %s)", player, r, c)
//        setattr(self, last, (r, c))

    }

    // Show the current board visually (calls show method on curr_board's image)
    public void _show() {
        if (curr_board != null) {
            this.curr_board.show();
        }
    }

    // Getter for the list of pieces
    public List<Piece> getPieces() {
        return pieces;
    }
}
