//package ui;
//
//import game.Game;
//
//import javax.swing.*;
//import java.awt.*;
//
//public class GameWindow extends JFrame {
//
//    public GameWindow(Game game) {
//        setTitle("Chess Game");
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        setLayout(new BorderLayout());
//
//        InstructionsPanel instructions = new InstructionsPanel();
//        add(instructions, BorderLayout.EAST);
//
//        BoardPanel boardPanel = new BoardPanel(game);
//        add(boardPanel, BorderLayout.CENTER);
//
//        pack();
//        setLocationRelativeTo(null);
//        setVisible(true);
//
//        // להזיז את זה לפה – אחרי שהחלון כבר נראה
//        SwingUtilities.invokeLater(() -> boardPanel.requestFocusInWindow());
//    }
//}
package ui;

import game.Game;
import javax.swing.*;
import java.awt.*;

public class GameWindow extends JFrame {

    public GameWindow(Game game) {
        setTitle("♛ KFCESS - Real-Time Chess ♛");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(1200, 800));

        JLabel titleLabel = new JLabel("♟️ KFCESS: Real-Time Chess ♟️", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setOpaque(true);
        titleLabel.setBackground(new Color(28, 38, 58));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        add(titleLabel, BorderLayout.NORTH);

        BoardPanel boardPanel = new BoardPanel(game);
        boardPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        add(boardPanel, BorderLayout.CENTER);

        MovesLogPanel player1Log = new MovesLogPanel("Player 1 Moves");
        MovesLogPanel player2Log = new MovesLogPanel("Player 2 Moves");

        add(player1Log, BorderLayout.WEST);
        add(player2Log, BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        SwingUtilities.invokeLater(() -> boardPanel.requestFocusInWindow());
    }
}
