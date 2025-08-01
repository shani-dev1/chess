package ui;
import game.Game;
import piece.Piece;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import classes.Pair;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class BoardPanel extends JPanel {
    private final Game game;

    public BoardPanel(Game game) {
        this.game = game;
        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                String key = KeyEvent.getKeyText(e.getKeyCode()).toLowerCase();
                System.out.println("Key pressed: " + key); // You want to see this!

                // Assuming game.kp1 handles both players based on the key
                // Or you could have separate logic for player 1 and 2 keys
                // For now, as per your code, it's directing to kp1
                game.kp1.processKey(key, 1); // For player 1
                // If player 2 also directly listens to the BoardPanel, you'd add:
                game.kp2.processKey(key, 2);

                repaint(); // Redraw the board after a key press
            }
        });

        // Add a FocusListener to ensure the panel always tries to regain focus
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                System.out.println("BoardPanel gained focus!");
            }

            @Override
            public void focusLost(FocusEvent e) {
                System.out.println("BoardPanel lost focus! Attempting to regain...");
                // Request focus again if it's lost
                // This is crucial for consistent keyboard input
                requestFocusInWindow();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        BufferedImage image = game.getCurrentBoardImage(); // includes board + pieces
        if (image != null) {
            g.drawImage(image, 0, 0, null);
        }

        drawCursors(g);
        drawSelectedHighlights(g);
    }

    private void drawCursors(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(4));

        int cellH = game.board.getCellHPix();
        int cellW = game.board.getCellWPix();

        // Player 1
        int[] c1 = game.kp1.getCursor(1);
        g2.setColor(Color.RED);
        g2.drawRect(c1[1] * cellW, c1[0] * cellH, cellW - 1, cellH - 1);

        // Player 2
        int[] c2 = game.kp2.getCursor(2);
        g2.setColor(Color.BLUE);
        g2.drawRect(c2[1] * cellW, c2[0] * cellH, cellW - 1, cellH - 1);
    }


    private void drawSelectedHighlights(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(3));
        int cellH = game.board.getCellHPix();
        int cellW = game.board.getCellWPix();

        if (game.selected_id_1 != null) {
            Piece p = game.pieceById.get(game.selected_id_1);
            if (p != null) {
                Pair cell = p.currentCell();
                g2.setColor(Color.YELLOW);
                g2.drawRect(cell.c * cellW, cell.r * cellH, cellW - 1, cellH - 1);
            }
        }

        if (game.selected_id_2 != null) {
            Piece p = game.pieceById.get(game.selected_id_2);
            if (p != null) {
                Pair cell = p.currentCell();
                g2.setColor(Color.YELLOW);
                g2.drawRect(cell.c * cellW, cell.r * cellH, cellW - 1, cellH - 1);
            }
        }
    }
}