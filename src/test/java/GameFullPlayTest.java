import classes.Command;
import classes.Moves;
import classes.Pair;
import enums.EState;
import game.Game;
import game.GameFactory;
import org.junit.jupiter.api.Test;
import piece.Piece;

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

        Piece pw = game.pos.get(new Pair(6,0)).get(0);
        Piece pb = game.pos.get(new Pair(1,1)).get(0);

        game.userInputQueue.add(new Command(game.game_time_ms(), pw.id, EState.MOVE, java.util.List.of(new Pair(6,0), new Pair(4,0))));
        game.userInputQueue.add(new Command(game.game_time_ms(), pb.id, EState.MOVE, java.util.List.of(new Pair(1,1), new Pair(3,1))));

        runLoops(game, 200);

        assertEquals(new Pair(4,0), pw.currentCell());
        assertEquals(new Pair(3,1), pb.currentCell());

        game.userInputQueue.add(new Command(game.game_time_ms(), pw.id, EState.MOVE, java.util.List.of(new Pair(4,0), new Pair(3,1))));
        runLoops(game, 200);

        assertEquals(new Pair(3,1), pw.currentCell());
        assertTrue(game.pieces.contains(pw));
        assertFalse(game.pieces.contains(pb));
    }
} 