package ui;

import javax.swing.*;
import java.awt.*;

public class InstructionsPanel extends JPanel {

    public InstructionsPanel() {
        setPreferredSize(new Dimension(350, 600));
        setLayout(new BorderLayout());

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setOpaque(false);
        textArea.setForeground(Color.WHITE);
        textArea.setFont(new Font("Arial", Font.BOLD, 16));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setMargin(new Insets(15, 15, 15, 15));

        // Translated instructions text
        textArea.setText(
                "📜 Game Instructions: KFCESS 📜\n\n" +
                        "Welcome to the fast-paced world of KFCESS! Forget everything you know about chess...\n\n" +
                        "👑 The Goal: Capture the opponent's King!\n" +
                        "The game ends in an instant victory when one of you manages to capture the opponent's king.\n\n" +
                        "⏰ Everyone Moves Together! (Real-Time)\n" +
                        "There are no turns. All pieces can move simultaneously. Plan your moves in real-time and attack without waiting!\n\n" +
                        "⚡️ Cooldown\n" +
                        "After each move, your piece needs to rest for a short time before it can move again. Different pieces require different cooldown times.\n\n" +
                        "🤸 Dodge Jump\n" +
                        "Under attack? Use a jump in place to dodge! A jump requires a shorter cooldown than a regular move.\n\n" +
                        "♟️ The Pieces:\n" +
                        "• Classic Pieces: The King, Queen, Rook, Bishop, Knight, and Pawn move like in regular chess.\n" +
                        "• New Piece - 🛸 Hover: Can move to any square within a two-step radius in any direction.\n\n" +
                        "🏆 How to Win?\n" +
                        "The game ends when one player successfully captures the opponent's King. There is no 'check' or 'checkmate' - only a direct capture!\n\n" +
                        "Good luck in the battle!"
        );

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        Color color1 = new Color(28, 71, 122);
        Color color2 = new Color(66, 135, 245);
        GradientPaint gp = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
}