import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class GameFullPlayTest {

    public GameFullPlayTest() throws URISyntaxException {
    }

    private void runLoops(Game game, int loops) {
        game._run_game_loop(loops, false);
    }

    ClassLoader cl = MainGame.class.getClassLoader();
    URI uri = cl.getResource("pieces").toURI();
    Path piecesPath = Paths.get(uri);

    @Test
    void testPawnMoveAndCapture() {
        Game game = GameFactory.createGame(piecesPath);
        game.setTimeFactor(1_000_000_000L);
        game._update_cell2piece_map();

        Piece pw = game.pos.get(new Moves.Pair(6,0)).get(0);
        Piece pb = game.pos.get(new Moves.Pair(1,1)).get(0);

        game.userInputQueue.add(new Command(game.game_time_ms(), pw.id, "move", java.util.List.of(new Moves.Pair(6,0), new Moves.Pair(4,0))));
        game.userInputQueue.add(new Command(game.game_time_ms(), pb.id, "move", java.util.List.of(new Moves.Pair(1,1), new Moves.Pair(3,1))));

        runLoops(game, 200);

        assertEquals(new Moves.Pair(4,0), pw.currentCell());
        assertEquals(new Moves.Pair(3,1), pb.currentCell());

        game.userInputQueue.add(new Command(game.game_time_ms(), pw.id, "move", java.util.List.of(new Moves.Pair(4,0), new Moves.Pair(3,1))));
        runLoops(game, 200);

        assertEquals(new Moves.Pair(3,1), pw.currentCell());
        assertTrue(game.pieces.contains(pw));
        assertFalse(game.pieces.contains(pb));
    }
} 