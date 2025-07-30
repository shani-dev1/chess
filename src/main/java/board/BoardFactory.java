package board;

import img.BuffImg;
import img.Img;

import java.nio.file.Path;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class BoardFactory {
    // Static method to load a default board image and create a board.Board object
    public static Board loadDefault(Path boardImgPath) {
        try {
            // Get the class loader to load resources from the classpath
            ClassLoader cl = BoardFactory.class.getClassLoader();

            // Load a BufferedImage of the board from the classpath resource (inside your JAR or project resources)
            BufferedImage img = ImageIO.read(cl.getResourceAsStream("pieces/board.png"));

            // Assume the board is 8x8 cells, so each cell is width / 8 pixels
            int cell = img.getWidth() / 8;

            // Create an Img.Img object by reading the file at the provided path
            Img bg = new BuffImg().read(boardImgPath.toString());

            // Create and return a board.Board object with 8x8 cells and each cell 'cell' pixels wide/high
            return new Board(cell, cell, 8, 8, bg);

        } catch(Exception e) {
            // If any exception occurs during loading, print the error and throw a runtime exception
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
