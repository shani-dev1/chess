package game;

import board.Board;
import classes.Command;
import classes.Pair;
import keyBoard.KeyboardProcessor;
import keyBoard.KeyboardProducer;
import piece.Piece;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Game extends Container {

    // Custom exception to indicate the board setup is invalid
    public static class InvalidBoard extends RuntimeException {}

    // List of all pieces (game units) in the game
    public final List<Piece> pieces;

    // The main game board
    public Board board;

    // A clone of the board used for drawing/updating visuals separately
    public Board curr_board = null;

    // Game start time in nanoseconds (used for timing game events)
    private final long startNs;

    // Time factor multiplier to speed up or slow down game time (for testing)
    private long timeFactor = 1;

    // Queue for commands coming from user input (thread-safe)
    public final BlockingQueue<Command> userInputQueue = new LinkedBlockingQueue<>();

    // Map from board cells (coordinates) to pieces currently occupying them
    public final Map<Pair, List<Piece>> pos = new HashMap<>();

    // Map from piece ID (string) to piece.Piece object, for quick lookup
    public final Map<String, Piece> pieceById = new HashMap<>();

    // Fields to store selected pieces by two players (IDs of selected pieces)
    public String selected_id_1 = null;
    public String selected_id_2 = null;

    // Keyboard input processors for two players
    public KeyboardProcessor kp1;
    public KeyboardProcessor kp2;

    // נוסיף בתוך Game
    private boolean waitingForTarget1 = false;
    private boolean waitingForTarget2 = false;

    // Keyboard input producers for two players (threads generating commands)
    private KeyboardProducer kbProd1, kbProd2;

    // Constructor to initialize the game with pieces and a board
    public Game(List<Piece> pieces, Board board) {
        if (!validate(pieces)) throw new InvalidBoard(); // Validate board setup
        this.pieces = new ArrayList<>(pieces);
        this.board = board;
        this.startNs = System.nanoTime(); // Record game start time
        for (Piece p : pieces) pieceById.put(p.id, p); // Map pieces by their IDs
        this.curr_board = board.cloneBoard(); // Clone the board for drawing
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
            if (p.id.startsWith("KW")) wKing = true; // Check for white king
            if (p.id.startsWith("KB")) bKing = true; // Check for black king
        }
        return wKing && bKing; // Valid only if both kings present
    }

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

        // this._draw(); // Draw initial state
        // this._show(); // Show initial board

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

            // grafix.Graphics update (commented out here)
            // if (withGraphics) {
            // this._drow();
            // this._show();
            // }

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

     public void _process_input(Command cmd) {
         Piece mover = pieceById.get(cmd.pieceId);
         if (mover == null) return;
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

    public void startUserInputThread() {
        Map<String, String> p1Map = Map.of(
                "up", "up", "down", "down", "left", "left", "right", "right",
                "enter", "select", "+", "jump"
        );
        Map<String, String> p2Map = Map.of(
                "w", "up", "s", "down", "a", "left", "d", "right",
                "f", "select", "g", "jump"
        );
        Map<Integer, Piece>[][] boardState = createBoardState();

        kp1 = new KeyboardProcessor(board.getHCells(), board.getWCells(), p1Map, userInputQueue, boardState);
        kp2 = new KeyboardProcessor(board.getHCells(), board.getWCells(), p2Map, userInputQueue, boardState);

        kp1.setCursor(1, 0, board.getWCells() - 1);
        kp2.setCursor(2, board.getHCells() - 1, 0);

        kbProd1 = new KeyboardProducer(this, userInputQueue, kp1, 1);
        kbProd2 = new KeyboardProducer(this, userInputQueue, kp2, 2);

        kbProd1.start();
        kbProd2.start();
    }


//    // Starts threads that listen for keyboard input from two players
//    public void startUserInputThread() {
//        // Key mappings for player 1
//        Map<String, String> p1Map = Map.of(
//                "up", "up", "down", "down", "left", "left", "right", "right",
//                "enter", "select", "+", "jump"
//        );
//
//        // Key mappings for player 2
//        Map<String, String> p2Map = Map.of(
//                "w", "up", "s", "down", "a", "left", "d", "right",
//                "f", "select", "g", "jump"
//        );
//
//        // Create keyboard processors for both players
//        kp1 = new KeyboardProcessor(board.getHCells(), board.getWCells(), p1Map);
//        kp2 = new KeyboardProcessor(board.getHCells(), board.getWCells(), p2Map);
//
//        kp1.setCursor(0, board.getWCells() - 1);
//        kp2.setCursor(board.getHCells() - 1, 0);
//
//        // Create producers that will read input and push commands into the queue
//        kbProd1 = new KeyboardProducer(this, userInputQueue, kp1, 1);
//        kbProd2 = new KeyboardProducer(this, userInputQueue, kp2, 2);
//
//        // Start the input threads
//        kbProd1.start();
//        kbProd2.start();
//    }

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
    public void _draw() {
        curr_board.getImg();

        for (Piece p : pieces) {
            p.drawOnBoard(curr_board, game_time_ms());
        }

        // drawCursors();
    }

    // private void drawCursors() {
    //     // Draw cursor for player 1 (green)
    //     int[] cursor1 = kp1.getCursor();
    //     int y1 = cursor1[0] * board.getCellHPix();
    //     int x1 = cursor1[1] * board.getCellWPix();
    //     int y2 = y1 + board.getCellHPix() - 1;
    //     int x2 = x1 + board.getCellWPix() - 1;
    //     curr_board.getImg().drawRect(x1, y1, x2, y2, Color.GREEN);
    //
    //     // Draw cursor for player 2 (red)
    //     int[] cursor2 = kp2.getCursor();
    //     y1 = cursor2[0] * board.getCellHPix();
    //     x1 = cursor2[1] * board.getCellWPix();
    //     y2 = y1 + board.getCellHPix() - 1;
    //     x2 = x1 + board.getCellWPix() - 1;
    //     curr_board.getImg().drawRect(x1, y1, x2, y2, Color.RED);
    // }

    // public void _show() {
    //     if (curr_board != null) {
    //         this.curr_board.show();
    //     }
    // }

    public List<Piece> getPieces() {
        return pieces;
    }

    public BufferedImage getCurrentBoardImage() {
        _draw();
        return curr_board.getImg().get();
    }

    @SuppressWarnings("unchecked")
    private Map<Integer, Piece>[][] createBoardState() {
        int rows = board.getHCells();
        int cols = board.getWCells();
        Map<Integer, Piece>[][] boardState = new Map[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                boardState[r][c] = new HashMap<>();
            }
        }

        // מלא את המצב עם הכלים על הלוח
        for (Piece p : pieces) {
            Pair cell = p.currentCell();
            int x = cell.r;
            int y = cell.c;

            int player = (p.id.charAt(1) == 'W') ? 1 : 2;
            boardState[x][y].put(player, p);
        }
        return boardState;
    }

}
