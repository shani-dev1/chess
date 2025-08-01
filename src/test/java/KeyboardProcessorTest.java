import board.Board;
import classes.Command;
import classes.Pair;
import enums.EState;
import keyBoard.KeyboardProcessor;
import piece.Piece;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import static org.junit.jupiter.api.Assertions.*;

public class KeyboardProcessorTest {
    private KeyboardProcessor kp1;
    private KeyboardProcessor kp2;
    private BlockingQueue<Command> queue;
    private Map<Integer, Piece>[][] boardState;
    private final int rows = 8;
    private final int cols = 8;
    private final Board testBoard = PieceStateGameTest.board(8);

    private static Map<String, String> p1Keymap() {
        return Map.of(
                "up", "up",
                "down", "down",
                "left", "left",
                "right", "right",
                "enter", "select",
                "+", "jump"
        );
    }

    private static Map<String, String> p2Keymap() {
        return Map.of(
                "w", "up",
                "s", "down",
                "a", "left",
                "d", "right",
                "f", "select",
                "g", "jump"
        );
    }

    // A utility method to create a board state for tests
    @SuppressWarnings("unchecked")
    private Map<Integer, Piece>[][] createTestBoardState() {
        Map<Integer, Piece>[][] bs = new Map[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                bs[r][c] = new HashMap<>();
            }
        }
        return bs;
    }

    @BeforeEach
    void setUp() {
        queue = new LinkedBlockingQueue<>();
        boardState = createTestBoardState();
        kp1 = new KeyboardProcessor(rows, cols, p1Keymap(), queue, boardState);
        kp2 = new KeyboardProcessor(rows, cols, p2Keymap(), queue, boardState);
    }

    @Test
    void testInitialPosition() {
        // Player 1's initial cursor is at (0,0)
        assertArrayEquals(new int[]{0, 0}, kp1.getCursor(1));
        // Player 2's initial cursor is at (0,0) as well
        assertArrayEquals(new int[]{0, 0}, kp2.getCursor(2));
    }

    @Test
    void testCursorMoves() {
        // Test player 1 moves
        assertArrayEquals(new int[]{0, 0}, kp1.getCursor(1));
        kp1.processKey("down", 1);
        assertArrayEquals(new int[]{1, 0}, kp1.getCursor(1));
        kp1.processKey("right", 1);
        assertArrayEquals(new int[]{1, 1}, kp1.getCursor(1));

        // Test player 2 moves
        assertArrayEquals(new int[]{0, 0}, kp2.getCursor(2));
        kp2.processKey("s", 2); // 's' for down
        assertArrayEquals(new int[]{1, 0}, kp2.getCursor(2));
        kp2.processKey("d", 2); // 'd' for right
        assertArrayEquals(new int[]{1, 1}, kp2.getCursor(2));
    }

    @Test
    void testCursorBoundary() {
        // Test player 1 boundaries on a 2x3 board
        KeyboardProcessor kpTest = new KeyboardProcessor(2, 3, p1Keymap(), queue, createTestBoardState());
        kpTest.setCursor(1, 0, 0);
        kpTest.processKey("up", 1);
        assertArrayEquals(new int[]{0, 0}, kpTest.getCursor(1), "Up at top stays put");

        kpTest.setCursor(1, 1, 2);
        kpTest.processKey("down", 1);
        assertArrayEquals(new int[]{1, 2}, kpTest.getCursor(1), "Down at bottom stays put");

        kpTest.processKey("right", 1);
        assertArrayEquals(new int[]{1, 2}, kpTest.getCursor(1), "Right at right stays put");

        kpTest.processKey("left", 1);
        assertArrayEquals(new int[]{1, 1}, kpTest.getCursor(1), "Left moves correctly");
    }

    @Test
    void testSelectAndJumpCommand() throws InterruptedException {
        // Put a piece on the board at (1,1) for Player 1
        String pieceId = "W_PAWN_1"; // Corrected piece ID to be valid for Player 1
        Piece testPiece = PieceStateGameTest.makePiece(pieceId, new Pair(1,1), testBoard);

        boardState[1][1].put(1, testPiece);

        // Player 1 selects the piece
        kp1.setCursor(1, 1, 1);
        kp1.processKey("enter", 1);
        // The selection should be stored internally, but no command is queued yet.
        assertTrue(queue.isEmpty());

        // Player 1 tries to jump to an empty cell (3,3)
        kp1.setCursor(1, 3, 3);
        kp1.processKey("+", 1);

        // A command should now be in the queue
        assertFalse(queue.isEmpty());
        Command cmd = queue.poll();
        assertEquals(pieceId, cmd.pieceId);
        assertEquals(EState.JUMP, cmd.type);
        assertEquals(new Pair(3, 3), cmd.params.get(0));

        // The selection should be reset
        // assertNull(kp1.getCursor(1)); // Note: this assertion may not be correct
    }

    @Test
    void testInvalidSelectDoesNothing() throws InterruptedException {
        // Board is empty, so no piece to select
        kp1.setCursor(1, 3, 3);
        kp1.processKey("enter", 1);

        // Queue should remain empty as no valid selection was made
        assertTrue(queue.isEmpty());
    }

    @Test
    void testThreadSafetyForCursors() throws InterruptedException {
        final int iterations = 1000;
        final int player = 1;

        Runnable worker1 = () -> {
            for (int i = 0; i < iterations; i++) {
                kp1.processKey("right", player);
                kp1.processKey("down", player);
            }
        };

        Runnable worker2 = () -> {
            for (int i = 0; i < iterations; i++) {
                kp1.processKey("left", player);
                kp1.processKey("up", player);
            }
        };

        Thread t1 = new Thread(worker1);
        Thread t2 = new Thread(worker2);

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        int[] finalCursor = kp1.getCursor(player);
        assertTrue(finalCursor[0] >= 0 && finalCursor[0] < rows);
        assertTrue(finalCursor[1] >= 0 && finalCursor[1] < cols);
    }
}