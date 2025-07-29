import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * BufferedImage-backed implementation of Img.
 * It keeps all heavyweight graphics logic internal so that the rest of the code can remain platform-agnostic.
 */
public class BuffImg implements Img {

    private BufferedImage img; // The internal image storage

    /* -------------- constructors -------------- */

    public BuffImg() {} // Empty constructor

    public BuffImg(int w, int h) {
        // Creates a new image with given width and height using ARGB format (supports transparency)
        this.img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    }

    public BuffImg(BufferedImage src) {
        // Wraps an existing BufferedImage
        this.img = src;
    }

    /* -------------- helper (for Board.cloneBoard etc.) -------------- */

    void setBufferedImage(BufferedImage bi) {
        // Allows setting the internal image manually
        this.img = bi;
    }

    /* -------------- loading -------------- */

//    change the img size
    @Override
    public Img read(String path, Dimension targetSize, boolean keepAspect, Object interpolation) {
        // Loads an image from file and optionally resizes it
        try {
            img = ImageIO.read(new File(path)); // Reads image from disk
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot load image: " + path);
        }
        if (img == null)
            throw new IllegalArgumentException("Unsupported image: " + path);

        // If resizing is requested
        if (targetSize != null) {
            int tw = targetSize.width, th = targetSize.height;
            int w = img.getWidth(), h = img.getHeight();
            int nw, nh;

            if (keepAspect) {
                // Maintain aspect ratio
                double s = Math.min(tw / (double) w, th / (double) h);
                nw = (int) Math.round(w * s);
                nh = (int) Math.round(h * s);
            } else {
                nw = tw;
                nh = th;
            }

            // Create resized version
            BufferedImage dst = new BufferedImage(nw, nh,
                    img.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
            Graphics2D g = dst.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(img, 0, 0, nw, nh, null);
            g.dispose();
            img = dst;
        }

        return this;
    }

    @Override
    public Img read(String path) {
        // Convenience method: load image without resizing
        return read(path, null, false, null);
    }

    /* -------------- drawing -------------- */

    @Override
    public void drawOn(Img other, int x, int y) {
        // Draws this image onto another image at position (x, y)
        System.out.printf("x    " + x + "y" + y);
        if (img == null || other.get() == null)
            throw new IllegalStateException("Both images must be loaded.");

        BufferedImage dstImg = other.get();
        if (x + img.getWidth() > dstImg.getWidth() || y + img.getHeight() > dstImg.getHeight())
            throw new IllegalArgumentException("Patch exceeds destination bounds.");

        Graphics2D g = dstImg.createGraphics();
        g.setComposite(AlphaComposite.SrcOver); // Enables transparency handling
        g.drawImage(img, x, y, null); // Draw the image
        g.dispose();
    }

    @Override
    public void putText(String txt, int x, int y, float fontSize, Color color, int thickness) {
        // Draw text on the image
        if (img == null) throw new IllegalStateException("Image not loaded.");
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(color);
        g.setFont(g.getFont().deriveFont(fontSize * 12)); // Multiply font size for visibility
        g.drawString(txt, x, y);
        g.dispose();
    }

    @Override
    public void drawRect(int x1, int y1, int x2, int y2, Color color) {
        // Draw a rectangle between (x1, y1) and (x2, y2)
        if (img == null) throw new IllegalStateException("Image not loaded.");
        Graphics2D g = img.createGraphics();
        g.setColor(color);
        g.drawRect(x1, y1, x2 - x1, y2 - y1);
        g.dispose();
    }

    /* -------------- display -------------- */

    @Override
    public void show() {
        // Display the image in a new window using Swing
        if (img == null) throw new IllegalStateException("Image not loaded.");
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Image");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.add(new JLabel(new ImageIcon(img)));
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }

    /* -------------- access -------------- */
    @Override
    public BufferedImage get() {
        // Get the internal BufferedImage
        return img;
    }
}
