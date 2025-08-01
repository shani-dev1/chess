//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.KeyAdapter;
//import java.awt.event.KeyEvent;
//import java.util.*;
//import java.util.List;
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.LinkedBlockingQueue;
//
//public class AllInOneGame {
//
//    // === Position ===
//    public static class Position {
//        public final int row, col;
//        public Position(int row, int col) { this.row = row; this.col = col; }
//        public String toString() { return "(" + row + ", " + col + ")"; }
//    }
//
//    // === Command ===
//    public static class Command {
//        private final String action;
//        private final Position position;
//        private final int player;
//        public Command(String action, Position position, int player) {
//            this.action = action; this.position = position; this.player = player;
//        }
//        public String getAction() { return action; }
//        public Position getPosition() { return position; }
//        public int getPlayer() { return player; }
//    }
//
//    // === KeyboardProcessor ===
//    public static class KeyboardProcessor {
//        private final int rows, cols;
//        private final Map<String, String> keymap;
//        private int cursorR = 0, cursorC = 0;
//        public KeyboardProcessor(int rows, int cols, Map<String, String> keymap) {
//            this.rows = rows; this.cols = cols; this.keymap = keymap;
//        }
//
//        public synchronized String processKey(String key) {
//            String action = keymap.get(key);
//            if (action == null) return null;
//            switch (action) {
//                case "up" -> { if (cursorR > 0) cursorR--; }
//                case "down" -> { if (cursorR < rows - 1) cursorR++; }
//                case "left" -> { if (cursorC > 0) cursorC--; }
//                case "right" -> { if (cursorC < cols - 1) cursorC++; }
//            }
//            return action;
//        }
//
//        public synchronized Position getCursor() {
//            return new Position(cursorR, cursorC);
//        }
//    }
//
//    // === KeyboardProducer ===
//    public static class KeyboardProducer {
//        private final KeyboardProcessor processor;
//        private final BlockingQueue<Command> queue;
//        private final int player;
//
//        public KeyboardProducer(KeyboardProcessor processor,
//                                BlockingQueue<Command> queue, int player) {
//            this.processor = processor; this.queue = queue; this.player = player;
//        }
//
//        public void onKey(String keyStr) {
//            String action = processor.processKey(keyStr);
//            if (action != null) {
//                Position pos = processor.getCursor();
//                Command cmd = new Command(action, pos, player);
//                queue.offer(cmd);
//            }
//        }
//    }
//
//    // === Game ===
//    public static class Game implements Runnable {
//        private final BlockingQueue<Command> queue;
//        public Game(BlockingQueue<Command> queue) { this.queue = queue; }
//        @Override public void run() {
//            System.out.println("Game started...");
//            while (true) {
//                try {
//                    Command cmd = queue.take();
//                    System.out.println("Player " + cmd.getPlayer() +
//                            " -> " + cmd.getAction() + " at " + cmd.getPosition());
//                } catch (InterruptedException e) { break; }
//            }
//        }
//    }
//
//    // === Dummy Piece ===
//    public static class Piece {
//        public void drawOnBoard(Board board, long t) {
//            // Placeholder – add logic for drawing if needed
//        }
//    }
//
//    // === Dummy Img ===
//    public static class Img {
//        private final int w, h;
//        public Img(int w, int h) {
//            this.w = w; this.h = h;
//        }
//
//        public void drawRect(int x1, int y1, int x2, int y2, Color color) {
//            System.out.println("Draw rect from ("+x1+","+y1+") to ("+x2+","+y2+") color=" + color);
//        }
//
//        public void show() {
//            System.out.println("Displaying board...");
//        }
//    }
//
//
//    // === GUI Panel ===
//    public static class GamePanel extends JPanel {
//        private final KeyboardProcessor processor;
//        private final Board board = new Board();
//        private Board curr_board = board.cloneBoard();
//
//        public GamePanel(KeyboardProcessor processor) {
//            this.processor = processor;
//            setPreferredSize(new Dimension(400, 400));
//        }
//
//        @Override
//        protected void paintComponent(Graphics g) {
//            super.paintComponent(g);
//            _draw();
//        }
//
//        private void _draw() {
//            curr_board.getImg();
//            drawCursors();
//            _show();
//        }
//
//        private void drawCursors(){
//            Position pos = processor.getCursor();
//            int x = pos.col * board.getCellWPix();
//            int y = pos.row * board.getCellHPix();
//            int x2 = x + board.getCellWPix() - 1;
//            int y2 = y + board.getCellHPix() - 1;
//            curr_board.getImg().drawRect(x, y, x2, y2, Color.BLUE);
//        }
//
//        public void _show() {
//            curr_board.getImg().show();
//        }
//    }
//
//    // === Main ===
//    public static void main(String[] args) {
//        var queue = new LinkedBlockingQueue<Command>();
//        var keymap = Map.of(
//                "W", "up", "A", "left", "S", "down", "D", "right",
//                "ENTER", "choose"
//        );
//        var processor = new KeyboardProcessor(8, 8, keymap);
//        var producer = new KeyboardProducer(processor, queue, 1);
//
//        new Thread(new Game(queue)).start();
//
//        JFrame frame = new JFrame("Chess Input Board");
//        GamePanel panel = new GamePanel(processor);
//        frame.add(panel);
//        frame.pack();
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setFocusable(true);
//
//        frame.addKeyListener(new KeyAdapter() {
//            @Override
//            public void keyPressed(KeyEvent e) {
//                String keyStr = KeyEvent.getKeyText(e.getKeyCode()).toUpperCase();
//                producer.onKey(keyStr);
//                panel.repaint();
//            }
//        });
//
//        frame.setVisible(true);
//    }
//}
