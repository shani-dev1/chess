import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class GameFactoryTest {
    ClassLoader cl = MainGame.class.getClassLoader();
    URI uri = cl.getResource("pieces").toURI();
    Path piecesPath = Paths.get(uri);

    public GameFactoryTest() throws URISyntaxException {
    }


    @Test
    void testCreateGameBuildsFullBoard() {
        Game game = GameFactory.createGame(piecesPath);
        assertNotNull(game);
        assertEquals(32, game.pieces.size());
    }

    @Test
    void testGraphicsFactoryLoadsSprites() {
        GraphicsFactory gf = new GraphicsFactory();
        Path spritesDir = piecesPath.resolve(Path.of("PW", "states", "idle", "sprites"));
        Graphics gfx = gf.load(spritesDir, new org.json.JSONObject(), new java.awt.Dimension(32,32));
        assertFalse(gfx.getFrames().isEmpty());
        for (Img img : gfx.getFrames()) {
            assertTrue(img instanceof Img);
        }
    }
} 