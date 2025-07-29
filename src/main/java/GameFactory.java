import java.nio.file.*;
import java.awt.Dimension;
import java.util.*;

public class GameFactory {
    // Convenience method to create a Game from a string path
    public static Game createGame(String piecesRootStr) {
        // Convert string path to Path and delegate to the Path version
        return createGame(Path.of(piecesRootStr));
    }

    // Constant cell size in pixels (width and height)
    final static int CELL_PX = 64;

    // Main method to create a Game given a directory path containing resources
    public static Game createGame(Path piecesRoot) {

        Path boardPng = piecesRoot.resolve("board.png");
        if (!Files.exists(boardPng)) {
            throw new RuntimeException("File not found: " + boardPng.toAbsolutePath());
        }
        Dimension boardSize = new Dimension(CELL_PX * 8, CELL_PX * 8);
        ImgFactory imgFactory = new ImgFactory();
        Img boardImg = imgFactory.create(boardPng.toString(), boardSize, false);
        Board board = new Board(CELL_PX, CELL_PX, 8, 8, boardImg);

        // Create a PieceFactory with the board (used to create pieces from resources)
        PieceFactory pFactory = new PieceFactory(board);
        try {
            // Generate the piece templates/library by reading pieces data from the directory
            pFactory.generateLibrary(piecesRoot);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build piece library", e);
        }

        // Read the CSV file that defines the initial board layout (piece codes per cell)
        Path csvPath = piecesRoot.resolve("board.csv");
        List<Piece> pieces = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(csvPath);
            for (int row = 0; row < lines.size(); row++) {
                // Split each line by commas to get the codes for each column
                String[] tokens = lines.get(row).strip().split(",");
                for (int col = 0; col < tokens.length; col++) {
                    String code = tokens[col].trim();
                    if (code.isEmpty()) continue;

                    // Create a piece object for this code at (row, col)
                    Piece piece = pFactory.createPiece(code, new Moves.Pair(row, col));
                    pieces.add(piece);
                }
            }
        } catch (Exception e) {
            // If reading/parsing CSV fails, throw a runtime exception
            throw new RuntimeException("Failed to parse board.csv", e);
        }

        // Return a new Game initialized with all created pieces and the board
        return new Game(pieces, board);
    }
}
