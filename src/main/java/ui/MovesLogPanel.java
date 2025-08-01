package ui;

import javax.swing.*;
import java.awt.*;

public class MovesLogPanel extends JPanel {

    private final JTextArea logArea;

    public MovesLogPanel(String title) {
        setPreferredSize(new Dimension(220, 600));
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setOpaque(true);
        titleLabel.setBackground(new Color(30, 40, 60));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        logArea.setForeground(Color.WHITE);
        logArea.setBackground(new Color(45, 55, 75));
        logArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        add(titleLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void addLine(String line) {
        logArea.append(line + "\n");
    }

    public void clearLog() {
        logArea.setText("");
    }
}
