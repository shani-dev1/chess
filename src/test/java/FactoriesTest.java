import img.MockImg;
import physics.IdlePhysics.IdlePhysics;
import img.Img;
import board.Board;
import classes.Pair;
import grafix.GraphicsFactory;
import org.junit.jupiter.api.Test;
import physics.IdlePhysics.JumpPhysics;
import physics.IdlePhysics.MovePhysics;
import physics.IdlePhysics.RestPhysics;
import physics.Physics;
import physics.PhysicsFactory;
import piece.Piece;
import piece.PieceFactory;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public class FactoriesTest {
    private static Board board() {
        int cell = 32;
        Img bg = new MockImg(cell*8, cell*8);
        return new Board(cell, cell, 8,8, bg);
    }

    private static final Path PIECES_DIR = Path.of("..", "pieces");

    /* ---------------- PHYSICS FACTORY ---------------- */
    @Test
    void testPhysicsFactoryCreatesSubclasses() {
        Board b = board();
        PhysicsFactory pf = new PhysicsFactory(b);

        Physics idle = pf.create(new Pair(0,0), "idle", new org.json.JSONObject());
        assertTrue(idle instanceof IdlePhysics);

        org.json.JSONObject moveCfg = new org.json.JSONObject().put("speed_m_per_sec", 2.0);
        Physics move = pf.create(new Pair(0,0), "move", moveCfg);
        assertTrue(move instanceof MovePhysics);
        assertEquals(2.0, ((MovePhysics)move).getSpeedCellsPerSec());

        Physics jump = pf.create(new Pair(0,0), "jump", new org.json.JSONObject());
        assertTrue(jump instanceof JumpPhysics);

        org.json.JSONObject restCfg = new org.json.JSONObject().put("duration_ms", 500);
        Physics rest = pf.create(new Pair(0,0), "long_rest", restCfg);
        assertTrue(rest instanceof RestPhysics);
        assertEquals(0.5, ((RestPhysics)rest).getDurationSec(), 1e-6);
    }

    /* ---------------- PIECE FACTORY ---------------- */
    @Test
    void testPieceFactoryGeneratesAllPieces() throws Exception {
        Board b = board();
        GraphicsFactory gfxFactory = new GraphicsFactory();
        PieceFactory pFactory = new PieceFactory(b);
        pFactory.generateLibrary(PIECES_DIR);

        int i=0, j=0;
        Set<String> ids = new HashSet<>();
        String[] types = {"B","K","P","Q","R"};
        String[] colors = {"W","B"};
        int created = 0;
        for (String t: types) {
            for (String c: colors) {
                String code = t + c;
                Pair loc = new Pair(i, j);
                Piece p = pFactory.createPiece(code, loc);
                assertTrue(p.id.startsWith(code + "_"));
                assertEquals(loc, p.currentCell());
                ids.add(p.id);
                created++;
                i++;
                if (i >= b.getHCells()) { i=0; j++; }
            }
        }
        assertEquals(created, ids.size());
    }
} 