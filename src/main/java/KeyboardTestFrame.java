import keyBoard.KeyboardProcessor;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class KeyboardTestFrame extends JFrame {
    private final KeyboardProcessor keyboardProcessor;

    public KeyboardTestFrame() {
        setTitle("Keyboard Test");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // מפת המקשים לדוגמה
        Map<String, String> keyMap = Map.of(
                "UP", "up",
                "DOWN", "down",
                "LEFT", "left",
                "RIGHT", "right",
                "ENTER", "select",
                "SPACE", "jump"
        );

        // נוצר queue ריק, boardState לא נחוץ כאן לדוגמה
        keyboardProcessor = new KeyboardProcessor(8, 8, keyMap, new LinkedBlockingQueue<>(), new Map[8][8]);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                String keyText = KeyEvent.getKeyText(e.getKeyCode()).toUpperCase();
                keyboardProcessor.processKey(keyText, 1); // לדוגמה שחקן 1
            }
        });

        setVisible(true);
        requestFocusInWindow();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(KeyboardTestFrame::new);
    }
}
