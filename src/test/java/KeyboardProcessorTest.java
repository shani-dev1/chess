import keyBoard.KeyboardProcessor;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import static org.junit.jupiter.api.Assertions.*;

public class KeyboardProcessorTest {
    private static java.util.Map<String,String> defaultKeymap() {
        return Map.of(
                "w", "up",
                "s", "down",
                "a", "left",
                "d", "right",
                "enter", "choose",
                "+", "jump"
        );
    }

    @Test
    void testInitialPosition() {
        KeyboardProcessor kp = new KeyboardProcessor(8,8, defaultKeymap());
        assertArrayEquals(new int[]{0,0}, kp.getCursor());
    }

    @Test
    void testCursorMovesAndWraps() {
        KeyboardProcessor kp = new KeyboardProcessor(2,3, defaultKeymap());
        // up at top-left stays put
        kp.processKey("w");
        assertArrayEquals(new int[]{0,0}, kp.getCursor());
        // down moves to (1,0)
        kp.processKey("s");
        assertArrayEquals(new int[]{1,0}, kp.getCursor());
        // down again still at bottom row
        kp.processKey("s");
        assertArrayEquals(new int[]{1,0}, kp.getCursor());
        // left at col 0 stays
        kp.processKey("a");
        assertArrayEquals(new int[]{1,0}, kp.getCursor());
        // right to (1,1)
        kp.processKey("d");
        assertArrayEquals(new int[]{1,1}, kp.getCursor());
    }

    @Test
    void testChooseAndJumpActions() {
        KeyboardProcessor kp = new KeyboardProcessor(5,5, defaultKeymap());
        kp.setCursor(3,4);
        assertEquals("choose", kp.processKey("enter"));
        assertEquals("jump", kp.processKey("+"));
        // unknown key returns null
        assertNull(kp.processKey("x"));
        assertEquals("up", kp.processKey("w"));
    }

    @Test
    void testCustomKeymap() {
        Map<String,String> km2 = Map.of(
                "i","up",
                "k","down",
                "j","left",
                "l","right",
                "o","choose",
                "p","jump"
        );
        KeyboardProcessor kp = new KeyboardProcessor(5,5, km2);
        kp.processKey("i"); // up
        assertArrayEquals(new int[]{0,0}, kp.getCursor());
        kp.processKey("k"); // down
        assertArrayEquals(new int[]{1,0}, kp.getCursor());
        kp.setCursor(2,2);
        assertEquals("choose", kp.processKey("o"));
        assertEquals("jump", kp.processKey("p"));
    }

    @Test
    void testThreadSafety() throws InterruptedException {
        KeyboardProcessor kp = new KeyboardProcessor(8,8, Map.of(
                "up","up",
                "down","down",
                "left","left",
                "right","right"
        ));
        String[] seq1 = {"up","up","left","down","right"};
        String[] seq2 = {"down","right","right","up","left"};
        int iterations = 500;
        CountDownLatch latch = new CountDownLatch(2);
        Runnable worker = (Runnable) () -> {
            for (int i=0;i<iterations;i++) {
                for (String k : seq1) kp.processKey(k);
            }
            latch.countDown();
        };
        Runnable worker2 = (Runnable) () -> {
            for (int i=0;i<iterations;i++) {
                for (String k : seq2) kp.processKey(k);
            }
            latch.countDown();
        };
        new Thread(worker).start();
        new Thread(worker2).start();
        latch.await();
        int[] cur = kp.getCursor();
        assertTrue(cur[0]>=0 && cur[0]<8 && cur[1]>=0 && cur[1]<8);
    }
} 