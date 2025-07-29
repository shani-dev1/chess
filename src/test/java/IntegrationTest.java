import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

public class IntegrationTest {
    private static final Path PIECES_DIR = Path.of("..", "pieces");

    @Test
    void testGameInitialisesFromCsv() {
        Game game = GameFactory.createGame(PIECES_DIR);
        assertNotNull(game);
        assertEquals(32, game.pieces.size());
    }

    @Test
    void testWinConditionDetectsMissingKing() {
        Game game = GameFactory.createGame(PIECES_DIR);
        // remove black king
        Piece blackKing = game.pieces.stream().filter(p -> p.id.startsWith("KB_")).findFirst().orElse(null);
        assertNotNull(blackKing);
        game.pieces.remove(blackKing);
        assertTrue(game._is_win());
    }
} 