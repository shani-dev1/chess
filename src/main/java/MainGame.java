import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainGame {
    public static void main(String[] args) throws Exception {
        ClassLoader cl = MainGame.class.getClassLoader();
        URI uri = cl.getResource("pieces").toURI();
        Path piecesPath = Paths.get(uri);


        Game game = GameFactory.createGame(piecesPath);

        game.run();
    }
}
