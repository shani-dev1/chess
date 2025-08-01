package keyBoard;

import classes.Command;
import classes.Pair;
import enums.EState;
import piece.Piece;

import java.util.*;
import java.util.concurrent.BlockingQueue;

public class KeyboardProcessor {
    private final int rows, cols;
    private final Map<String, String> keyMap;
    private final BlockingQueue<Command> queue;
    private final Map<Integer, String> selectedPieceMap = new HashMap<>();

    private int cursorX1 = 0, cursorY1 = 0;
    private int cursorX2 = 0, cursorY2 = 0;

    private final Map<Integer, Piece>[][] boardState;

    public KeyboardProcessor(int rows, int cols, Map<String, String> keyMap,
                             BlockingQueue<Command> queue,
                             Map<Integer, Piece>[][] boardState) {
        this.rows = rows;
        this.cols = cols;
        this.keyMap = keyMap;
        this.queue = queue;
        this.boardState = boardState;
    }

    public synchronized int[] getCursor(int player) {
        return (player == 1) ? new int[]{cursorX1, cursorY1} : new int[]{cursorX2, cursorY2};
    }

    public synchronized void setCursor(int player, int x, int y) {
        x = Math.max(0, Math.min(rows - 1, x));
        y = Math.max(0, Math.min(cols - 1, y));
        if (player == 1) {
            cursorX1 = x;
            cursorY1 = y;
        } else {
            cursorX2 = x;
            cursorY2 = y;
        }
    }

    public synchronized void moveCursor(int player, String direction) {
        if (player == 1) {
            switch (direction) {
                case "up" -> cursorX1 = Math.max(0, cursorX1 - 1);
                case "down" -> cursorX1 = Math.min(rows - 1, cursorX1 + 1);
                case "left" -> cursorY1 = Math.max(0, cursorY1 - 1);
                case "right" -> cursorY1 = Math.min(cols - 1, cursorY1 + 1);
            }
        } else {
            switch (direction) {
                case "up" -> cursorX2 = Math.max(0, cursorX2 - 1);
                case "down" -> cursorX2 = Math.min(rows - 1, cursorX2 + 1);
                case "left" -> cursorY2 = Math.max(0, cursorY2 - 1);
                case "right" -> cursorY2 = Math.min(cols - 1, cursorY2 + 1);
            }
        }
    }

    public void processKey(String key, int player) {
        String action = keyMap.getOrDefault(key, "");
        if (action.isEmpty()) return;

        int[] cursor = getCursor(player);
        int x = cursor[0];
        int y = cursor[1];

        switch (action) {
            case "up", "down", "left", "right" -> {
                moveCursor(player, action);
                int[] newCursor = getCursor(player);
                System.out.printf("Player %d moved cursor to: [%d,%d]%n", player, newCursor[0], newCursor[1]);
            }

            case "select" -> {
                Piece piece = getPieceAt(x, y, player);
                if (piece != null && belongsToPlayer(piece.getId(), player)) {
                    selectedPieceMap.put(player, piece.getId());
                    System.out.printf("Player %d selected piece: %s%n", player, piece.getId());
                } else {
                    System.out.printf("Player %d attempted to select an invalid piece at [%d,%d]%n", player, x, y);
                }
            }

            case "jump" -> {
                String selectedId = selectedPieceMap.get(player);
                if (selectedId != null) {
                    List<Object> params = new ArrayList<>();
                    params.add(new Pair(x, y));
                    Command cmd = new Command(System.currentTimeMillis(), selectedId, EState.JUMP, params);
                    try {
                        queue.put(cmd);
                        System.out.printf("Player %d issued a JUMP command for %s to [%d,%d]%n", player, selectedId, x, y);
                    } catch (InterruptedException e) {
                        System.out.printf("Error sending command: %s%n", e.getMessage());
                        Thread.currentThread().interrupt();
                    }
                    selectedPieceMap.remove(player); // Reset selection
                } else {
                    System.out.printf("Player %d attempted to jump without selecting a piece%n", player);
                }
            }
        }

    }

    private boolean belongsToPlayer(String pieceId, int player) {
        return pieceId != null && ((player == 1 && pieceId.startsWith("W")) || (player == 2 && pieceId.startsWith("B")));
    }

    // IMPORTANT CHANGE HERE: Add 'player' parameter
    private Piece getPieceAt(int x, int y, int player) {
        if (x >= 0 && x < rows && y >= 0 && y < cols && boardState[x][y] != null) {
            // Retrieve the piece specifically for the given player at this cell
            return boardState[x][y].getOrDefault(player, null);
        }
        return null;
    }
}